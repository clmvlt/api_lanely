<#
  seed-vehicles.ps1 - Create test vehicles for a company, via the HTTP API.

  Business rules apply (MANAGE_VEHICLES permission, registration plate unique per
  company, etc.). Each created vehicle also gets one initial mileage reading.

  The access token and company id are read from the shared config (config.ps1).

  Usage:
    ./scripts/seed-vehicles.ps1
    ./scripts/seed-vehicles.ps1 -Count 25 -BaseUrl http://localhost:8090
#>

param(
    [string]$BaseUrl,
    [int]   $Count = 10,
    [string]$Lang
)

. "$PSScriptRoot/config.ps1"

if (-not $BaseUrl) { $BaseUrl = $LanelyBaseUrl }
if (-not $Lang)    { $Lang    = $LanelyLang }
$Token     = $LanelyToken
$CompanyId = $LanelyCompanyId

$ErrorActionPreference = "Stop"
$endpoint = "$BaseUrl/companies/$CompanyId/vehicles"
$headers  = @{
    "Authorization"   = "Bearer $Token"
    "Accept-Language" = $Lang
}

# --- Data pools to generate varied vehicles ---
$plateLetters = @('AA','AB','BC','CD','DE','EF','FG','GH','HJ','JK','KL','LM','MN','NP','PQ','QR','RS','ST','TV','VW')
$makes        = @('Renault','Mercedes-Benz','Iveco','MAN','Volvo','Scania','Peugeot','Citroen','Ford','DAF')
$models       = @('Master','Sprinter','Daily','TGE','FH','R450','Boxer','Jumper','Transit','XF')
$versions     = @('L2H2 dCi 135','316 CDI','35C16','3.140','460 Globetrotter','Highline','BlueHDi 140','L3H2','350 L3','480 FT')
$types        = @('VAN','TRUCK','SEMI_TRAILER','TRUCK','VAN','TRUCK','VAN','TRAILER','TRUCK','CAR')
$fuels        = @('DIESEL','DIESEL','ELECTRIC','DIESEL','HYBRID','DIESEL','PLUGIN_HYBRID','DIESEL','CNG','PETROL')
$emissions    = @('Euro 6','Euro 6d','Crit''Air 1','Crit''Air 2')
$insurers     = @('AXA Fleet','Allianz Pro','MAAF Entreprise','Groupama Flotte','Generali')
$coverages    = @('comprehensive','third-party','third-party-plus')

$today = Get-Date

function New-VehiclePayload([int]$i) {
    $l1   = $plateLetters[$i % $plateLetters.Count]
    $l2   = $plateLetters[($i * 3) % $plateLetters.Count]
    $plate = "$l1-$($i.ToString('000'))-$l2"

    $type = $types[$i % $types.Count]
    $fuel = $fuels[$i % $fuels.Count]

    $firstReg   = $today.AddYears(-(($i % 6) + 1)).AddDays(-($i * 7)).ToString('yyyy-MM-dd')
    $insStart   = $today.AddMonths(-(($i % 11) + 1)).ToString('yyyy-MM-dd')
    $insEnd     = $today.AddMonths((12 - ($i % 11))).ToString('yyyy-MM-dd')
    $techInsp   = $today.AddMonths((($i % 18) + 1)).ToString('yyyy-MM-dd')
    $roadTax    = $today.AddMonths((($i % 12) + 1)).ToString('yyyy-MM-dd')

    $payload = @{
        registrationPlate             = $plate
        vin                           = ("VF1{0}" -f (10000000000000 + $i)).Substring(0, 17)
        make                          = $makes[$i % $makes.Count]
        model                         = $models[$i % $models.Count]
        version                       = $versions[$i % $versions.Count]
        vehicleType                   = $type
        fuelType                      = $fuel
        firstRegistrationDate         = $firstReg
        emissionClass                 = $emissions[$i % $emissions.Count]
        grossWeightKg                 = (3500 + ($i % 8) * 1500)
        payloadKg                     = (1000 + ($i % 8) * 800)
        registrationCertificateNumber = "{0}AB{1}" -f $today.Year, (10000 + $i)
        insurance                     = @{
            insurerName  = $insurers[$i % $insurers.Count]
            policyNumber = "POL-{0}-{1}" -f $today.Year, $i.ToString('00000')
            coverageType = $coverages[$i % $coverages.Count]
            startDate    = $insStart
            endDate      = $insEnd
            contact      = "+331" + (40000000 + $i)
        }
        technicalInspectionDate       = $techInsp
        roadTaxDueDate                = $roadTax
        notes                         = "Test vehicle #$i ($type)."
    }
    return $payload
}

Write-Host "Seeding $Count vehicles -> $endpoint" -ForegroundColor Cyan
$ok = 0
$ko = 0
$firstError = $null

for ($i = 1; $i -le $Count; $i++) {
    $body = (New-VehiclePayload $i | ConvertTo-Json -Depth 6 -Compress)
    try {
        $resp = Invoke-RestMethod -Method Post -Uri $endpoint -Headers $headers `
            -ContentType "application/json; charset=utf-8" -Body $body
        $ok++

        # Add one initial mileage reading for the freshly created vehicle.
        try {
            $reading = @{
                valueKm    = (50000 + $i * 1234)
                recordedAt = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
                note       = "Initial odometer reading (seed)."
            } | ConvertTo-Json -Compress
            Invoke-RestMethod -Method Post -Uri "$endpoint/$($resp.id)/mileage-readings" -Headers $headers `
                -ContentType "application/json; charset=utf-8" -Body $reading | Out-Null
        } catch {}

        Write-Host ("  {0,3}/{1}  plate={2}" -f $i, $Count, $resp.registrationPlate) -ForegroundColor DarkGray
    }
    catch {
        $ko++
        $status = $null
        try { $status = $_.Exception.Response.StatusCode.value__ } catch {}
        if (-not $firstError) {
            $firstError = "HTTP $status - " + $_.Exception.Message
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $firstError = $firstError + " | body: " + $reader.ReadToEnd()
            } catch {}
        }
        Write-Host ("  {0,3}/{1}  ERROR (HTTP {2})" -f $i, $Count, $status) -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Done: $ok created, $ko failed." -ForegroundColor $(if ($ko -eq 0) { "Green" } else { "Yellow" })
if ($firstError) { Write-Host "First error: $firstError" -ForegroundColor Yellow }
