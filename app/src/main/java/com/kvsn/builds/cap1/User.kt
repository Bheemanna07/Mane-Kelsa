package com.kvsn.builds.cap1

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var email: String? = "",
    var id: String? = "",
    var contact_Number: String? = "",
    var aadhar_Number: String? = "",
    var street_No: String? = "",
    var pincode: String? = "",
    var state: String? = "",
    var city: String? = "",
    var gender: String? = "",
    var profession: String? = "",
    var type: String? = "",
    var name: String? = "",
    var alternate_Contact_Number: String? = ""
)
