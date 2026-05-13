package com.kvsn.builds.cap1

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordModule : Activity() {
    private var editTextEmail: EditText? = null
    private var mAuth: FirebaseAuth? = null
    private var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_module)
        title = "Forgot Password"

        pd = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
    }

    private fun passwordReset(email: String) {
        pd?.setMessage("Sending reset email...")
        pd?.show()

        mAuth?.sendPasswordResetEmail(email.trim())?.addOnCompleteListener { task ->
            pd?.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(
                    applicationContext,
                    "Reset Password instruction has been sent to your registered email id",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                val error = task.exception?.message ?: "Failed to send reset email"
                Toast.makeText(applicationContext, error, Toast.LENGTH_LONG).show()
            }
        }?.addOnFailureListener { e ->
            pd?.dismiss()
            Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validation(email: String): Boolean {
        var valid = true
        if (email.isEmpty()) {
            editTextEmail?.error = "Email is required"
            editTextEmail?.requestFocus()
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail?.error = "Please enter a valid email address"
            editTextEmail?.requestFocus()
            valid = false
        }
        return valid
    }

    fun Reset(view: View) {
        editTextEmail = findViewById(R.id.editTextForgotPassword)
        val emailInput = editTextEmail?.text.toString().trim()

        if (emailInput.isEmpty()) {
            editTextEmail?.error = "Please enter your email"
            return
        }

        // Check if it looks like a phone number
        if (emailInput.matches("\\d{10}".toRegex())) {
            Toast.makeText(
                this,
                "Phone number reset is not supported yet. Please enter your registered email.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (validation(emailInput)) {
            passwordReset(emailInput)
        }
    }
}
