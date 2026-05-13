$mappings = @{
    "import android.support.annotation.NonNull;" = "import androidx.annotation.NonNull;"
    "import android.support.v7.app.AppCompatActivity;" = "import androidx.appcompat.app.AppCompatActivity;"
    "import android.support.v7.app.AlertDialog;" = "import androidx.appcompat.app.AlertDialog;"
    "import android.support.v4.app.ActivityCompat;" = "import androidx.core.app.ActivityCompat;"
    "import android.support.v4.app.FragmentActivity;" = "import androidx.fragment.app.FragmentActivity;"
    "import android.support.v4.app.ActivityOptionsCompat;" = "import androidx.core.app.ActivityOptionsCompat;"
    "import android.support.v4.view.ViewCompat;" = "import androidx.core.view.ViewCompat;"
    "import android.support.v7.widget.Toolbar;" = "import androidx.appcompat.widget.Toolbar;"
    "import android.support.v7.widget.CardView;" = "import androidx.cardview.widget.CardView;"
    "import android.support.v7.widget.RecyclerView;" = "import androidx.recyclerview.widget.RecyclerView;"
    "import android.support.v7.widget.LinearLayoutManager;" = "import androidx.recyclerview.widget.LinearLayoutManager;"
}

$files = Get-ChildItem -Path "app\src\main\java" -Filter "*.java" -Recurse

foreach ($file in $files) {
    $content = Get-Content $file.FullName
    $changed = $false
    foreach ($old in $mappings.Keys) {
        if ($content -contains $old) {
            $content = $content -replace [regex]::Escape($old), $mappings[$old]
            $changed = $true
        }
    }
    if ($changed) {
        $content | Set-Content $file.FullName
        Write-Host "Updated $($file.FullName)"
    }
}
