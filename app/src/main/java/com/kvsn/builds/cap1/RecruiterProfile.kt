package com.kvsn.builds.cap1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class RecruiterProfile : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var msubref: DatabaseReference? = null
    private var image: CircleImageView? = null
    private var nameTv: TextView? = null
    private var mailTv: TextView? = null
    private var aadhaarTv: TextView? = null
    private var mobileTv: TextView? = null
    private var stateTv: TextView? = null
    private var cityTv: TextView? = null
    private var addressTv: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_recruiter_profile)
            title = "Profile"
            CheckInternetConnection(this).checkConnection()

            nameTv = findViewById(R.id.name_recruiter)
            mailTv = findViewById(R.id.mail_recruiter)
            addressTv = findViewById(R.id.recruiter_address)
            aadhaarTv = findViewById(R.id.recruiter_aadhaar)
            cityTv = findViewById(R.id.recruiter_city)
            mobileTv = findViewById(R.id.recruiter_mobile)
            stateTv = findViewById(R.id.recruiter_state)
            image = findViewById(R.id.profile_image)

            mDatabase = FirebaseDatabase.getInstance("https://self-employment-app-default-rtdb.asia-southeast1.firebasedatabase.app").reference
            mAuth = FirebaseAuth.getInstance()

            retrieve()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun retrieve() {
        val user = mAuth?.currentUser ?: return
        msubref = mDatabase?.child("Users")?.child(user.uid)
        msubref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val nameVal = if (dataSnapshot.hasChild("name")) dataSnapshot.child("name").value.toString()
                                  else if (dataSnapshot.hasChild("Name")) dataSnapshot.child("Name").value.toString()
                                  else "Not Set"
                    nameTv?.text = nameVal

                    val mailVal = if (dataSnapshot.hasChild("email")) dataSnapshot.child("email").value.toString()
                                  else if (dataSnapshot.hasChild("Email")) dataSnapshot.child("Email").value.toString()
                                  else ""
                    mailTv?.text = mailVal

                    val addrVal = if (dataSnapshot.hasChild("street_No")) dataSnapshot.child("street_No").value.toString()
                                  else if (dataSnapshot.hasChild("Street_No")) dataSnapshot.child("Street_No").value.toString()
                                  else "Not Set"
                    addressTv?.text = addrVal

                    val aadharVal = if (dataSnapshot.hasChild("aadhar_Number")) dataSnapshot.child("aadhar_Number").value.toString()
                                    else if (dataSnapshot.hasChild("Aadhar_Number")) dataSnapshot.child("Aadhar_Number").value.toString()
                                    else "Not Set"
                    aadhaarTv?.text = aadharVal

                    val cityVal = if (dataSnapshot.hasChild("city")) dataSnapshot.child("city").value.toString()
                                  else if (dataSnapshot.hasChild("City")) dataSnapshot.child("City").value.toString()
                                  else "Not Set"
                    cityTv?.text = cityVal

                    val mobVal = if (dataSnapshot.hasChild("contact_Number")) dataSnapshot.child("contact_Number").value.toString()
                                 else if (dataSnapshot.hasChild("Contact_Number")) dataSnapshot.child("Contact_Number").value.toString()
                                 else ""
                    mobileTv?.text = mobVal

                    val stateVal = if (dataSnapshot.hasChild("state")) dataSnapshot.child("state").value.toString()
                                   else if (dataSnapshot.hasChild("State")) dataSnapshot.child("State").value.toString()
                                   else "Not Set"
                    stateTv?.text = stateVal

                    if (dataSnapshot.hasChild("urlToImage")) {
                        Picasso.get().load(dataSnapshot.child("urlToImage").getValue(String::class.java)).into(image)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onBackPressed() {
        startActivity(Intent(this@RecruiterProfile, RecruiterMain::class.java))
        finish()
    }

    fun image(v: View) {
        startActivity(Intent(this@RecruiterProfile, PhotoActivity::class.java))
    }

    fun edit_recruiter(v: View) {
        startActivity(Intent(this@RecruiterProfile, EditRecruiter::class.java))
    }
}
