package com.kvsn.builds.cap1

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class EditRecruiter : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var msubref: DatabaseReference? = null
    private var nameEt: EditText? = null
    private var mobileEt: EditText? = null
    private var stateEt: EditText? = null
    private var cityEt: EditText? = null
    private var addressEt: EditText? = null
    private var mailTv: TextView? = null
    private var aadhaarTv: TextView? = null
    private var profileIv: CircleImageView? = null
    private var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recruiter)

        title = "Edit Profile"
        CheckInternetConnection(this).checkConnection()

        nameEt = findViewById(R.id.name_recruiter_edit)
        mailTv = findViewById(R.id.mail_recruiter_edit)
        addressEt = findViewById(R.id.recruiter_address_edit)
        aadhaarTv = findViewById(R.id.recruiter_aadhaar_edit)
        cityEt = findViewById(R.id.recruiter_city_edit)
        mobileEt = findViewById(R.id.recruiter_mobile_edit)
        stateEt = findViewById(R.id.recruiter_state_edit)
        profileIv = findViewById(R.id.profile_image_edit)
        pd = ProgressDialog(this)

        mDatabase = FirebaseDatabase.getInstance("https://self-employment-app-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        mAuth = FirebaseAuth.getInstance()

        profileIv?.setOnClickListener {
            startActivity(Intent(this@EditRecruiter, PhotoUpload::class.java))
        }

        retrieve()
    }

    fun retrieve() {
        val user = mAuth?.currentUser ?: return
        msubref = mDatabase?.child("Users")?.child(user.uid)
        msubref?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) return
                
                try {
                    val nameVal = if (dataSnapshot.hasChild("name")) dataSnapshot.child("name").value.toString()
                                  else if (dataSnapshot.hasChild("Name")) dataSnapshot.child("Name").value.toString()
                                  else ""
                    nameEt?.setText(nameVal)

                    val mailVal = if (dataSnapshot.hasChild("email")) dataSnapshot.child("email").value.toString()
                                  else if (dataSnapshot.hasChild("Email")) dataSnapshot.child("Email").value.toString()
                                  else ""
                    mailTv?.text = mailVal

                    val addrVal = if (dataSnapshot.hasChild("street_No")) dataSnapshot.child("street_No").value.toString()
                                  else if (dataSnapshot.hasChild("Street_No")) dataSnapshot.child("Street_No").value.toString()
                                  else if (dataSnapshot.hasChild("Address")) dataSnapshot.child("Address").value.toString()
                                  else ""
                    addressEt?.setText(addrVal)

                    val aadharVal = if (dataSnapshot.hasChild("aadhar_Number")) dataSnapshot.child("aadhar_Number").value.toString()
                                    else if (dataSnapshot.hasChild("Aadhar_Number")) dataSnapshot.child("Aadhar_Number").value.toString()
                                    else ""
                    aadhaarTv?.text = aadharVal

                    val cityVal = if (dataSnapshot.hasChild("city")) dataSnapshot.child("city").value.toString()
                                  else if (dataSnapshot.hasChild("City")) dataSnapshot.child("City").value.toString()
                                  else ""
                    cityEt?.setText(cityVal)

                    val mobVal = if (dataSnapshot.hasChild("contact_Number")) dataSnapshot.child("contact_Number").value.toString()
                                 else if (dataSnapshot.hasChild("Contact_Number")) dataSnapshot.child("Contact_Number").value.toString()
                                 else if (dataSnapshot.hasChild("Mobile")) dataSnapshot.child("Mobile").value.toString()
                                 else ""
                    mobileEt?.setText(mobVal)

                    val stateVal = if (dataSnapshot.hasChild("state")) dataSnapshot.child("state").value.toString()
                                   else if (dataSnapshot.hasChild("State")) dataSnapshot.child("State").value.toString()
                                   else ""
                    stateEt?.setText(stateVal)

                    if (dataSnapshot.hasChild("urlToImage")) {
                        Picasso.get().load(dataSnapshot.child("urlToImage").getValue(String::class.java)).into(profileIv)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun updatedata(v: View) {
        pd?.setTitle("Updating Details...")
        pd?.show()
        
        val user = mAuth?.currentUser ?: return
        val ref = mDatabase?.child("Users")?.child(user.uid) ?: return

        val n = nameEt?.text.toString()
        val a = addressEt?.text.toString()
        val c = cityEt?.text.toString()
        val s = stateEt?.text.toString()
        val m = mobileEt?.text.toString()

        ref.child("Name").setValue(n)
        ref.child("name").setValue(n)
        ref.child("Street_No").setValue(a)
        ref.child("street_No").setValue(a)
        ref.child("City").setValue(c)
        ref.child("city").setValue(c)
        ref.child("State").setValue(s)
        ref.child("state").setValue(s)
        ref.child("Contact_Number").setValue(m)
        ref.child("contact_Number").setValue(m)
        ref.child("Mobile").setValue(m)

        val recRef = mDatabase?.child("Recruiter")?.child(user.uid)
        recRef?.child("Name")?.setValue(n)
        recRef?.child("name")?.setValue(n)

        pd?.dismiss()
        startActivity(Intent(this@EditRecruiter, RecruiterProfile::class.java))
        finish()
    }

    override fun onBackPressed() {
        startActivity(Intent(this@EditRecruiter, RecruiterProfile::class.java))
        finish()
    }
}
