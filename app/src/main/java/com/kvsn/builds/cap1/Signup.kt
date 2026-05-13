package com.kvsn.builds.cap1

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    private var states: Array<String> = arrayOf()
    private var emailStr = ""
    private var id: String? = null
    private var contactNumber = ""
    private var aadharNumber = ""
    private var streetNo = ""
    private var pincode = ""
    private var state = ""
    private var city = ""
    private var gender = ""
    private var passwordStr = ""
    private var confirmPasswordStr = ""
    private var profession = ""
    private var type = ""
    private var name = ""
    private var alternateContactNumber = ""
    private val TAG = "abcdefg"

    private lateinit var editTextEmail: EditText
    private lateinit var editTextContact_No: EditText
    private lateinit var editTextAadhar_No: EditText
    private lateinit var editTextStreet: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var editTextPincode: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextAlternate_contact_No: EditText
    private lateinit var pd: ProgressDialog
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerProfession: Spinner
    private lateinit var spinnerState: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var radioButtonSeeker: RadioButton
    private lateinit var radioButtonRecruiter: RadioButton
    private lateinit var mAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var database: DatabaseReference
    private lateinit var recRef: DatabaseReference
    private lateinit var subRef: DatabaseReference
    private lateinit var seekerRef: DatabaseReference
    private lateinit var dupRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        title = "SignUp"

        CheckInternetConnection(this).checkConnection()

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://self-employment-app-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        dupRef = database.child("Users")
        recRef = database.child("Recruiter")
        seekerRef = database.child("Seeker")

        init()

        // Gender Spinner
        val items = arrayOf("Select", "Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinnerGender.adapter = adapter
        spinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                gender = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                gender = ""
            }
        }

        // Profession Spinner
        val items1 = arrayOf("Select", "Electrician", "Mason", "Carpenter", "Painter", "Plumber", "Labour")
        val professionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items1)
        spinnerProfession.isEnabled = false
        spinnerProfession.isClickable = false
        spinnerProfession.adapter = professionAdapter
        spinnerProfession.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                profession = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                profession = ""
            }
        }

        // State Spinner
        states = resources.getStringArray(R.array.states)
        val stateAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, states) {
            override fun isEnabled(position: Int): Boolean = position != 0
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView
                tv.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinnerState.adapter = stateAdapter
        spinnerState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val s = parent.selectedItem.toString()
                loadCities(s)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun init() {
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        editTextAadhar_No = findViewById(R.id.editTextAadhar_No)
        editTextStreet = findViewById(R.id.editTextStreet)
        editTextName = findViewById(R.id.editTextName)
        editTextPincode = findViewById(R.id.editTextPincode)
        editTextContact_No = findViewById(R.id.editTextContact_No)
        editTextAlternate_contact_No = findViewById(R.id.editTextAlternate_No)
        radioButtonRecruiter = findViewById(R.id.Radio_Btn_recruiter)
        radioButtonSeeker = findViewById(R.id.Radio_btn_seeker)
        spinnerGender = findViewById(R.id.spinner_gender)
        spinnerState = findViewById(R.id.statespinner)
        spinnerCity = findViewById(R.id.cityspinner)
        spinnerProfession = findViewById(R.id.spinner_profession)
        pd = ProgressDialog(this)

        findViewById<CheckBox>(R.id.show_pwd_signup)?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                editTextConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                editTextConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            editTextPassword.setSelection(editTextPassword.text.length)
            editTextConfirmPassword.setSelection(editTextConfirmPassword.text.length)
        }
    }

    override fun onClick(v: View?) {}
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
    override fun onNothingSelected(parent: AdapterView<*>?) {}

    fun test(v: View?) {
        createUser()
    }

    fun onRadioButtonclicked(view: View) {
        val id = view.id
        if (id == R.id.Radio_btn_seeker) {
            if (radioButtonSeeker.isChecked) {
                spinnerProfession.isEnabled = true
                spinnerProfession.isClickable = true
                type = "Seeker"
            }
        } else if (id == R.id.Radio_Btn_recruiter) {
            if (radioButtonRecruiter.isChecked) {
                spinnerProfession.isEnabled = false
                spinnerProfession.isClickable = false
                profession = ""
                type = "Recruiter"
            }
        }
    }

    private fun createUser() {
        pd.setTitle("Registering User Please Wait...")
        pd.show()
        pd.setCanceledOnTouchOutside(false)

        emailStr = editTextEmail.text.toString().trim()
        passwordStr = editTextPassword.text.toString().trim()
        name = editTextName.text.toString().trim()
        contactNumber = editTextContact_No.text.toString().trim()
        aadharNumber = editTextAadhar_No.text.toString().trim()
        alternateContactNumber = editTextAlternate_contact_No.text.toString().trim()
        state = spinnerState.selectedItem.toString().trim()
        streetNo = editTextStreet.text.toString().trim()
        city = spinnerCity.selectedItem.toString().trim()
        pincode = editTextPincode.text.toString().trim()
        confirmPasswordStr = editTextConfirmPassword.text.toString().trim()

        if (!validation()) {
            pd.dismiss()
            return
        }

        mAuth.createUserWithEmailAndPassword(emailStr, passwordStr).addOnSuccessListener(this) {
            Toast.makeText(this, "User Registered", Toast.LENGTH_SHORT).show()
            pd.dismiss()
            currentUser = FirebaseAuth.getInstance().currentUser
            id = currentUser?.uid
            writeData()
        }.addOnFailureListener {
            Toast.makeText(this, "Email Already In Use", Toast.LENGTH_SHORT).show()
            pd.dismiss()
        }
    }

    private fun duplicate() {
        val u = User(emailStr, id, contactNumber, aadharNumber, streetNo, pincode, state, city, gender, profession, type, name, alternateContactNumber)
        id?.let {
            dupRef.child(it).setValue(u).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(applicationContext, "Failed to save data: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun writeData() {
        val uid = id ?: return
        val u = User(emailStr, uid, contactNumber, aadharNumber, streetNo, pincode, state, city, gender, profession, type, name, alternateContactNumber)
        
        if (radioButtonRecruiter.isChecked) {
            duplicate()
            recRef.child(uid).setValue(u).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    database.child("Users").child(uid).child("Profession").setValue("")
                    Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, RecruiterMain::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error saving recruiter data: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else if (radioButtonSeeker.isChecked) {
            val category = spinnerProfession.selectedItem.toString()
            seekerRef.child(category).child(uid).setValue(u).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    duplicate()
                    seekerRef.child(category).child(uid).child("isAvailable").setValue(true)
                    database.child("Users").child(uid).child("isAvailable").setValue(true)
                    Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SeekerMain::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error saving seeker data: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validation(): Boolean {
        var valid = true
        if (emailStr.isEmpty()) {
            editTextEmail.error = "Email is required"
            editTextEmail.requestFocus()
            valid = false
        }
        if (!radioButtonSeeker.isChecked && !radioButtonRecruiter.isChecked) {
            Toast.makeText(this, "Please select whether you are a Seeker or Recruiter", Toast.LENGTH_SHORT).show()
            valid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
            editTextEmail.error = "Please Enter valid Email address"
            editTextEmail.requestFocus()
            valid = false
        }
        if (passwordStr.isEmpty()) {
            editTextPassword.error = "Please Enter the password"
            editTextPassword.requestFocus()
            valid = false
        }
        if (passwordStr.length < 6) {
            editTextPassword.error = "Minimum password length is 6"
            editTextPassword.requestFocus()
            valid = false
        }
        if (passwordStr != confirmPasswordStr) {
            editTextConfirmPassword.error = "Passwords do not match"
            editTextConfirmPassword.requestFocus()
            valid = false
        }
        if (gender.isEmpty() || gender == "Select") {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
            valid = false
        }
        if (pincode.isEmpty()) {
            editTextPincode.error = "Enter Pincode"
            editTextPincode.requestFocus()
            valid = false
        }
        if (contactNumber.isEmpty()) {
            editTextContact_No.error = "Enter Mobile Number"
            editTextContact_No.requestFocus()
            valid = false
        }
        if (aadharNumber.isEmpty()) {
            editTextAadhar_No.error = "Enter Aadhaar"
            editTextAadhar_No.requestFocus()
            valid = false
        }
        if (streetNo.isEmpty()) {
            editTextStreet.error = "Enter the Street Name or Number"
            editTextStreet.requestFocus()
            valid = false
        }
        if (name.isEmpty()) {
            editTextName.error = "Enter name"
            editTextName.requestFocus()
            valid = false
        }
        return valid
    }

    private fun loadCities(stateName: String) {
        val resourceId = when (stateName) {
            "Andaman and Nicobar Islands" -> R.array.andaman
            "Andhra Pradesh" -> R.array.andhrapradesh
            "Arunachal Pradesh" -> R.array.arunachalpradesh
            "Assam" -> R.array.assam
            "Bihar" -> R.array.bihar
            "Chandigarh" -> R.array.chandigarh
            "Chattisgarh" -> R.array.chattisgarh
            "Dadra and Nagar Haveli" -> R.array.dadranagarhaveli
            "Daman and Diu" -> R.array.damandiu
            "Delhi" -> R.array.Delhi
            "Goa" -> R.array.Goa
            "Gujarat" -> R.array.gujarat
            "Haryana" -> R.array.haryana
            "Himachal Pradesh" -> R.array.himachal
            "Jammu and Kashmir" -> R.array.jammu
            "Jharkhand" -> R.array.jarkhand
            "Karnataka" -> R.array.karnataka
            "Kerala" -> R.array.kerala
            "Lakshwadweep" -> R.array.lakshwadeep
            "Madhya Pradesh" -> R.array.madhyapradesh
            "Maharashtra" -> R.array.maharashtra
            "Manipur" -> R.array.manipur
            "Meghalaya" -> R.array.meghalaya
            "Mizoram" -> R.array.mizoram
            "Nagaland" -> R.array.nagaland
            "Orissa" -> R.array.orissa
            "Pondicherry" -> R.array.pondicherry
            "Punjab" -> R.array.Punjab
            "Rajasthan" -> R.array.rajasthan
            "Sikkim" -> R.array.sikkim
            "Tamil Nadu" -> R.array.tamilnadu
            "Tripura" -> R.array.tripura
            "Uttar Pradesh" -> R.array.uttarpradesh
            "Uttaranchal" -> R.array.uttaranchal
            "West Bengal" -> R.array.westbengal
            else -> null
        }

        resourceId?.let {
            val adapter = ArrayAdapter.createFromResource(this, it, android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCity.adapter = adapter
        }
    }
}
