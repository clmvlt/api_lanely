<#
  seed-ldv.ps1 - Create test waybills (lettres de voiture) for a company, based on its existing clients.

  The script first lists the company's clients and their addresses (via the HTTP API), keeps those
  that have at least one geolocated address, then creates waybills pairing two distinct clients:
    - ordering customer (donneur d'ordre) + shipper  = client A (its primary geolocated address)
    - consignee                                       = client B (its primary geolocated address)
  Places of taking over / delivery are linked to those clients/addresses, so the API derives the
  coordinates and computes the round-trip route on its own.

  Each waybill is created in DRAFT, then moved to a realistic status (ISSUED, COLLECTED, IN_TRANSIT,
  DELIVERED, FAILED) for a varied dataset; a share is left in DRAFT.

  Business rules apply (MANAGE_TRANSPORTS permission, WBL-XXXX reference generation, etc.).
  The access token and company id are read from the shared config (config.ps1).

  Usage:
    ./scripts/seed-ldv.ps1
    ./scripts/seed-ldv.ps1 -Count 50 -BaseUrl http://localhost:8090
#>

param(
    [string]$BaseUrl,
    [int]   $Count = 30,
    [string]$Lang
)

. "$PSScriptRoot/config.ps1"

if (-not $BaseUrl) { $BaseUrl = $LanelyBaseUrl }
if (-not $Lang)    { $Lang    = $LanelyLang }
$Token     = $LanelyToken
$CompanyId = $LanelyCompanyId

$ErrorActionPreference = "Stop"
$clientsEndpoint  = "$BaseUrl/companies/$CompanyId/clients"
$waybillsEndpoint = "$BaseUrl/companies/$CompanyId/waybills"
$headers  = @{
    "Authorization"   = "Bearer $Token"
    "Accept-Language" = $Lang
}

function Iso-Utc([datetime]$dt) { $dt.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ") }

# --- Pools to vary the goods carried. ---
$goodsPool = @(
    @{ desc = "Palletized canned food";        pack = "Pallet";      pkg = 12; w = 980.0;  v = 2.4 },
    @{ desc = "Refrigerated dairy products";    pack = "Roll cage";   pkg = 6;  w = 540.0;  v = 1.8 },
    @{ desc = "Furniture flat packs";           pack = "Crate";       pkg = 4;  w = 320.0;  v = 3.6 },
    @{ desc = "Automotive spare parts";         pack = "Pallet";      pkg = 8;  w = 1250.0; v = 1.5 },
    @{ desc = "Bottled wine cases";             pack = "Pallet";      pkg = 16; w = 1120.0; v = 1.6 },
    @{ desc = "Building materials";             pack = "Bundle";      pkg = 5;  w = 2100.0; v = 2.0 },
    @{ desc = "Cosmetics parcels";              pack = "Carton";      pkg = 24; w = 210.0;  v = 1.1 },
    @{ desc = "Pharmaceutical totes";           pack = "Tote";        pkg = 10; w = 180.0;  v = 0.9 },
    @{ desc = "Outdoor equipment";              pack = "Carton";      pkg = 14; w = 430.0;  v = 2.7 },
    @{ desc = "Books and printed matter";       pack = "Pallet";      pkg = 9;  w = 760.0;  v = 1.3 }
)

# Status mix: index-based, realistic distribution.
$statusPlan = @(
    "DELIVERED","DELIVERED","DELIVERED",
    "IN_TRANSIT","IN_TRANSIT",
    "COLLECTED",
    "ISSUED","ISSUED","ISSUED",
    "DRAFT","DRAFT",
    "FAILED"
)

# ----------------------------------------------------------------------------
# 1. Load the company's clients and their geolocated addresses.
# ----------------------------------------------------------------------------
Write-Host "Loading clients from $clientsEndpoint ..." -ForegroundColor Cyan
$page = Invoke-RestMethod -Method Get -Uri "${clientsEndpoint}?status=ACTIVE&size=200&sort=name" -Headers $headers
$summaries = $page.content

$pool = @()
foreach ($s in $summaries) {
    try {
        $addresses = Invoke-RestMethod -Method Get -Uri "$clientsEndpoint/$($s.id)/addresses" -Headers $headers
    } catch { continue }
    $geo = @($addresses | Where-Object { $_.latitude -ne $null -and $_.longitude -ne $null })
    if ($geo.Count -eq 0) { continue }
    $primary = ($geo | Where-Object { $_.isPrimary } | Select-Object -First 1)
    if (-not $primary) { $primary = $geo[0] }
    $pool += [pscustomobject]@{
        Id          = $s.id
        Name        = $s.name
        Phone       = $s.phone
        Email       = $s.email
        AddressId   = $primary.id
        City        = $primary.address.city
        ContactName = $primary.contactName
        ContactPhone= $primary.contactPhone
    }
}

Write-Host "Eligible clients (with a geolocated address): $($pool.Count)" -ForegroundColor Cyan
if ($pool.Count -lt 2) {
    Write-Host "Need at least 2 geolocated clients to create waybills. Run seed-clients.ps1 first." -ForegroundColor Red
    return
}

# ----------------------------------------------------------------------------
# 2. Create the waybills.
# ----------------------------------------------------------------------------
Write-Host "Seeding $Count waybills -> $waybillsEndpoint" -ForegroundColor Cyan
$today = (Get-Date).Date
$ok = 0
$ko = 0
$firstError = $null

for ($i = 0; $i -lt $Count; $i++) {
    $a = $pool[$i % $pool.Count]
    $b = $pool[($i + 1 + ($i % ($pool.Count - 1))) % $pool.Count]
    if ($b.Id -eq $a.Id) { $b = $pool[($i + 1) % $pool.Count] }

    $status = $statusPlan[$i % $statusPlan.Count]

    switch ($status) {
        "DELIVERED"  { $dayOffset = -10 + ($i % 8) }
        "FAILED"     { $dayOffset = -7  + ($i % 5) }
        "IN_TRANSIT" { $dayOffset = -1 }
        "COLLECTED"  { $dayOffset = 0 }
        "ISSUED"     { $dayOffset = 1 + ($i % 5) }
        default      { $dayOffset = 3 + ($i % 8) }   # DRAFT
    }
    $pickup   = $today.AddDays($dayOffset).AddHours(8)
    $delivery = $pickup.AddHours(6)

    $g1 = $goodsPool[$i % $goodsPool.Count]
    $g2 = $goodsPool[($i + 3) % $goodsPool.Count]
    $goodsLines = @(
        @{ description = $g1.desc; packagingType = $g1.pack; numberOfPackages = $g1.pkg; grossWeightKg = $g1.w; volumeM3 = $g1.v; dangerousGoods = $false }
    )
    if (($i % 3) -eq 0) {
        $goodsLines += @{ description = $g2.desc; packagingType = $g2.pack; numberOfPackages = $g2.pkg; grossWeightKg = $g2.w; volumeM3 = $g2.v; dangerousGoods = $false }
    }

    $payload = @{
        scope    = "NATIONAL"
        clientId = $a.Id
        shipper  = @{
            name            = $a.Name
            clientId        = $a.Id
            clientAddressId = $a.AddressId
            contactName     = $a.ContactName
            contactPhone    = $a.ContactPhone
        }
        consignee = @{
            name            = $b.Name
            clientId        = $b.Id
            clientAddressId = $b.AddressId
            contactName     = $b.ContactName
            contactPhone    = $b.ContactPhone
        }
        placeOfTakingOver = @{ clientId = $a.Id; clientAddressId = $a.AddressId; plannedAt = (Iso-Utc $pickup) }
        placeOfDelivery   = @{ clientId = $b.Id; clientAddressId = $b.AddressId; plannedAt = (Iso-Utc $delivery) }
        goodsLines        = $goodsLines
        carriageChargesAmount   = [math]::Round(250 + ($i * 37.5) % 900, 2)
        carriageChargesCurrency = "EUR"
        senderInstructions      = "Handle with care. Call the consignee 30 min before delivery."
        notes                   = "Seed waybill #$($i + 1): $($a.City) -> $($b.City)."
    }

    try {
        $body = ($payload | ConvertTo-Json -Depth 8 -Compress)
        $wb = Invoke-RestMethod -Method Post -Uri $waybillsEndpoint -Headers $headers `
            -ContentType "application/json; charset=utf-8" -Body $body

        if ($status -ne "DRAFT") {
            $statusBody = @{ status = $status }
            if ($status -eq "FAILED") { $statusBody.failureReason = "Recipient absent, no safe place available." }
            $statusJson = ($statusBody | ConvertTo-Json -Compress)
            $null = Invoke-RestMethod -Method Post -Uri "$waybillsEndpoint/$($wb.id)/status" -Headers $headers `
                -ContentType "application/json; charset=utf-8" -Body $statusJson
        }

        $ok++
        Write-Host ("  {0,2}/{1}  {2,-10} {3,-30} {4} -> {5}" -f ($i + 1), $Count, $status, $wb.reference, $a.City, $b.City) -ForegroundColor DarkGray
    }
    catch {
        $ko++
        $code = $null
        try { $code = $_.Exception.Response.StatusCode.value__ } catch {}
        if (-not $firstError) {
            $firstError = "HTTP $code - " + $_.Exception.Message
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $firstError = $firstError + " | body: " + $reader.ReadToEnd()
            } catch {}
        }
        Write-Host ("  {0,2}/{1}  ERROR (HTTP {2})" -f ($i + 1), $Count, $code) -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Done: $ok created, $ko failed." -ForegroundColor $(if ($ko -eq 0) { "Green" } else { "Yellow" })
if ($firstError) { Write-Host "First error: $firstError" -ForegroundColor Yellow }
