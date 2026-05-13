package com.kvsn.builds.cap1

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class ShowComments : AppCompatActivity() {
    private var database: DatabaseReference? = null
    private var mref: DatabaseReference? = null
    var mAuth: FirebaseAuth? = null
    private var lv: ListView? = null
    private var commentslist = ArrayList<String>()
    private var arrayAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_comments)
        lv = findViewById(R.id.lv)
        title = "Comments"

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, commentslist)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val currentUser = mAuth?.currentUser
        if (currentUser != null) {
            mref = database?.child("Review")?.child(currentUser.uid)
        }

        mref?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                commentslist.clear()
                for (dataloop in dataSnapshot.children) {
                    val id = dataloop.key?.trim() ?: ""
                    val comment = dataloop.getValue(String::class.java)
                    if (comment != null) {
                        commentslist.add(comment)
                    }
                }
                adapterload()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun adapterload() {
        lv?.adapter = arrayAdapter
    }
}
