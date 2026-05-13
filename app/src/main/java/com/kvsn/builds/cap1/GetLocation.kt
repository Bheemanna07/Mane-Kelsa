package com.kvsn.builds.cap1

import android.Manifest
import android.app.ProgressDialog
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.util.*

class GetLocation : AppCompatActivity() {
    private var currLocation: CardView? = null
    private var categoryBadgeTv: TextView? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var Scity: String? = null
    private var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_location)
        
        currLocation = findViewById(R.id.curr_Location)
        categoryBadgeTv = findViewById(R.id.category_badge_text)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("Categories", Context.MODE_PRIVATE)
        editor = sharedPreferences?.edit()
        
        val category = sharedPreferences?.getString("categorie", "Labour")
        categoryBadgeTv?.text = category
        
        pd = ProgressDialog(this)
    }

    fun onOtherLocationClick(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter City")
        val input = EditText(this)
        input.hint = "e.g. Bangalore"
        builder.setView(input)
        
        builder.setPositiveButton("OK") { dialog, _ ->
            val city = input.text.toString().trim()
            if (city.isNotEmpty()) {
                editor?.putString("City", city)
                editor?.putString("Address", city)
                editor?.apply()
                
                Toast.makeText(this, "Opening workers for $city...", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, AvailableWorkers::class.java)
                startActivity(intent)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // Renamed to avoid any potential conflict with parent activity
    fun onCurrentLocationClick(v: View) {
        // Disable button to prevent multiple clicks
        currLocation?.isEnabled = false
        getlocation()
    }

    private fun getlocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            pd?.setTitle("Fetching Location...")
            pd?.setMessage("Please wait...")
            pd?.setCanceledOnTouchOutside(true)
            pd?.show()
            
            fusedLocationProviderClient?.lastLocation?.addOnCompleteListener { task ->
                pd?.dismiss()
                currLocation?.isEnabled = true // Re-enable button
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    val geocoder = Geocoder(this@GetLocation, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            Scity = addr.subAdminArea ?: addr.featureName ?: addr.locality ?: ""
                            
                            editor?.putString("City", Scity)
                            editor?.putString("Address", addr.getAddressLine(0))
                            editor?.apply()
                            
                            Toast.makeText(this, "Location Found: $Scity. Opening workers...", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, AvailableWorkers::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Could not determine address.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error finding city: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "GPS Failed. Use 'Other Location' button.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getlocation()
            } else {
                currLocation?.isEnabled = true
                Toast.makeText(this, "Permission Denied. Cannot fetch location.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
