package com.kvsn.builds.cap1

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var r1: RelativeLayout
    private lateinit var r2: RelativeLayout
    private lateinit var login_icon: ImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var msubref: DatabaseReference? = null
    private lateinit var et_mail: EditText
    private lateinit var et_pwd: EditText
    private var mail: String? = null
    private var pwd: String? = null
    private var type: String? = null
    private lateinit var pd: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "App Started", Toast.LENGTH_SHORT).show()
        setContentView(R.layout.activity_main)

        r1 = findViewById(R.id.relative_login)
        login_icon = findViewById(R.id.imgView_logo)
        r2 = findViewById(R.id.relative_signup)
        et_mail = findViewById(R.id.login_mail)
        et_pwd = findViewById(R.id.login_pwd)
        pd = ProgressDialog(this)

        val cb_show_pwd = findViewById<CheckBox>(R.id.show_pwd_login)
        cb_show_pwd?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                et_pwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                et_pwd.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            et_pwd.setSelection(et_pwd.text.length)
        }

        // initialising firebase services
        mDatabase = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()

        // Show UI immediately to fix black screen
        r1.visibility = View.VISIBLE
        r2.visibility = View.VISIBLE
    }

    fun login(v: View?) {
        val m = et_mail.text.toString().trim()
        val p = et_pwd.text.toString().trim()
        pd.setTitle("Logging In.....")
        pd.show()
        pd.setCanceledOnTouchOutside(false)

        try {
            mAuth.signInWithEmailAndPassword(m, p).addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    retrieve()
                } else {
                    pd.dismiss()
                    Toast.makeText(this@MainActivity, "Sign In Failed: " + task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            pd.dismiss()
            Toast.makeText(this, "Login Crash: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun retrieve() {
        val user = mAuth.currentUser
        if (user == null) {
            Toast.makeText(this@MainActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
            return
        }
        msubref = mDatabase.child("Users").child(user.uid)
        try {
            msubref?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists() && (dataSnapshot.hasChild("Type") || dataSnapshot.hasChild("type"))) {
                        var type: String? = null
                        if (dataSnapshot.hasChild("Type")) {
                            type = dataSnapshot.child("Type").getValue(String::class.java)
                        } else if (dataSnapshot.hasChild("type")) {
                            type = dataSnapshot.child("type").getValue(String::class.java)
                        }

                        if (type != null) {
                            if (type.equals("Seeker", ignoreCase = true)) {
                                pd.dismiss()
                                startActivity(Intent(this@MainActivity, SeekerMain::class.java))
                                finish()
                            } else if (type.equals("Recruiter", ignoreCase = true)) {
                                pd.dismiss()
                                startActivity(Intent(this@MainActivity, RecruiterMain::class.java))
                                finish()
                            } else {
                                pd.dismiss()
                                Toast.makeText(this@MainActivity, "Unknown user type", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            pd.dismiss()
                            Toast.makeText(this@MainActivity, "User profile incomplete", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        pd.dismiss()
                        Toast.makeText(this@MainActivity, "User data not found for ID: " + user.uid, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    pd.dismiss()
                    Toast.makeText(this@MainActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Retrieve Crash: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun validation(): Boolean {
        var valid = true
        val m = et_mail.text.toString()
        val p = et_pwd.text.toString()

        if (m.isEmpty()) {
            et_mail.error = "Email is required"
            et_mail.requestFocus()
            valid = false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(m).matches()) {
            et_mail.error = "Please Enter valid Email address"
            et_mail.requestFocus()
            valid = false
        }
        if (p.isEmpty()) {
            et_pwd.error = "Please Enter the password"
            et_pwd.requestFocus()
            valid = false
        }
        if (p.length < 6) {
            et_pwd.error = "Minimum password length is 6"
            et_pwd.requestFocus()
            valid = false
        }
        return valid
    }

    fun signup(v: View?) { // called when signup button is clicked
        val shift_to_signup = Intent(this@MainActivity, Signup::class.java)
        val actop = ActivityOptionsCompat.makeSceneTransitionAnimation(this, login_icon, ViewCompat.getTransitionName(login_icon)!!)
        startActivity(shift_to_signup, actop.toBundle())
        overridePendingTransition(R.anim.fadein, R.anim.fadeout) // to replace default animations with fade in and fade out;
    }

    fun forgotpassword(v: View?) {
        startActivity(Intent(this@MainActivity, ForgotPasswordModule::class.java))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
