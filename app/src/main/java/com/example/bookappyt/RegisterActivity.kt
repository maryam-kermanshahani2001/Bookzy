package com.example.bookappyt

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookappyt.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityRegisterBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
//        setContentView(R.layout.activity_register)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // init progress dialog, will show while creating account | Register User
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle back button click

        binding.backBtn.setOnClickListener {
            onBackPressed() // goto previous screen
        }

        // handle click, begin register
        binding.registerBtn.setOnClickListener {
            /*Steps
            * Input data
            * Validate data
            * Create account - Firebase Auth
            * Save user info - firebase realtime database*/
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""
    private fun validateData() {
        // 1) Input data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        // 2) validate data
        if (name.isEmpty())
        // empty name...
            Toast.makeText(this, "Enter your name ...", Toast.LENGTH_SHORT).show()
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // invalid email pattern
            Toast.makeText(this, "Invalid Email Pattern ...", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            // empty password
            Toast.makeText(this, "Enter password ...", Toast.LENGTH_SHORT).show()
        } else if (cPassword.isEmpty()) {
            // empty password
            Toast.makeText(this, "Confirm password ...", Toast.LENGTH_SHORT).show()
        } else if (password != cPassword) {
            Toast.makeText(this, "Password doesn't match ...", Toast.LENGTH_SHORT).show()
        } else {
            createUserAccount()
        }

    }

    private fun createUserAccount() {
        // Create account - firebase auth
        // show pregress
        progressDialog.setMessage("Creating Account")
        progressDialog.show()

        // create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // account created, now add user info in db
                updateUserInfo()

            }
            .addOnFailureListener { e ->
                // failed creating account
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Failed creating account due to ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun updateUserInfo() {
        // save user info - Firebase realtime databse
        progressDialog.setMessage("Saving user info...")
        val timestamp = System.currentTimeMillis()

        // get current user uid, since user is registered so we can get it now
        val uid = firebaseAuth.uid
        //setup date to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = "" // add empty, will do in profile edit
        hashMap["userType"] =
            "user" // possible values are user/admin, will change value to admin manually on firebase db
        hashMap["timestamp"] = timestamp

        // set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!).setValue(hashMap).addOnSuccessListener {
            // user info saved, open user dashboard
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Account created...",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
            finish()

        }.addOnFailureListener { e ->
            // failed creating account
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Failed saving user information due to ${e.message}",
                Toast.LENGTH_SHORT
            ).show()

        }


    }
}