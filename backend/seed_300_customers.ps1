# Seed 300 Demo Customers with Complete Form Details and Realistic Fake Images

Add-Type -AssemblyName System.Drawing

Write-Host "=== 1. Generating 40 Diverse Fake Avatar Images & KYC Placeholders ===" -ForegroundColor Cyan
$uploadsDir = "backend/data/uploads/customers"
if (!(Test-Path $uploadsDir)) {
    New-Item -ItemType Directory -Path $uploadsDir -Force | Out-Null
}

$palette = @(
    @([System.Drawing.Color]::FromArgb(234, 88, 12), [System.Drawing.Color]::FromArgb(254, 215, 170)),  # Orange
    @([System.Drawing.Color]::FromArgb(37, 99, 235), [System.Drawing.Color]::FromArgb(191, 219, 254)),  # Blue
    @([System.Drawing.Color]::FromArgb(16, 185, 129), [System.Drawing.Color]::FromArgb(167, 243, 208)), # Emerald
    @([System.Drawing.Color]::FromArgb(217, 119, 6), [System.Drawing.Color]::FromArgb(254, 240, 138)),  # Amber/Gold
    @([System.Drawing.Color]::FromArgb(147, 51, 234), [System.Drawing.Color]::FromArgb(233, 213, 255)), # Purple
    @([System.Drawing.Color]::FromArgb(225, 29, 72), [System.Drawing.Color]::FromArgb(254, 205, 211)),  # Rose
    @([System.Drawing.Color]::FromArgb(13, 148, 136), [System.Drawing.Color]::FromArgb(153, 246, 228)), # Teal
    @([System.Drawing.Color]::FromArgb(79, 70, 229), [System.Drawing.Color]::FromArgb(199, 210, 254)),  # Indigo
    @([System.Drawing.Color]::FromArgb(202, 138, 4), [System.Drawing.Color]::FromArgb(254, 240, 138)),  # Yellow-Gold
    @([System.Drawing.Color]::FromArgb(220, 38, 38), [System.Drawing.Color]::FromArgb(254, 202, 202))   # Crimson
)

# Generate 40 unique avatars
for ($imgIdx = 1; $imgIdx -le 40; $imgIdx++) {
    $pair = $palette[($imgIdx - 1) % $palette.Count]
    $bgColor = $pair[0]
    $fgColor = $pair[1]

    $bmp = New-Object System.Drawing.Bitmap 140, 140
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::Transparent)

    # Outer circle
    $bgBrush = New-Object System.Drawing.SolidBrush $bgColor
    $g.FillEllipse($bgBrush, 2, 2, 136, 136)

    # Border ring
    $pen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(255, 255, 255, 255)), 4
    $g.DrawEllipse($pen, 5, 5, 130, 130)

    # Stylized person silhouette: Head circle + Body arc
    $silBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
    $g.FillEllipse($silBrush, 45, 28, 50, 50)
    $g.FillPie($silBrush, 22, 80, 96, 96, 200, 140)

    # Initials badge on bottom-right
    $badgeBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(24, 24, 27))
    $g.FillEllipse($badgeBrush, 90, 90, 44, 44)
    $badgePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(240, 138, 24)), 2.5
    $g.DrawEllipse($badgePen, 90, 90, 44, 44)

    $numStr = ($imgIdx).ToString()
    $font = New-Object System.Drawing.Font("Arial", 14, [System.Drawing.FontStyle]::Bold)
    $fontBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(254, 240, 138))
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rect = New-Object System.Drawing.RectangleF 90, 90, 44, 44
    $g.DrawString($numStr, $font, $fontBrush, $rect, $sf)

    $targetFile = "$uploadsDir/avatar_$imgIdx.png"
    $bmp.Save($targetFile, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
}

# Generate sample Aadhaar Front Card image
$aadhaarFrontBmp = New-Object System.Drawing.Bitmap 340, 210
$gAf = [System.Drawing.Graphics]::FromImage($aadhaarFrontBmp)
$gAf.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$gAf.Clear([System.Drawing.Color]::White)
# Tri-color top stripe
$gAf.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 153, 51))), 0, 0, 340, 16)
$gAf.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(19, 136, 8))), 0, 194, 340, 16)
$gAf.DrawRectangle((New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(200, 200, 200)), 2), 1, 1, 338, 208)
# Photo box
$gAf.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(230, 235, 245))), 20, 36, 70, 85)
$gAf.DrawRectangle((New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(120, 140, 180)), 1), 20, 36, 70, 85)
# Text lines
$fH = New-Object System.Drawing.Font("Arial", 11, [System.Drawing.FontStyle]::Bold)
$fT = New-Object System.Drawing.Font("Arial", 9, [System.Drawing.FontStyle]::Regular)
$bDark = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(30, 41, 59))
$bRed = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(185, 28, 28))
$gAf.DrawString("GOVERNMENT OF INDIA", $fH, $bRed, 100, 28)
$gAf.DrawString("UNIQUE IDENTIFICATION AUTHORITY", $fT, $bDark, 100, 48)
$gAf.DrawString("Name: VERIFIED CUSTOMER", $fT, $bDark, 100, 75)
$gAf.DrawString("DOB: 15/08/1988 | Gender: M", $fT, $bDark, 100, 95)
# 12-digit number banner
$fNum = New-Object System.Drawing.Font("Arial", 14, [System.Drawing.FontStyle]::Bold)
$gAf.DrawString("XXXX  XXXX  1098", $fNum, $bRed, 80, 145)
$aadhaarFrontBmp.Save("$uploadsDir/aadhaar_front_sample.png", [System.Drawing.Imaging.ImageFormat]::Png)
$gAf.Dispose()
$aadhaarFrontBmp.Dispose()

# Generate sample Aadhaar Back Card image
$aadhaarBackBmp = New-Object System.Drawing.Bitmap 340, 210
$gAb = [System.Drawing.Graphics]::FromImage($aadhaarBackBmp)
$gAb.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$gAb.Clear([System.Drawing.Color]::White)
$gAb.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 153, 51))), 0, 0, 340, 16)
$gAb.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(19, 136, 8))), 0, 194, 340, 16)
$gAb.DrawRectangle((New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(200, 200, 200)), 2), 1, 1, 338, 208)
$gAb.DrawString("Address / Address Details:", $fH, $bDark, 20, 30)
$gAb.DrawString("S/o Rameshwar Sharma, House No. 45/B", $fT, $bDark, 20, 55)
$gAb.DrawString("Bowbazar Street, Bowbazar Market Area", $fT, $bDark, 20, 75)
$gAb.DrawString("Kolkata, West Bengal - 700012", $fT, $bDark, 20, 95)
# QR code box placeholder
$gAb.FillRectangle((New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(240, 240, 240))), 230, 40, 80, 80)
$gAb.DrawRectangle((New-Object System.Drawing.Pen ([System.Drawing.Color]::Black), 1), 230, 40, 80, 80)
$gAb.DrawString("QR VERIFIED", (New-Object System.Drawing.Font("Arial", 8, [System.Drawing.FontStyle]::Bold)), $bDark, 235, 75)
$gAb.DrawString("Help: 1947 | uidai.gov.in", $fT, $bDark, 90, 160)
$aadhaarBackBmp.Save("$uploadsDir/aadhaar_back_sample.png", [System.Drawing.Imaging.ImageFormat]::Png)
$gAb.Dispose()
$aadhaarBackBmp.Dispose()

Write-Host "✓ 40 Avatars and 2 Aadhaar KYC card images generated in $uploadsDir" -ForegroundColor Green

Write-Host "`n=== 2. Authenticating with Backend API ===" -ForegroundColor Cyan
$loginPayload = @{ username = "admin"; password = "Admin@123" } | ConvertTo-Json
$auth = Invoke-RestMethod -Uri 'http://localhost:18080/api/v1/auth/login' -Method Post -Body $loginPayload -ContentType 'application/json'
$headers = @{ Authorization = "Bearer $($auth.accessToken)" }
Write-Host "✓ Logged in as: $($auth.user.displayName)" -ForegroundColor Green

# Clean up any leftover TEST customers from benchmark
$testCusts = Invoke-RestMethod -Uri 'http://localhost:18080/api/v1/masters/customers?q=TEST' -Headers $headers
foreach ($tc in $testCusts) {
    if ($tc.code -like "TEST*") {
        try {
            $null = Invoke-RestMethod -Uri "http://localhost:18080/api/v1/masters/customers/$($tc.id)" -Method Delete -Headers $headers
        } catch {}
    }
}

Write-Host "`n=== 3. Seeding 300 Comprehensive Demo Customers ===" -ForegroundColor Cyan

# Indian names repository
$firstNamesM = @(
    "Aarav", "Rohan", "Rajesh", "Suresh", "Vikram", "Aditya", "Kunal", "Amit", "Alok", "Debabrata",
    "Sourav", "Pradeep", "Vishal", "Gaurav", "Anand", "Rahul", "Deepak", "Manish", "Sanjay", "Sachin",
    "Rakesh", "Vivek", "Ajay", "Vijay", "Sandeep", "Hemant", "Pankaj", "Ashish", "Arpan", "Indranil",
    "Brijesh", "Chirag", "Darshan", "Mayank", "Nilesh", "Parth", "Tarun", "Yash", "Devendra", "Gopal",
    "Harish", "Jitendra", "Kishore", "Lalit", "Mahesh", "Naveen", "Omkar", "Prakash", "Raman", "Shyam"
)

$firstNamesF = @(
    "Aditi", "Priya", "Ananya", "Sneha", "Pooja", "Meera", "Shreya", "Ritu", "Payel", "Priyanka",
    "Tanvi", "Riya", "Swati", "Kavita", "Sunita", "Rekha", "Shilpa", "Rashmi", "Deepa", "Neha",
    "Rupa", "Sharmila", "Debolina", "Mithu", "Sudipta", "Pallavi", "Archana", "Madhuri", "Divya", "Ishita",
    "Juhi", "Kriti", "Lavanya", "Nandini", "Parul", "Richa", "Sakshi", "Tanya", "Urvi", "Vaishali",
    "Asha", "Bhavna", "Chitra", "Damini", "Geeta", "Jyoti", "Kiran", "Lata", "Manju", "Nisha"
)

$lastNames = @(
    "Sharma", "Verma", "Gupta", "Patel", "Mehta", "Shah", "Ghosh", "Mukherjee", "Banerjee", "Chatterjee",
    "Das", "Paul", "Singha", "Sen", "Roy", "Dutta", "Chakraborty", "Dey", "Mondal", "Biswas",
    "Sarkar", "Saha", "Nandi", "Bhowmick", "Joshi", "Kulkarni", "Deshmukh", "Patil", "Pawar", "Shinde",
    "Jadhav", "Reddy", "Rao", "Nair", "Pillai", "Iyer", "Agarwal", "Mittal", "Bansal", "Jain",
    "Goyal", "Singhal", "Chauhan", "Yadav", "Trivedi", "Pandey", "Mishra", "Tripathi", "Shukla", "Bhat"
)

$cities = @(
    @{ City = "Kolkata"; State = "West Bengal"; Pin = "700012"; Area = "Bowbazar Jewellery Market" },
    @{ City = "Howrah"; State = "West Bengal"; Pin = "711101"; Area = "Howrah Maidan" },
    @{ City = "Burdwan"; State = "West Bengal"; Pin = "713101"; Area = "Curzon Gate Area" },
    @{ City = "Hooghly"; State = "West Bengal"; Pin = "712101"; Area = "Chinsurah Gold Mohalla" },
    @{ City = "Mumbai"; State = "Maharashtra"; Pin = "400002"; Area = "Zaveri Bazaar" },
    @{ City = "Pune"; State = "Maharashtra"; Pin = "411002"; Area = "Raviwar Peth" },
    @{ City = "Surat"; State = "Gujarat"; Pin = "395003"; Area = "Varachha Diamond Market" },
    @{ City = "Ahmedabad"; State = "Gujarat"; Pin = "380001"; Area = "Manek Chowk Gold Bazaar" },
    @{ City = "Jaipur"; State = "Rajasthan"; Pin = "302003"; Area = "Johari Bazaar" },
    @{ City = "Delhi"; State = "Delhi"; Pin = "110006"; Area = "Dariba Kalan, Chandni Chowk" },
    @{ City = "Bangalore"; State = "Karnataka"; Pin = "560002"; Area = "Chickpet Jewellery Hub" },
    @{ City = "Hyderabad"; State = "Telangana"; Pin = "500002"; Area = "Pot Market, Secunderabad" },
    @{ City = "Lucknow"; State = "Uttar Pradesh"; Pin = "226003"; Area = "Chowk Saraf Bazaar" },
    @{ City = "Patna"; State = "Bihar"; Pin = "800004"; Area = "Bakerganj Gold Market" }
)

$banks = @(
    @{ Name = "State Bank of India"; IFSC = "SBIN0001234" },
    @{ Name = "HDFC Bank"; IFSC = "HDFC0004567" },
    @{ Name = "ICICI Bank"; IFSC = "ICIC0007890" },
    @{ Name = "Punjab National Bank"; IFSC = "PUNB0002345" },
    @{ Name = "Axis Bank"; IFSC = "UTIB0006789" },
    @{ Name = "Bank of Baroda"; IFSC = "BARB0003456" },
    @{ Name = "Kotak Mahindra Bank"; IFSC = "KKBK0008901" }
)

$occupations = @(
    "Jewellery Merchant", "Bullion Dealer", "Doctor / Surgeon", "Software Architect",
    "Chartered Accountant", "Civil Contractor", "High School Principal", "Senior Advocate",
    "Real Estate Developer", "Professor", "Homemaker", "Industrialist", "Wholesale Cloth Merchant",
    "Government Officer (Class I)", "Hotelier & Restaurateur", "Diamond Polishing Unit Owner"
)

$annualIncomes = @("₹5 - 10 Lakhs", "₹10 - 25 Lakhs", "₹25 - 50 Lakhs", "₹50+ Lakhs", "₹3 - 5 Lakhs")
$castes = @("Baniya", "Kshatriya", "Brahmin", "Kayastha", "Vaishya", "Marwari", "General", "OBC")
$qualifications = @("Graduate (B.Com)", "Post Graduate (M.B.A)", "B.Tech / B.E.", "M.B.B.S / M.D.", "Chartered Accountant (FCA)", "Higher Secondary")

$otherNotes = @(
    "VIP Wedding Client - Prefers 22K/916 BIS Hallmarked bridal jewellery sets.",
    "Diamond & Polki enthusiast - regular festive buyer for Dhanteras and Diwali.",
    "Frequent 24K gold bar and silver bullion investor.",
    "Traditional family customer since 2012 with prompt and verified payment history.",
    "Wholesale silver ornaments & utensils trader - receives GST invoices.",
    "Prefers instant WhatsApp catalog alerts for new temple jewellery designs.",
    "Purchases customized uncut diamond jadau sets on advance order.",
    "High-net-worth customer - authorized for premium showroom lounge booking."
)

$totalToSeed = 300
$createdCount = 0
$startTime = [System.Diagnostics.Stopwatch]::StartNew()

for ($i = 1; $i -le $totalToSeed; $i++) {
    $codeNum = 100 + $i
    $code = "C" + $codeNum.ToString("0000")

    $isFemale = ($i % 2 -eq 0)
    $first = if ($isFemale) { $firstNamesF[($i * 7) % $firstNamesF.Count] } else { $firstNamesM[($i * 11) % $firstNamesM.Count] }
    $last = $lastNames[($i * 13) % $lastNames.Count]
    $fullName = "$first $last"

    $gender = if ($isFemale) { "F" } else { "M" }
    $title = if ($isFemale) { if ($i % 3 -eq 0) { "Mrs." } else { "Ms." } } else { if ($i % 15 -eq 0) { "Dr." } else { "Mr." } }

    $loc = $cities[$i % $cities.Count]
    $b = $banks[$i % $banks.Count]
    $accNo = "9180" + (10000000 + $i * 97).ToString()

    $panLetters = "ABCDE"[$i % 5] + "P" + "QRSTU"[($i * 3) % 5] + $first[0] + $last[0]
    $pan = ($panLetters + (1000 + $i).ToString() + "F").ToUpper()
    $gstin = if ($i % 3 -eq 0) { "19$pan" + "1Z" + ($i % 9).ToString() } else { $null }
    $aadhaar = (4000 + $i * 3).ToString() + " " + (5000 + $i * 7).ToString() + " " + (1000 + $i * 11).ToString()

    $birthYear = 1970 + ($i % 32)
    $birthMonth = (1 + ($i % 12)).ToString("00")
    $birthDay = (1 + ($i % 28)).ToString("00")
    $dob = "$birthYear-$birthMonth-$birthDay"

    $annivYear = $birthYear + 24
    $anniv = if ($title -eq "Mrs." -or ($gender -eq "M" -and $i % 2 -eq 0)) { "$annivYear-11-25" } else { $null }

    $relType = if ($gender -eq "M") { "S/o" } else { if ($title -eq "Mrs.") { "W/o" } else { "D/o" } }
    $father = $firstNamesM[($i + 5) % $firstNamesM.Count] + " " + $last
    $mother = $firstNamesF[($i + 9) % $firstNamesF.Count] + " " + $last
    $spouse = if ($relType -eq "W/o") { $firstNamesM[($i + 3) % $firstNamesM.Count] + " " + $last } else { if ($gender -eq "M" -and $anniv) { $firstNamesF[($i + 3) % $firstNamesF.Count] + " " + $last } else { $null } }

    $phone = "98" + (30000000 + $i * 739).ToString().Substring(0, 8)
    $email = "$($first.ToLower()).$($last.ToLower()).$codeNum@gmail.com"

    $avatarIdx = (($i - 1) % 40) + 1
    $photoUrl = "/api/v1/uploads/customers/avatar_$avatarIdx.png"

    $partyType = if ($i % 4 -eq 0) { "Wholesale Customer" } else { "Retail Customer" }
    $accGroup = if ($i % 10 -eq 0) { "Sundry Creditors" } else { "Sundry Debtors" }

    $customerPayload = @{
        code = $code
        name = $fullName
        firstName = $first
        lastName = $last
        partyType = $partyType
        title = $title
        gender = $gender
        relationType = $relType
        fatherName = $father
        motherName = $mother
        spouseName = $spouse
        caste = $castes[$i % $castes.Count]
        qualification = $qualifications[$i % $qualifications.Count]
        maritalStatus = if ($anniv) { "Married" } else { "Single" }
        dob = $dob
        spouseDob = if ($spouse) { "$($birthYear + 2)-06-15" } else { $null }
        anniversaryDate = $anniv
        hos = "HOS"
        accountGroup = $accGroup
        panNo = $pan
        gstin = $gstin
        aadhaarNo = $aadhaar
        bankName = $b.Name
        bankAccName = $fullName
        bankAccNo = $accNo
        bankIfscCode = $b.IFSC
        occupation = $occupations[$i % $occupations.Count]
        annualIncome = $annualIncomes[$i % $annualIncomes.Count]
        nomineeName = if ($spouse) { $spouse } else { $father }
        nomineeRelation = if ($spouse) { "Spouse" } else { "Father" }
        designation = if ($partyType -eq "Wholesale Customer") { "Proprietor" } else { "Client / Buyer" }
        loginId = "USR_$code"
        portalPassword = "Pass@$codeNum"
        masterPassword = "Mst@$codeNum"
        businessName = if ($partyType -eq "Wholesale Customer") { "$last Jewellers & Bullion" } else { $null }
        officeAddress = "$($loc.Area), Suite $($i + 10), $($loc.City)"
        permanentAddress = "House No. $($i * 4), Main Road, $($loc.Area), $($loc.City)"
        currentAddress = "Flat No. $($i * 2 + 101), Royal Heights, $($loc.Area), $($loc.City)"
        shippingAddress = "Flat No. $($i * 2 + 101), Royal Heights, $($loc.Area), $($loc.City)"
        village = $loc.Area
        wardNumber = "Ward " + (($i % 45) + 1).ToString()
        tehsil = "$($loc.City) Sadar"
        city = $loc.City
        countryName = "India"
        pincode = $loc.Pin
        stateName = $loc.State
        otherInfo = $otherNotes[$i % $otherNotes.Count]
        phone = $phone
        email = $email
        address = "$($loc.Area), $($loc.City)"
        photoBase64 = $photoUrl
        aadhaarFrontBase64 = "/api/v1/uploads/customers/aadhaar_front_sample.png"
        aadhaarFrontDesc = "Aadhaar Card Front (UIDAI Verified)"
        aadhaarBackBase64 = "/api/v1/uploads/customers/aadhaar_back_sample.png"
        aadhaarBackDesc = "Aadhaar Card Back (Address Verified)"
        sendWelcomeSms = $true
    } | ConvertTo-Json

    try {
        $res = Invoke-RestMethod -Uri 'http://localhost:18080/api/v1/masters/customers' -Method Post -Body $customerPayload -ContentType 'application/json' -Headers $headers
        $createdCount++
        if ($createdCount % 50 -eq 0 -or $createdCount -eq $totalToSeed) {
            Write-Host " -> Progress: $createdCount / $totalToSeed customers created (Latest: $($res.name) - $($res.code))" -ForegroundColor Yellow
        }
    } catch {`
        Write-Host " ⚠ Error creating customer ${code}: $($_.Exception.Message)" -ForegroundColor Red
    }
}

$startTime.Stop()
Write-Host "`n========================================================" -ForegroundColor Green
Write-Host "✓ SUCCESSFULLY SEEDED $createdCount DEMO CUSTOMERS IN $($startTime.Elapsed.TotalSeconds.ToString('F1'))s" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green
