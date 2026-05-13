package com.kvsn.builds.cap1

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle
import java.util.*

class RecruiterMain : AppCompatActivity(), DuoMenuView.OnMenuClickListener {
    private var mMenuAdapter: MenuAdapter? = null
    private var mViewHolder: ViewHolder? = null
    private var header: CircleImageView? = null
    private var mAuth: FirebaseAuth? = null
    private var mDatabase: DatabaseReference? = null
    private var mref: DatabaseReference? = null
    private var pd: ProgressDialog? = null
    private var mStorageRef: StorageReference? = null
    private var nameTv: TextView? = null
    private var mailTv: TextView? = null
    private var mTitles = ArrayList<String>()

    private val cropImage: ActivityResultLauncher<CropImageContractOptions> = 
        registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val resultUri = result.uriContent
                handleCropResult(resultUri)
            } else {
                val error = result.error
                Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_main)

        try {
            mViewHolder = ViewHolder()
            
            mViewHolder?.mDuoMenuView?.let { menuView ->
                nameTv = menuView.findViewById(R.id.header_name)
                mailTv = menuView.findViewById(R.id.header_mail)
                header = menuView.findViewById(R.id.image_header)
            }
            
            pd = ProgressDialog(this)
            mAuth = FirebaseAuth.getInstance()
            mDatabase = FirebaseDatabase.getInstance("https://self-employment-app-default-rtdb.asia-southeast1.firebasedatabase.app").reference
            mStorageRef = FirebaseStorage.getInstance().reference
            
            val currentUser = mAuth?.currentUser
            if (currentUser != null) {
                mref = mDatabase?.child("Users")?.child(currentUser.uid)
            } else {
                finish()
                return
            }
            
            mTitles = ArrayList(listOf(*resources.getStringArray(R.array.menuOptions)))

            handleToolbar()
            handleMenu()
            handleDrawer()
            
            mMenuAdapter?.setViewSelected(0, true)
            title = mTitles[0]
            
            header?.setOnClickListener {
                val galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK)
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Setup Error: ${e.message}", Toast.LENGTH_LONG).show()
        }

        mref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        nameTv?.text = dataSnapshot.child("Name").getValue(String::class.java)
                        mailTv?.text = dataSnapshot.child("Email").getValue(String::class.java)
                        if (dataSnapshot.hasChild("urlToImage")) {
                            header?.let {
                                Picasso.get().load(dataSnapshot.child("urlToImage").getValue(String::class.java)).into(it)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RecruiterMain, "Data Load Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            val imageUri = data?.data
            val cropImageOptions = CropImageOptions()
            cropImageOptions.imageSourceIncludeGallery = true
            cropImageOptions.aspectRatioX = 1
            cropImageOptions.aspectRatioY = 1
            cropImageOptions.fixAspectRatio = true
            
            imageUri?.let {
                cropImage.launch(CropImageContractOptions(it, cropImageOptions))
            }
        }
    }

    private fun handleCropResult(resultUri: Uri?) {
        if (resultUri == null) return
        
        pd?.setTitle("Uploading")
        pd?.setMessage("Please wait")
        pd?.show()

        val currentUserId = mAuth?.currentUser?.uid ?: return
        val filepath = mStorageRef?.child("profile_images")?.child("$currentUserId.jpg")

        filepath?.putFile(resultUri)?.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                mref?.child("urlToImage")?.setValue(downloadUrl)
                pd?.dismiss()
                Toast.makeText(this@RecruiterMain, "Upload Success", Toast.LENGTH_LONG).show()
            }
        }?.addOnFailureListener { e ->
            pd?.dismiss()
            Toast.makeText(this@RecruiterMain, "Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleToolbar() {
        mViewHolder?.mToolbar?.let {
            setSupportActionBar(it)
        }
    }

    private fun handleDrawer() {
        val drawerLayout = mViewHolder?.mDuoDrawerLayout
        val toolbar = mViewHolder?.mToolbar
        if (drawerLayout != null && toolbar != null) {
            val duoDrawerToggle = DuoDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
            )
            drawerLayout.setDrawerListener(duoDrawerToggle)
            duoDrawerToggle.syncState()
        }
    }

    private fun handleMenu() {
        mMenuAdapter = MenuAdapter(mTitles)
        mViewHolder?.mDuoMenuView?.let { duoMenuView ->
            duoMenuView.setOnMenuClickListener(this)
            duoMenuView.adapter = mMenuAdapter
            
            // Explicitly handle the footer click as a backup
            duoMenuView.findViewById<View>(R.id.footer_logout)?.setOnClickListener {
                onFooterClicked()
            }
        }
    }

    override fun onFooterClicked() {
        pd?.setTitle("Logging out")
        pd?.show()
        mAuth?.signOut()
        startActivity(Intent(this@RecruiterMain, MainActivity::class.java))
        finish()
    }

    override fun onHeaderClicked() {
        val i = Intent(this, RecruiterProfile::class.java)
        val headerView = header
        if (headerView != null) {
            val actop = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, headerView, ViewCompat.getTransitionName(headerView) ?: ""
            )
            startActivity(i, actop.toBundle())
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }
    }

    override fun onOptionClicked(position: Int, objectClicked: Any) {
        when (position) {
            2 -> startActivity(Intent(this@RecruiterMain, AboutUs::class.java))
            1 -> onHeaderClicked()
        }
        mViewHolder?.mDuoDrawerLayout?.closeDrawer()
    }

    private inner class ViewHolder {
        var mDuoDrawerLayout: DuoDrawerLayout? = findViewById(R.id.drawer)
        var mDuoMenuView: DuoMenuView? = mDuoDrawerLayout?.menuView as? DuoMenuView
        var mToolbar: Toolbar? = findViewById(R.id.toolbar)
    }

    fun dothis(v: View) {
        val sharedPreferences = getSharedPreferences("Categories", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        var category: String? = null
        var intent: Intent? = null
        val id = v.id
        
        when (id) {
            R.id.electrician -> {
                category = "Electrician"
                Toast.makeText(this@RecruiterMain, "Electrician", Toast.LENGTH_SHORT).show()
            }
            R.id.bricklayer -> {
                category = "Mason"
                Toast.makeText(this@RecruiterMain, "Mason", Toast.LENGTH_SHORT).show()
            }
            R.id.carpainter -> {
                category = "Carpenter"
                Toast.makeText(this@RecruiterMain, "Carpenter", Toast.LENGTH_SHORT).show()
            }
            R.id.plumber -> {
                category = "Plumber"
                Toast.makeText(this@RecruiterMain, "Plumber", Toast.LENGTH_SHORT).show()
            }
            R.id.painter -> {
                category = "Painter"
                Toast.makeText(this@RecruiterMain, "Painter", Toast.LENGTH_SHORT).show()
            }
            R.id.labour -> {
                category = "Labour"
                Toast.makeText(this@RecruiterMain, "Labour", Toast.LENGTH_SHORT).show()
            }
        }
        
        category?.let {
            editor.putString("categorie", it)
            editor.apply()
            intent = Intent(this@RecruiterMain, GetLocation::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        // Keeping it empty as per original Java
    }

    companion object {
        private const val GALLERY_PICK = 1
    }
}
