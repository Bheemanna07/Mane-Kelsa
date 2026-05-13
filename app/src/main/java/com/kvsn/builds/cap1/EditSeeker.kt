package com.kvsn.builds.cap1

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class EditSeeker : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var msubref: DatabaseReference? = null
    private var nameEt: EditText? = null
    private var mobileEt: EditText? = null
    private var stateEt: EditText? = null
    private var cityEt: EditText? = null
    private var addressEt: EditText? = null
    private var professionEt: EditText? = null
    private var mailTv: TextView? = null
    private var aadhaarTv: TextView? = null
    private var profileIv: CircleImageView? = null
    private var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_seeker)

        title = "Edit Profile"
        CheckInternetConnection(this).checkConnection()

        nameEt = findViewById(R.id.name_seeker_edit)
        mailTv = findViewById(R.id.mail_seeker_edit)
        addressEt = findViewById(R.id.seeker_address_edit)
        aadhaarTv = findViewById(R.id.seeker_aadhaar_edit)
        cityEt = findViewById(R.id.seeker_city_edit)
        mobileEt = findViewById(R.id.seeker_mobile_edit)
        stateEt = findViewById(R.id.seeker_state_edit)
        professionEt = findViewById(R.id.profesion_seeker_edit)
        profileIv = findViewById(R.id.profile_image_seeker_edit)
        pd = ProgressDialog(this)

        mDatabase = FirebaseDatabase.getInstance("https://self-employment-app-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        mAuth = FirebaseAuth.getInstance()
        
        profileIv?.setOnClickListener {
            startActivity(Intent(applicationContext, PhotoUpload::class.java))
        }

        // Toast.makeText(this, "Test In Profile", Toast.LENGTH_SHORT).show()
        retrieve()
    }

    fun retrieve() {
        val user = mAuth?.currentUser ?: return
        msubref = mDatabase?.child("Users")?.child(user.uid)
        msubref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
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

                    val profVal = if (dataSnapshot.hasChild("profession")) dataSnapshot.child("profession").value.toString()
                                  else if (dataSnapshot.hasChild("Profession")) dataSnapshot.child("Profession").value.toString()
                                  else ""
                    professionEt?.setText(profVal)

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
        pd?.setTitle("Updating User Details Please Wait...")
        pd?.setCanceledOnTouchOutside(false)
        pd?.show()
        
        val user = mAuth?.currentUser ?: return
        val ref = mDatabase?.child("Users")?.child(user.uid) ?: return

        val n = nameEt?.text.toString()
        val a = addressEt?.text.toString()
        val c = cityEt?.text.toString()
        val s = stateEt?.text.toString()
        val m = mobileEt?.text.toString()
        val p = professionEt?.text.toString()

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
        ref.child("Profession").setValue(p)
        ref.child("profession").setValue(p)

        if (p.isNotEmpty()) {
            val seekerRef = mDatabase?.child("Seeker")?.child(p)?.child(user.uid)
            seekerRef?.child("Name")?.setValue(n)
            seekerRef?.child("name")?.setValue(n)
            seekerRef?.child("City")?.setValue(c)
            seekerRef?.child("city")?.setValue(c)
            seekerRef?.child("Contact_Number")?.setValue(m)
            seekerRef?.child("contact_Number")?.setValue(m)
        }

        pd?.dismiss()
        startActivity(Intent(this@EditSeeker, SeekerProfile::class.java))
        finish()
    }

    override fun onBackPressed() {
        startActivity(Intent(this@EditSeeker, SeekerProfile::class.java))
        finish()
    }
}
