<#
  seed-clients.ps1 - Create a curated set of ~20 realistic test clients for a company, via the HTTP API.

  Each client is created with the POST /companies/{id}/clients endpoint, then a primary
  address is added via POST /companies/{id}/clients/{clientId}/addresses with real, accurate
  WGS84 coordinates matching the postal address (so the map view is realistic).

  Business rules apply (MANAGE_CLIENTS permission, CLI-XXXX reference generation,
  country derived from the company, etc.).

  The access token and company id are read from the shared config (config.ps1).

  Usage:
    ./scripts/seed-clients.ps1
    ./scripts/seed-clients.ps1 -BaseUrl http://localhost:8090
#>

param(
    [string]$BaseUrl,
    [string]$Lang
)

. "$PSScriptRoot/config.ps1"

if (-not $BaseUrl) { $BaseUrl = $LanelyBaseUrl }
if (-not $Lang)    { $Lang    = $LanelyLang }
$Token     = $LanelyToken
$CompanyId = $LanelyCompanyId

$ErrorActionPreference = "Stop"
$clientsEndpoint = "$BaseUrl/companies/$CompanyId/clients"
$headers  = @{
    "Authorization"   = "Bearer $Token"
    "Accept-Language" = $Lang
}

# --- Curated, realistic clients of a French transport company. ---
# Companies (manufacturers, distributors, retailers, e-commerce) + a few individuals.
# Coordinates are real WGS84 points matching the street/city.
$clients = @(
    @{
        type = "COMPANY"; name = "Maison Lefevre Vins"; legalForm = "SAS"; reg = "412563987"; vat = "FR40412563987"
        email = "logistique@maison-lefevre.fr"; phone = "+33380414253"; website = "https://maison-lefevre.fr"
        terms = 45; lang = "fr"; notes = "Negociant en vins, livraisons palettisees vers la grande distribution."
        label = "Entrepot principal"; addrType = "DEPOT"
        line1 = "14 Rue des Tonneliers"; postal = "21000"; city = "Dijon"; lat = 47.3220; lng = 5.0415
        contactName = "Bernard Lefevre"; contactPhone = "+33380414254"
        instr = "Quai numero 2, cariste sur place de 7h a 16h."
    },
    @{
        type = "COMPANY"; name = "AtlantiqueFroid Distribution"; legalForm = "SARL"; reg = "528741036"; vat = "FR28528741036"
        email = "expeditions@atlantiquefroid.fr"; phone = "+33240356789"; website = "https://atlantiquefroid.fr"
        terms = 30; lang = "fr"; notes = "Produits surgeles, exige une chaine du froid continue (-18 C)."
        label = "Plateforme logistique"; addrType = "DEPOT"
        line1 = "8 Boulevard de Seattle"; postal = "44300"; city = "Nantes"; lat = 47.2542; lng = -1.5246
        contactName = "Helene Caron"; contactPhone = "+33240356790"
        instr = "Camions frigorifiques uniquement. Hayon obligatoire."
    },
    @{
        type = "COMPANY"; name = "Provence Olives & Co"; legalForm = "SAS"; reg = "631458720"; vat = "FR12631458720"
        email = "contact@provence-olives.fr"; phone = "+33442238190"; website = "https://provence-olives.fr"
        terms = 30; lang = "fr"; notes = "Producteur d'huile d'olive et conserves, expeditions hebdomadaires."
        label = "Moulin et entrepot"; addrType = "DEPOT"
        line1 = "120 Route de Berre"; postal = "13100"; city = "Aix-en-Provence"; lat = 43.5297; lng = 5.4474
        contactName = "Marc Rossi"; contactPhone = "+33442238191"
        instr = "Acces poids lourds par l'arriere, sonner au portail B."
    },
    @{
        type = "COMPANY"; name = "Atelier du Meuble Lyonnais"; legalForm = "SARL"; reg = "749852136"; vat = "FR65749852136"
        email = "livraisons@meuble-lyonnais.fr"; phone = "+33472100245"; website = "https://meuble-lyonnais.fr"
        terms = 60; lang = "fr"; notes = "Mobilier sur mesure, colis volumineux et fragiles."
        label = "Showroom Confluence"; addrType = "SHIPPING"
        line1 = "26 Cours Charlemagne"; postal = "69002"; city = "Lyon"; lat = 45.7407; lng = 4.8186
        contactName = "Sophie Garnier"; contactPhone = "+33472100246"
        instr = "Livraison en centre-ville, stationnement limite a 20 min."
    },
    @{
        type = "COMPANY"; name = "TechNord Composants"; legalForm = "SA"; reg = "302147568"; vat = "FR93302147568"
        email = "reception@technord.fr"; phone = "+33320557841"; website = "https://technord.fr"
        terms = 45; lang = "fr"; notes = "Distributeur de composants electroniques, flux tendu."
        label = "Centre de distribution"; addrType = "DEPOT"
        line1 = "5 Rue du Vieux Faubourg"; postal = "59000"; city = "Lille"; lat = 50.6358; lng = 3.0758
        contactName = "Antoine Mercier"; contactPhone = "+33320557842"
        instr = "Reception marchandises de 8h a 12h et 13h30 a 17h."
    },
    @{
        type = "COMPANY"; name = "Boulangerie des Capitouls"; legalForm = "SARL"; reg = "851236974"; vat = "FR47851236974"
        email = "commandes@capitouls.fr"; phone = "+33561223344"; website = "https://capitouls.fr"
        terms = 15; lang = "fr"; notes = "Reseau de boulangeries, livraisons matinales quotidiennes."
        label = "Laboratoire central"; addrType = "DEPOT"
        line1 = "33 Avenue de Lombez"; postal = "31300"; city = "Toulouse"; lat = 43.5972; lng = 1.4188
        contactName = "Julie Fontaine"; contactPhone = "+33561223345"
        instr = "Livraisons avant 6h30, code portail 1947."
    },
    @{
        type = "COMPANY"; name = "Garonne Materiaux"; legalForm = "SAS"; reg = "478963214"; vat = "FR70478963214"
        email = "achats@garonne-materiaux.fr"; phone = "+33556481020"; website = "https://garonne-materiaux.fr"
        terms = 60; lang = "fr"; notes = "Negoce de materiaux de construction, charges lourdes."
        label = "Depot Bordeaux Nord"; addrType = "DEPOT"
        line1 = "210 Avenue de Labarde"; postal = "33300"; city = "Bordeaux"; lat = 44.8788; lng = -0.5544
        contactName = "Philippe Roux"; contactPhone = "+33556481021"
        instr = "Grue auxiliaire recommandee. Acces semi-remorques OK."
    },
    @{
        type = "COMPANY"; name = "Alsace Brasserie Artisanale"; legalForm = "SARL"; reg = "365214789"; vat = "FR21365214789"
        email = "expedition@alsace-brasserie.fr"; phone = "+33388325467"; website = "https://alsace-brasserie.fr"
        terms = 30; lang = "fr"; notes = "Biere artisanale, expeditions de futs et bouteilles."
        label = "Brasserie"; addrType = "DEPOT"
        line1 = "17 Route de la Wantzenau"; postal = "67000"; city = "Strasbourg"; lat = 48.6004; lng = 7.7794
        contactName = "Klaus Weber"; contactPhone = "+33388325468"
        instr = "Reprise des futs vides a chaque livraison."
    },
    @{
        type = "COMPANY"; name = "Riviera Cosmetics"; legalForm = "SAS"; reg = "596314287"; vat = "FR88596314287"
        email = "supply@riviera-cosmetics.fr"; phone = "+33493876510"; website = "https://riviera-cosmetics.fr"
        terms = 45; lang = "en"; notes = "Cosmetics manufacturer, mixed pallet and parcel shipments."
        label = "Production site"; addrType = "DEPOT"
        line1 = "44 Avenue Sainte-Marguerite"; postal = "06200"; city = "Nice"; lat = 43.6890; lng = 7.2155
        contactName = "Laura Bianchi"; contactPhone = "+33493876511"
        instr = "Loading dock on the right, ID required at gate."
    },
    @{
        type = "COMPANY"; name = "Bretagne Mareyage"; legalForm = "SARL"; reg = "417852369"; vat = "FR33417852369"
        email = "logistique@bretagne-mareyage.fr"; phone = "+33299315278"; website = "https://bretagne-mareyage.fr"
        terms = 15; lang = "fr"; notes = "Mareyeur, produits de la mer frais, urgence quotidienne."
        label = "Atelier de maree"; addrType = "DEPOT"
        line1 = "3 Quai des Indes"; postal = "35000"; city = "Rennes"; lat = 48.1009; lng = -1.6794
        contactName = "Yann Le Goff"; contactPhone = "+33299315279"
        instr = "Chaine du froid 0-4 C. Enlevement avant 5h."
    },
    @{
        type = "COMPANY"; name = "Languedoc Pharma Logistics"; legalForm = "SA"; reg = "283649175"; vat = "FR59283649175"
        email = "transport@languedoc-pharma.fr"; phone = "+33467548890"; website = "https://languedoc-pharma.fr"
        terms = 30; lang = "fr"; notes = "Logistique pharmaceutique, traçabilite et temperature controlee."
        label = "Plateforme sante"; addrType = "DEPOT"
        line1 = "190 Rue Leon Blum"; postal = "34000"; city = "Montpellier"; lat = 43.6155; lng = 3.9050
        contactName = "Nadia Benali"; contactPhone = "+33467548891"
        instr = "Releve de temperature obligatoire a la reception."
    },
    @{
        type = "COMPANY"; name = "Alpes Outdoor Equipement"; legalForm = "SAS"; reg = "639185247"; vat = "FR16639185247"
        email = "logistique@alpes-outdoor.fr"; phone = "+33476452310"; website = "https://alpes-outdoor.fr"
        terms = 45; lang = "fr"; notes = "Materiel de montagne et plein air, pics saisonniers."
        label = "Entrepot regional"; addrType = "DEPOT"
        line1 = "12 Rue des Glairaux"; postal = "38120"; city = "Grenoble"; lat = 45.2230; lng = 5.6850
        contactName = "Thomas Faure"; contactPhone = "+33476452311"
        instr = "Hauteur de quai standard, palettes Europe."
    },
    @{
        type = "COMPANY"; name = "Seine Editions"; legalForm = "SARL"; reg = "504217863"; vat = "FR82504217863"
        email = "diffusion@seine-editions.fr"; phone = "+33235071420"; website = "https://seine-editions.fr"
        terms = 60; lang = "fr"; notes = "Maison d'edition, expeditions de livres vers les librairies."
        label = "Magasin de diffusion"; addrType = "DEPOT"
        line1 = "28 Rue de la Republique"; postal = "76000"; city = "Rouen"; lat = 49.4404; lng = 1.0945
        contactName = "Claire Moreau"; contactPhone = "+33235071421"
        instr = "Colis lourds, prevoir transpalette."
    },
    @{
        type = "COMPANY"; name = "Champagne Devaux & Fils"; legalForm = "SAS"; reg = "318527496"; vat = "FR04318527496"
        email = "expeditions@devaux-champagne.fr"; phone = "+33326478135"; website = "https://devaux-champagne.fr"
        terms = 45; lang = "fr"; notes = "Maison de champagne, expeditions fragiles et tracees."
        label = "Caves Devaux"; addrType = "DEPOT"
        line1 = "9 Rue du Champ de Mars"; postal = "51100"; city = "Reims"; lat = 49.2614; lng = 4.0250
        contactName = "Isabelle Devaux"; contactPhone = "+33326478136"
        instr = "Manipulation soignee, cartons 6 bouteilles."
    },
    @{
        type = "COMPANY"; name = "Auvergne Fromages"; legalForm = "SARL"; reg = "462913578"; vat = "FR75462913578"
        email = "ventes@auvergne-fromages.fr"; phone = "+33473194062"; website = "https://auvergne-fromages.fr"
        terms = 30; lang = "fr"; notes = "Affineur de fromages AOP, livraisons refrigerees."
        label = "Cave d'affinage"; addrType = "DEPOT"
        line1 = "54 Avenue de la Republique"; postal = "63000"; city = "Clermont-Ferrand"; lat = 45.7826; lng = 3.1018
        contactName = "Gerard Chaput"; contactPhone = "+33473194063"
        instr = "Temperature 4-8 C, livraison a l'arriere du batiment."
    },
    @{
        type = "COMPANY"; name = "Loire Pieces Auto"; legalForm = "SAS"; reg = "527841693"; vat = "FR61527841693"
        email = "reception@loire-pieces.fr"; phone = "+33247208877"; website = "https://loire-pieces.fr"
        terms = 45; lang = "fr"; notes = "Distributeur de pieces detachees automobiles."
        label = "Centre logistique"; addrType = "DEPOT"
        line1 = "76 Boulevard Abel Gance"; postal = "37700"; city = "Tours"; lat = 47.3645; lng = 0.7110
        contactName = "Kevin Brunet"; contactPhone = "+33247208878"
        instr = "Reception jusqu'a 17h, bordereau a faire signer."
    },
    @{
        type = "COMPANY"; name = "Val de Loire Pepinieres"; legalForm = "EURL"; reg = "613472859"; vat = "FR28613472859"
        email = "expeditions@vdl-pepinieres.fr"; phone = "+33238532914"; website = "https://vdl-pepinieres.fr"
        terms = 30; lang = "fr"; notes = "Pepinieriste, vegetaux fragiles, saison printaniere intense."
        label = "Pepiniere"; addrType = "SHIPPING"
        line1 = "Route de Sandillon"; postal = "45650"; city = "Saint-Jean-le-Blanc"; lat = 47.8862; lng = 1.9421
        contactName = "Martine Lopez"; contactPhone = "+33238532915"
        instr = "Plantes en pot, eviter les chocs, bachage conseille."
    },
    @{
        type = "INDIVIDUAL"; name = "Camille Dubois"; legalForm = $null; reg = $null; vat = $null
        email = "camille.dubois@example.com"; phone = "+33612049587"; website = $null
        terms = 15; lang = "fr"; notes = "Particulier, livraisons ponctuelles de mobilier."
        label = "Domicile"; addrType = "SHIPPING"
        line1 = "21 Rue Oberkampf"; postal = "75011"; city = "Paris"; lat = 48.8654; lng = 2.3715
        contactName = "Camille Dubois"; contactPhone = "+33612049587"
        instr = "Interphone Dubois, 3e etage sans ascenseur."
    },
    @{
        type = "INDIVIDUAL"; name = "Olivier Mercier"; legalForm = $null; reg = $null; vat = $null
        email = "olivier.mercier@example.com"; phone = "+33627583140"; website = $null
        terms = 15; lang = "fr"; notes = "Particulier, electromenager volumineux."
        label = "Domicile"; addrType = "SHIPPING"
        line1 = "8 Rue de la Charite"; postal = "69002"; city = "Lyon"; lat = 45.7556; lng = 4.8320
        contactName = "Olivier Mercier"; contactPhone = "+33627583140"
        instr = "Appeler 30 min avant. Livraison en bas d'immeuble."
    },
    @{
        type = "INDIVIDUAL"; name = "Sarah Lambert"; legalForm = $null; reg = $null; vat = $null
        email = "sarah.lambert@example.com"; phone = "+33634710296"; website = $null
        terms = 15; lang = "en"; notes = "Private customer, occasional parcel deliveries."
        label = "Home"; addrType = "SHIPPING"
        line1 = "15 Rue Esquermoise"; postal = "59800"; city = "Lille"; lat = 50.6398; lng = 3.0610
        contactName = "Sarah Lambert"; contactPhone = "+33634710296"
        instr = "Leave with the concierge if absent."
    }
)

function New-ClientPayload($c, [int]$i) {
    $payload = @{
        type             = $c.type
        name             = $c.name
        email            = $c.email
        phone            = $c.phone
        paymentTermsDays = $c.terms
        notes            = $c.notes
        settings         = @{
            preferredLanguage             = $c.lang
            autoSendInvoiceEmail          = ($c.type -eq "COMPANY")
            autoSendDeliveryNotifications = $true
            autoSendPaymentReminders      = ($c.type -eq "COMPANY")
        }
    }
    if ($c.website) { $payload.website = $c.website }
    if ($c.type -eq "COMPANY") {
        $payload.settings.billingEmail = "billing@" + (($c.email -split "@")[1])
        $payload.legalInfo = @{
            legalName          = $c.name
            registrationNumber = $c.reg
            vatNumber          = $c.vat
            legalForm          = $c.legalForm
        }
    }
    return $payload
}

function New-AddressPayload($c) {
    return @{
        label   = $c.label
        type    = $c.addrType
        address = @{
            line1      = $c.line1
            postalCode = $c.postal
            city       = $c.city
            country    = "FR"
        }
        latitude             = $c.lat
        longitude            = $c.lng
        isPrimary            = $true
        isDefaultShipping    = $true
        isDefaultBilling     = ($c.type -eq "COMPANY")
        contactName          = $c.contactName
        contactPhone         = $c.contactPhone
        contactEmail         = $c.email
        deliveryInstructions = $c.instr
    }
}

Write-Host "Seeding $($clients.Count) realistic clients -> $clientsEndpoint" -ForegroundColor Cyan
$ok = 0
$ko = 0
$firstError = $null

for ($i = 0; $i -lt $clients.Count; $i++) {
    $c = $clients[$i]
    $n = $i + 1
    try {
        $body = (New-ClientPayload $c $n | ConvertTo-Json -Depth 6 -Compress)
        $client = Invoke-RestMethod -Method Post -Uri $clientsEndpoint -Headers $headers `
            -ContentType "application/json; charset=utf-8" -Body $body

        $addrEndpoint = "$clientsEndpoint/$($client.id)/addresses"
        $addrBody = (New-AddressPayload $c | ConvertTo-Json -Depth 6 -Compress)
        $null = Invoke-RestMethod -Method Post -Uri $addrEndpoint -Headers $headers `
            -ContentType "application/json; charset=utf-8" -Body $addrBody

        $ok++
        Write-Host ("  {0,2}/{1}  {2,-32} {3}  ({4})" -f $n, $clients.Count, $c.name, $client.reference, $c.city) -ForegroundColor DarkGray
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
        Write-Host ("  {0,2}/{1}  ERROR (HTTP {2}) {3}" -f $n, $clients.Count, $status, $c.name) -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Done: $ok created, $ko failed." -ForegroundColor $(if ($ko -eq 0) { "Green" } else { "Yellow" })
if ($firstError) { Write-Host "First error: $firstError" -ForegroundColor Yellow }
