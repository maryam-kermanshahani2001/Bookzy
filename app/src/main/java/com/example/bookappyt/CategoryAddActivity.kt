package com.example.bookappyt

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.bookappyt.databinding.ActivityCategoryAddBinding
import com.example.bookappyt.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseRegistrar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class CategoryAddActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityCategoryAddBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle click, go back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // handle click, begn upload category
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var category = ""
    private fun validateData() {
        /*Before adding validate data*/
        // get data
        category = binding.categoryEt.text.toString().trim()

        // validate if not empty
        if (category.isEmpty()) {
            Toast.makeText(this, "Please enter category...!", Toast.LENGTH_SHORT).show()

        } else {
            addCategoryFirebase()
        }


    }

    private fun addCategoryFirebase() {
        // show progress
        progressDialog.setMessage("Adding category...")
        progressDialog.show()

        // get timestamp
        val timestamp = System.currentTimeMillis()

        // setup data to add in firebase db
        val hashMap: HashMap<String, Any?> = HashMap()

        hashMap["id"] = "$timestamp"
        hashMap["category"] = "$category"
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        // add to firebase db .... Database Root > Categories > categoryId > category info
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp").setValue(hashMap).addOnSuccessListener {
            // category add success
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Category added successfully...",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener { e ->
            // category add failed
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Failed to add dui to ${e.message}",
                Toast.LENGTH_SHORT
            ).show()

        }


    }
}