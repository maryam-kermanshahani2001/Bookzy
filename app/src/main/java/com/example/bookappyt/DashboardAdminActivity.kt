package com.example.bookappyt

import android.R.layout.simple_spinner_item
import android.R.layout.simple_spinner_dropdown_item
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappyt.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardAdminActivity : AppCompatActivity() {
    // name of the chosen song
    private var songName: String = "raw/autumn"

    // view binding
    private lateinit var binding: ActivityDashboardAdminBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // arraylist to hold categories
    private lateinit var categoryArraylist: ArrayList<ModelCategory>

    // adapter
    private lateinit var adapterCategory: AdapterCategory

    // media player
    var mMediaPlayer: MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)

        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()
        loadCategories()

        val arraySpinner = arrayOf(
            "Autumn", "Norwegian_Wood", "Moonswept"
        )

        val adapter = ArrayAdapter(
            this,
            simple_spinner_item, arraySpinner
        )
        adapter.setDropDownViewResource(simple_spinner_dropdown_item)
        binding.spinnerSong.adapter = adapter
        binding.spinnerSong.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                Log.d("MEDIA PLAYER", "set song: ${arraySpinner[position]}")
                songName = "raw/" + arraySpinner[position].lowercase()
                stopSound()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


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
            if (mMediaPlayer == null || !mMediaPlayer!!.isPlaying) {
                playSound()
            } else {
                pauseSound()
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

    // 1. Plays the water sound
    @SuppressLint("DiscouragedApi")
    fun playSound() {
        if (mMediaPlayer == null) {
            Log.d("MEDIA PLAYER", "song to play: $songName")
            mMediaPlayer = MediaPlayer.create(this, this.resources.getIdentifier(songName, "id", this.packageName))
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    // 2. Pause playback
    fun pauseSound() {
        if (mMediaPlayer?.isPlaying == true) mMediaPlayer?.pause()
    }

    // 3. Stops playback
    fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    // 4. Destroys the MediaPlayer instance when the app is closed
    //override fun onStop() {
    //    super.onStop()
    //    if (mMediaPlayer != null) {
    //        mMediaPlayer!!.release()
    //        mMediaPlayer = null
    //    }
    //}

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