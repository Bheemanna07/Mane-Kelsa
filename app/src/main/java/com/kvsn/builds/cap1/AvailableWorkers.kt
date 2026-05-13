package com.kvsn.builds.cap1

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.ArrayList

class AvailableWorkers : AppCompatActivity() {
    private var professionTv: TextView? = null
    private var al: ArrayList<Person> = ArrayList()
    private var md: MyAdaptor? = null
    private var rv: RecyclerView? = null
    private var cityName: String? = null
    private var categoryName: String? = ""
    private var database: DatabaseReference? = null
    private var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // IMMEDIATE STATUS CHECK
            Toast.makeText(this, "Worker List Initializing...", Toast.LENGTH_SHORT).show()
            
            setContentView(R.layout.activity_available_workers)
            title = getString(R.string.available_workers)
            
            pd = ProgressDialog(this)
            pd?.setMessage("Loading workers...")
            pd?.show()
            
            professionTv = findViewById(R.id.ava_profession)
            val sharedPreferences = getSharedPreferences("Categories", Context.MODE_PRIVATE)
            categoryName = sharedPreferences.getString("categorie", "Labour")
            cityName = sharedPreferences.getString("City", "0")
            
            professionTv?.text = categoryName
            
            rv = findViewById(R.id.rec)
            rv?.layoutManager = LinearLayoutManager(this)
            
            al = ArrayList()
            md = MyAdaptor(this@AvailableWorkers, al)
            rv?.adapter = md
            
            database = FirebaseDatabase.getInstance().reference
            val seekerRef = database?.child("Seeker")?.child(categoryName ?: "Labour")
            
            seekerRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    pd?.dismiss()
                    al.clear()
                    
                    if (!dataSnapshot.exists()) {
                        Toast.makeText(this@AvailableWorkers, "No $categoryName workers found.", Toast.LENGTH_LONG).show()
                        return
                    }
                    
                    for (dataloop in dataSnapshot.children) {
                        try {
                            val isAvailable = dataloop.child("isAvailable").value?.toString()?.equals("true", ignoreCase = true) ?: false
                            if (!isAvailable) continue
                            
                            val workerCity = dataloop.child("city").value?.toString() ?: dataloop.child("City").value?.toString() ?: ""
                            
                            // Flexible city filtering
                            if (cityName != "0" && !workerCity.contains(cityName!!, ignoreCase = true) && !cityName!!.contains(workerCity, ignoreCase = true)) {
                                continue
                            }
                            
                            val p = Person()
                            p.Name = dataloop.child("Name").value?.toString() ?: dataloop.child("name").value?.toString() ?: "Worker"
                            p.id = dataloop.child("id").value?.toString() ?: dataloop.child("Id").value?.toString() ?: ""
                            p.Rating = dataloop.child("Rating").value?.toString() ?: dataloop.child("rating").value?.toString() ?: "New"
                            p.url = dataloop.child("urlToImage").value?.toString() ?: dataloop.child("url").value?.toString() ?: null
                            
                            al.add(p)
                        } catch (e: Exception) {
                            Log.e("AvailableWorkers", "Loop Error: ${e.message}")
                        }
                    }
                    
                    if (al.isEmpty()) {
                        Toast.makeText(this@AvailableWorkers, "No available workers in $cityName", Toast.LENGTH_SHORT).show()
                    }
                    md?.notifyDataSetChanged()
                }
                
                override fun onCancelled(databaseError: DatabaseError) {
                    pd?.dismiss()
                    Toast.makeText(this@AvailableWorkers, "Error: ${databaseError.message}", Toast.LENGTH_LONG).show()
                }
            })
            
        } catch (e: Exception) {
            Toast.makeText(this, "Startup Crash Prevented: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("AvailableWorkers", "onCreate Error: ${e.message}")
        }
    }

    // Removed custom onBackPressed to allow default back navigation to GetLocation
}
