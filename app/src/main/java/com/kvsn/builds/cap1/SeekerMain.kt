package com.kvsn.builds.cap1

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
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

class SeekerMain : AppCompatActivity(), DuoMenuView.OnMenuClickListener {
    private var mMenuAdapter: MenuAdapter? = null
    private var mViewHolder: ViewHolder? = null
    private var header: CircleImageView? = null
    private var image: CircleImageView? = null
    private var mAuth: FirebaseAuth? = null
    private var pd: ProgressDialog? = null
    private var nameTv: TextView? = null
    private var aadhaarTv: TextView? = null
    private var mobileTv: TextView? = null
    private var commentsTv: TextView? = null
    private var experTv: TextView? = null
    private var newworkTv: TextView? = null
    private var categoryTv: TextView? = null
    private var headernameTv: TextView? = null
    private var headermailTv: TextView? = null
    private var availabilitySwitch: Switch? = null
    private var mStorageRef: StorageReference? = null
    private var mDatabase: DatabaseReference? = null
    private var msubref: DatabaseReference? = null
    private var workref: DatabaseReference? = null
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
        // Toast.makeText(this, "Starting Seeker Dashboard...", Toast.LENGTH_SHORT).show()
        setContentView(R.layout.activity_seeker_main)
        title = "Home"

        nameTv = findViewById(R.id.Seeker_name)
        aadhaarTv = findViewById(R.id.Seeker_adhar_no)
        mobileTv = findViewById(R.id.Seeker_mob_no)
        commentsTv = findViewById(R.id.textviewcomments)
        experTv = findViewById(R.id.exp)
        newworkTv = findViewById(R.id.new_work)
        categoryTv = findViewById(R.id.Seeker_type)
        mViewHolder = ViewHolder()
        
        mViewHolder?.mDuoMenuView?.let { menuView ->
            headernameTv = menuView.findViewById(R.id.header_name)
            headermailTv = menuView.findViewById(R.id.header_mail)
            header = menuView.findViewById(R.id.image_header)
        }

        pd = ProgressDialog(this)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance("https://self-employment-app-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        
        val currentUser = mAuth?.currentUser
        if (currentUser == null) {
            finish()
            return
        }
        
        msubref = mDatabase?.child("Users")?.child(currentUser.uid)
        mTitles = ArrayList(listOf(*resources.getStringArray(R.array.menuOptions)))
        image = findViewById(R.id.Img_profile_Seeker)
        workref = mDatabase?.child("Extra detail of seeker")?.child("Experience of all")?.child(currentUser.uid)

        handleToolbar()
        handleMenu()
        handleDrawer()

        mMenuAdapter?.setViewSelected(0, true)
        title = mTitles[0]

        availabilitySwitch = findViewById(R.id.availability_switch)
        availabilitySwitch?.setOnCheckedChangeListener { _, isChecked ->
            // 1. Update main user profile
            msubref?.child("isAvailable")?.setValue(isChecked)
            
            // 2. Fetch category and update Seeker node
            msubref?.child("Profession")?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var prof = snapshot.getValue(String::class.java)
                    if (prof == null) {
                        prof = categoryTv?.text.toString()
                    }
                    
                    if (!prof.isNullOrEmpty()) {
                        mDatabase?.child("Seeker")?.child(prof)?.child(currentUser.uid)?.child("isAvailable")?.setValue(isChecked)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        retrieve()

        mStorageRef = FirebaseStorage.getInstance().reference

        image?.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.type = "image/*"
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK)
        }
    }

    fun showComments(view: View) {
        startActivity(Intent(this@SeekerMain, ShowComments::class.java))
    }

    fun retrieve() {
        val currentUserUid = mAuth?.currentUser?.uid ?: return
        
        msubref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) return
                
                nameTv?.text = dataSnapshot.child("Name").getValue(String::class.java)
                mobileTv?.text = dataSnapshot.child("Mobile").getValue(String::class.java)
                aadhaarTv?.text = dataSnapshot.child("Aadhaar").getValue(String::class.java)
                
                val profVal = if (dataSnapshot.hasChild("Profession")) dataSnapshot.child("Profession").getValue(String::class.java) ?: ""
                              else if (dataSnapshot.hasChild("profession")) dataSnapshot.child("profession").getValue(String::class.java) ?: ""
                              else ""
                categoryTv?.text = profVal
                
                // AUTO-REPAIR: Ensure availability is synced
                val currentStatus = availabilitySwitch?.isChecked ?: false
                if (profVal.isNotEmpty()) {
                    val seekerRef = mDatabase?.child("Seeker")?.child(profVal)?.child(currentUserUid)
                    seekerRef?.child("isAvailable")?.setValue(currentStatus)
                    seekerRef?.child("Name")?.setValue(dataSnapshot.child("Name").value)
                    seekerRef?.child("Contact_Number")?.setValue(dataSnapshot.child("Contact_Number").value)
                    seekerRef?.child("City")?.setValue(dataSnapshot.child("City").value)
                    seekerRef?.child("city")?.setValue(dataSnapshot.child("City").value)
                }
                
                val experience = dataSnapshot.child("Experience").getValue(Long::class.java)
                experTv?.text = (experience ?: 0L).toString()
                headermailTv?.text = dataSnapshot.child("Email").getValue(String::class.java)
                headernameTv?.text = dataSnapshot.child("Name").getValue(String::class.java)
                
                if (dataSnapshot.hasChild("urlToImage")) {
                    val url = dataSnapshot.child("urlToImage").getValue(String::class.java)
                    Picasso.get().load(url).into(header)
                    Picasso.get().load(url).into(image)
                }
                
                if (dataSnapshot.hasChild("isAvailable")) {
                    val avail = dataSnapshot.child("isAvailable").getValue(Boolean::class.java)
                    availabilitySwitch?.isChecked = avail ?: false
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        workref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("New work")) {
                    val valL = dataSnapshot.child("New work").getValue(Long::class.java)
                    newworkTv?.text = (valL ?: 0L).toString()
                } else {
                    newworkTv?.text = "0"
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
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
        startActivity(Intent(this@SeekerMain, MainActivity::class.java))
        finish()
    }

    override fun onHeaderClicked() {
        val i = Intent(this, SeekerProfile::class.java)
        header?.let {
            val actop = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, it, ViewCompat.getTransitionName(it) ?: ""
            )
            startActivity(i, actop.toBundle())
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }
    }

    override fun onOptionClicked(position: Int, objectClicked: Any) {
        when (position) {
            2 -> startActivity(Intent(this@SeekerMain, AboutUs::class.java))
            1 -> onHeaderClicked()
        }
        mViewHolder?.mDuoDrawerLayout?.closeDrawer()
    }

    private inner class ViewHolder {
        var mDuoDrawerLayout: DuoDrawerLayout? = findViewById(R.id.drawer)
        var mDuoMenuView: DuoMenuView? = mDuoDrawerLayout?.menuView as? DuoMenuView
        var mToolbar: Toolbar? = findViewById(R.id.toolbar)
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
        
        pd?.setTitle(R.string.uploading)
        pd?.setMessage(getString(R.string.please_wait))
        pd?.show()

        val currentUserId = mAuth?.currentUser?.uid ?: return
        val filepath = mStorageRef?.child("profile_images")?.child("$currentUserId.jpg")

        filepath?.putFile(resultUri)?.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                msubref?.child("urlToImage")?.setValue(downloadUrl)
                
                msubref?.child("Profession")?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val prof = snapshot.getValue(String::class.java)
                        if (!prof.isNullOrEmpty()) {
                            mDatabase?.child("Seeker")?.child(prof)?.child(currentUserId)?.child("urlToImage")?.setValue(downloadUrl)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
                
                pd?.dismiss()
                Toast.makeText(this@SeekerMain, R.string.upload_success, Toast.LENGTH_LONG).show()
            }
        }?.addOnFailureListener { e ->
            pd?.dismiss()
            Toast.makeText(this@SeekerMain, "Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        // Empty as per original Java
    }

    companion object {
        private const val GALLERY_PICK = 1
    }
}
