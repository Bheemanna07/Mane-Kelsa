package com.kvsn.builds.cap1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PhotoActivity : AppCompatActivity() {
    private var iv: CircleImageView? = null
    private var UserProfileImagesReference: StorageReference? = null
    private var mauth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var mDatabase: DatabaseReference? = null
    private var msubref: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        title = "Profile Image"

        CheckInternetConnection(this).checkConnection()
        iv = findViewById(R.id.iv)
        
        mauth = FirebaseAuth.getInstance()
        user = mauth?.currentUser
        
        UserProfileImagesReference = FirebaseStorage.getInstance().reference.child("Profile Pictures")
        mDatabase = FirebaseDatabase.getInstance().reference
        
        val currentUserUid = user?.uid ?: return
        msubref = mDatabase?.child("Users")?.child(currentUserUid)
        
        msubref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("urlToImage")) {
                    Picasso.get().load(dataSnapshot.child("urlToImage").getValue(String::class.java)).into(iv)
                } else {
                    iv?.setImageResource(R.drawable.profile)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
