package com.example.bookappyt

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappyt.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardAdminActivity : AppCompatActivity() {
    // view binding
    private lateinit var binding: ActivityDashboardAdminBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // arraylist to hold categories
    private lateinit var categoryArraylist: ArrayList<ModelCategory>

    // adapter
    private lateinit var adapterCategory: AdapterCategory

    private val player = MediaPlayer()
    private val afd = assets.openFd("/raw/Autumn")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()
        loadCategories()

        // search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterCategory.filter.filter(s)

                } catch (e: Exception) {

                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        // handle click, log out
        binding.logoutBtn.setOnClickListener {

            firebaseAuth.signOut()
            checkUser()
        }


        binding.playBtn.setOnClickListener {
            if (!player.isPlaying) {
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                player.prepare()
                player.start()
            } else {
                player.stop()
            }
        }

        // handle click start category add screen
        /* binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardAdminActivity.this, CategoryAddActivity.class))
            }
         });*/

        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }


        // handle click, start add pdf page
        binding.addPdfFab.setOnClickListener{
            startActivity(Intent(this,PdfAddActivity::class.java))

        }



    }

    private fun loadCategories() {
        // init arraylist
        categoryArraylist = ArrayList()

        // get all categories from firebase database... Firebase db < categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before starting adding data into it
                categoryArraylist.clear()
                for (ds in snapshot.children) {
                    // get data as model
                    val model = ds.getValue(ModelCategory::class.java)
                    // add to arraylist
                    categoryArraylist.add(model!!)
                }

                // setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArraylist)

                // set adapter to recyclerview
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkUser() {
        /// get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // logged in, get and show user info
            val email = firebaseUser.email

            // set to textview of toolbar
            binding.subTitleTv.text = email

        }
    }
}