package com.kvsn.builds.cap1

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import org.json.JSONException
import kotlin.math.round

class Show_Available_worker_Profile : AppCompatActivity() {
    private var tv_name1: TextView? = null
    private var tv_profession1: TextView? = null
    private var tv_phone1: TextView? = null
    private var tv_alter_phone1: TextView? = null
    private var tv_rating_num1: TextView? = null
    private var tv_experience_num1: TextView? = null
    private var tv_email_id: TextView? = null
    private var l1: LinearLayout? = null
    private var b1: Button? = null
    private var b2: Button? = null

    private var prevrating: Float = 0.0f
    private var oldexp: Long = 0L
    private var img_profile1: ImageView? = null
    private var database: DatabaseReference? = null
    private var detailref: DatabaseReference? = null
    private var expref: DatabaseReference? = null
    private var refrev: DatabaseReference? = null
    private var dupref: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var UrlToImg: String? = null
    private var type: String? = null
    private var key: String? = ""
    private var seeker_id: String? = null
    private var rRequestQueue: RequestQueue? = null
    private var mob: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_show__available_worker__profile)
            val sharedPreferences = getSharedPreferences("Getkey", Context.MODE_PRIVATE)
            key = sharedPreferences.getString("key", "")
            tv_name1 = findViewById(R.id.tv_name)

            title = getString(R.string.app_name)

            CheckInternetConnection(this).checkConnection()
            b1 = findViewById(R.id.confirm)
            b2 = findViewById(R.id.finish)
            b1?.isClickable = false
            l1 = findViewById(R.id.confirmandfinish)
            tv_profession1 = findViewById(R.id.tv_profession)
            tv_phone1 = findViewById(R.id.tv_phone)
            tv_alter_phone1 = findViewById(R.id.tv_alter_phone)
            tv_rating_num1 = findViewById(R.id.tv_rating_num)
            tv_experience_num1 = findViewById(R.id.tv_experience_num)
            img_profile1 = findViewById(R.id.img_profile)
            tv_email_id = findViewById(R.id.tv_email_id)
            
            rRequestQueue = Volley.newRequestQueue(this)
            database = FirebaseDatabase.getInstance("https://self-employment-app-default-rtdb.asia-southeast1.firebasedatabase.app").reference
            detailref = database?.child("Extra detail of seeker")?.child("New work of all")
            expref = database?.child("Extra detail of seeker")?.child("Experience of all")
            refrev = database?.child("Review")
            mAuth = FirebaseAuth.getInstance()
            displayProfile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun displayProfile() {
        val workerKey = key ?: return
        database?.child("Users")?.child(workerKey)?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        val n = if (dataSnapshot.hasChild("name")) dataSnapshot.child("name").value.toString()
                                else if (dataSnapshot.hasChild("Name")) dataSnapshot.child("Name").value.toString()
                                else "Unknown"
                        tv_name1?.text = n
                        
                        val p = if (dataSnapshot.hasChild("profession")) dataSnapshot.child("profession").value.toString()
                                else if (dataSnapshot.hasChild("Profession")) dataSnapshot.child("Profession").value.toString()
                                else "Worker"
                        tv_profession1?.text = p
                        type = p

                        val m = if (dataSnapshot.hasChild("contact_Number")) dataSnapshot.child("contact_Number").value.toString()
                                else if (dataSnapshot.hasChild("Contact_Number")) dataSnapshot.child("Contact_Number").value.toString()
                                else if (dataSnapshot.hasChild("Mobile")) dataSnapshot.child("Mobile").value.toString()
                                else "Not Set"
                        tv_phone1?.text = m
                        mob = m

                        val am = if (dataSnapshot.hasChild("alternate_Contact_Number")) dataSnapshot.child("alternate_Contact_Number").value.toString()
                                 else if (dataSnapshot.hasChild("Alternate_Contact_Number")) dataSnapshot.child("Alternate_Contact_Number").value.toString()
                                 else if (dataSnapshot.hasChild("Alternate Mobile")) dataSnapshot.child("Alternate Mobile").value.toString()
                                 else "Not Set"
                        tv_alter_phone1?.text = am

                        val e = if (dataSnapshot.hasChild("email")) dataSnapshot.child("email").value.toString()
                                else if (dataSnapshot.hasChild("Email")) dataSnapshot.child("Email").value.toString()
                                else "Not Set"
                        tv_email_id?.text = e

                        seeker_id = if (dataSnapshot.hasChild("id")) dataSnapshot.child("id").value.toString()
                                    else if (dataSnapshot.hasChild("Id")) dataSnapshot.child("Id").value.toString()
                                    else workerKey

                        if (dataSnapshot.hasChild("experience")) {
                            tv_experience_num1?.text = dataSnapshot.child("experience").value.toString()
                            oldexp = dataSnapshot.child("experience").value.toString().toLongOrNull() ?: 0L
                        } else if (dataSnapshot.hasChild("Experience")) {
                            tv_experience_num1?.text = dataSnapshot.child("Experience").value.toString()
                            oldexp = dataSnapshot.child("Experience").value.toString().toLongOrNull() ?: 0L
                        } else {
                            tv_experience_num1?.text = "0"
                            oldexp = 0L
                        }
                        
                        if (dataSnapshot.hasChild("urlToImage")) {
                            UrlToImg = dataSnapshot.child("urlToImage").value.toString()
                            if (!UrlToImg.isNullOrEmpty()) {
                                Picasso.get().load(UrlToImg).into(img_profile1)
                            }
                        }

                        if (dataSnapshot.hasChild("Rating")) {
                            tv_rating_num1?.text = dataSnapshot.child("Rating").value.toString()
                            prevrating = dataSnapshot.child("Rating").value.toString().toFloatOrNull() ?: 0.0f
                        } else {
                            tv_rating_num1?.text = "New"
                            prevrating = 0.0f
                        }
                        
                        dupref = database?.child("Seeker")?.child(type ?: "")?.child(workerKey)
                        
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                } else {
                    Toast.makeText(applicationContext, "Data does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(applicationContext, "Data loading failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showconfirm(v: View) {
        val number = tv_phone1?.text.toString()
        val i = Intent(Intent.ACTION_DIAL)
        i.data = Uri.parse("tel:+91$number")
        startActivity(i)
        l1?.visibility = View.VISIBLE
        b2?.isClickable = false
        b1?.isClickable = true

        seeker_id?.let { sid ->
            expref?.child(sid)?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild("New work") && dataSnapshot.child("New work").value != null) {
                        var valI = dataSnapshot.child("New work").getValue(Int::class.java) ?: 0
                        valI++
                        expref?.child(sid)?.child("New work")?.setValue(valI)
                    } else {
                        expref?.child(sid)?.child("New work")?.setValue(1)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
    }

    fun showdone(v: View) {
        b2?.isClickable = true

        seeker_id?.let { sid ->
            expref?.child(sid)?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild("New work") && dataSnapshot.child("New work").value != null) {
                        var valL = dataSnapshot.child("New work").getValue(Long::class.java) ?: 0L
                        if (valL > 0) valL--
                        expref?.child(sid)?.child("New work")?.setValue(valL)
                    } else {
                        expref?.child(sid)?.child("New work")?.setValue(0)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        sendmsg()
        b1?.isClickable = false
    }

    fun updaterating(currentrating: Float) {
        val workerKey = key ?: return
        if (prevrating == 0.0f) {
            database?.child("Users")?.child(workerKey)?.child("Rating")?.setValue(currentrating)
        } else {
            val newxp = oldexp + 1
            val newrating = round((((prevrating * oldexp) + currentrating) / newxp) * 100.0) / 100
            database?.child("Users")?.child(workerKey)?.child("Rating")?.setValue(newrating)
            dupref?.child("Rating")?.setValue(newrating)
        }
        b2?.isClickable = false
    }

    fun workdone(v: View) {
        val builder = AlertDialog.Builder(this@Show_Available_worker_Profile)
        val inflater = layoutInflater
        builder.setTitle("Please Review The Work..")
        val dialogLayout = inflater.inflate(R.layout.activity_reviewdailog, null)
        val ratingBar = dialogLayout.findViewById<RatingBar>(R.id.ratingbar)
        val comment = dialogLayout.findViewById<EditText>(R.id.review_comment)
        builder.setView(dialogLayout)
        builder.setCancelable(false)
        builder.setPositiveButton("Submit") { dialog, which ->
            val com = comment.text.toString()
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                refrev?.child(key ?: "")?.child(currentUser.uid)?.setValue(com)
            }
        }
        builder.show()
        
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            database?.child("Users")?.child(key ?: "")?.child("Rating")?.setValue(rating)
            seeker_id?.let { sid ->
                expref?.child(sid)?.child("Experience")?.setValue(rating)
            }
            updaterating(rating)
        }

        seeker_id?.let { sid ->
            expref?.child(sid)?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild("Experience")) {
                        var valL = dataSnapshot.child("Experience").getValue(Long::class.java) ?: 0L
                        valL++
                        expref?.child(sid)?.child("Experience")?.setValue(valL)
                        database?.child("Users")?.child(sid)?.child("Experience")?.setValue(valL)
                        dupref?.child("Experience")?.setValue(valL)
                    } else {
                        expref?.child(sid)?.child("Experience")?.setValue(1)
                        database?.child("Users")?.child(sid)?.child("Experience")?.setValue(1)
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }
        b1?.isClickable = false
    }

    fun sendmsg() {
        parseJASON()
    }

    fun parseJASON() {
        val sharedPreferences = getSharedPreferences("Categories", Context.MODE_PRIVATE)
        val address = sharedPreferences.getString("Address", "") ?: ""
        
        if (mob.isNullOrEmpty() || mob == "Not Set") {
            Toast.makeText(applicationContext, "Worker mobile number not available", Toast.LENGTH_SHORT).show()
            return
        }

        val ms = "This is from Sharm Address of the work Area:\n$address"
        val encodedMsg = Uri.encode(ms)
        val url = "https://www.fast2sms.com/dev/bulk?authorization=zjKM1fZomdYNROkyDsvHc2wSVB0Jnex8rTWQbIX6ClgLUita3GzLFh9iHA4gdyKrV1oN6CYcJUSMjD73&sender_id=FSTSMS&message=$encodedMsg&language=english&route=p&numbers=$mob"
        
        val request = JsonObjectRequest(Request.Method.GET, url, null, { response ->
            try {
                val s = response.getString("return")
                if (s == "true") {
                    Toast.makeText(applicationContext, "Message sent successfully to $mob", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "Message sending failed: $response", Toast.LENGTH_LONG).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error parsing response", Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            error.printStackTrace()
            Toast.makeText(applicationContext, "Messaging API Error: ${error.message}", Toast.LENGTH_LONG).show()
        })
        rRequestQueue?.add(request)
    }

    fun callWorker(v: View) {
        showconfirm(v)
    }
}
