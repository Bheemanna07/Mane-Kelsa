package com.kvsn.builds.cap1

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class PhotoUpload : AppCompatActivity() {
    private val SELECT_PICTURE = 100
    private var image: CircleImageView? = null
    private var filepath: Uri? = null
    private var mAuth: FirebaseAuth? = null
    private var rdatabase: DatabaseReference? = null
    private var ruserid: String? = null
    private var rstorage: StorageReference? = null
    private var rstorageReference: StorageReference? = null
    private var sstorageReference: StorageReference? = null
    private var category: String? = null
    private var type: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_upload)

        title = "Select Image"
        CheckInternetConnection(this).checkConnection()
        image = findViewById(R.id.uploadimage)
        
        mAuth = FirebaseAuth.getInstance()
        ruserid = mAuth?.currentUser?.uid
        
        if (ruserid != null) {
            rstorage = FirebaseStorage.getInstance().reference
            rdatabase = FirebaseDatabase.getInstance().getReference("Users").child(ruserid!!)
            rstorageReference = rstorage?.child("Images")?.child("RecruiterImages")
            sstorageReference = rstorage?.child("Images")?.child("SeekerImages")
            typeOfUser()
        } else {
            finish()
        }
    }

    private fun typeOfUser() {
        ruserid?.let { uid ->
            FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        category = dataSnapshot.child("Profession").getValue(String::class.java)
                        type = dataSnapshot.child("Type").getValue(String::class.java)
                    }
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }
    }

    fun chooseimage(v: View) {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
    }

    fun uploadimage(v: View) {
        val fileUri = filepath ?: run {
            Toast.makeText(application, "No file selected", Toast.LENGTH_SHORT).show()
            return
        }

        val pd = ProgressDialog(this)
        pd.setTitle("Uploading Image...")
        pd.setCanceledOnTouchOutside(false)
        pd.show()

        val userId = ruserid ?: return
        val ref: StorageReference = if ("Recruiter".equals(type, ignoreCase = true)) {
            rstorageReference!!.child("$userId.jpg")
        } else {
            sstorageReference!!.child("$userId.jpg")
        }

        ref.putFile(fileUri).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { uri ->
                val urlToImage = uri.toString()
                
                rdatabase?.child("urlToImage")?.setValue(urlToImage)
                rdatabase?.child("UrlToImage")?.setValue(urlToImage)
                
                if ("Seeker".equals(type, ignoreCase = true) && !category.isNullOrEmpty()) {
                    FirebaseDatabase.getInstance().getReference("Seeker")
                        .child(category!!).child(userId).child("urlToImage").setValue(urlToImage)
                }

                image?.let { iv ->
                    Picasso.get().load(urlToImage).transform(CropCircleTransformation()).into(iv)
                }
                pd.dismiss()
                Toast.makeText(applicationContext, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                pd.dismiss()
                Toast.makeText(this@PhotoUpload, "Failed to get download URL: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            pd.dismiss()
            Toast.makeText(application, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }.addOnProgressListener { taskSnapshot ->
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            pd.setMessage("Uploading: ${progress.toInt()}%")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            filepath = data?.data
            filepath?.let { uri ->
                image?.let { iv ->
                    Picasso.get().load(uri).transform(CropCircleTransformation()).into(iv)
                }
            }
        }
    }
}
