$files = Get-ChildItem -Path "app\src\main\java" -Filter "*.java" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $changed = $false

    # Fix CropImage constants
    if ($content -match "CropImage\.CROP_IMAGE_ACTIVITY_REQUEST_CODE") {
        $content = $content -replace "CropImage\.CROP_IMAGE_ACTIVITY_REQUEST_CODE", "203"
        $changed = $true
    }
    if ($content -match "CropImage\.CROP_IMAGE_RESULT_ERROR_CODE") {
        $content = $content -replace "CropImage\.CROP_IMAGE_RESULT_ERROR_CODE", "204"
        $changed = $true
    }

    # Fix switch statements with resource IDs in Signup.java
    if ($file.Name -eq "Signup.java") {
        # This is a bit complex for a simple regex, but let's try to fix the specific switch reported
        # switch(view.getId()) { case R.id.Radio_btn_seeker: ... }
        
        $newContent = $content -replace 'switch\s*\((view|v)\.getId\(\)\)\s*\{', 'int id = $1.getId(); if (false) {}'
        $newContent = $newContent -replace 'case\s+(R\.id\.\w+):', 'else if (id == $1)'
        $newContent = $newContent -replace 'break;', ''
        
        if ($newContent -ne $content) {
            $content = $newContent
            $changed = $true
        }
    }

    if ($changed) {
        $content | Set-Content $file.FullName
        Write-Host "Patched $($file.FullName)"
    }
}
