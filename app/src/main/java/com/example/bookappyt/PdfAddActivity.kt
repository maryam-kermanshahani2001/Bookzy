package com.example.bookappyt

import android.app.AlertDialog
import android.app.Application.ActivityLifecycleCallbacks
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.ColorSpace.Model
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bookappyt.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    // setup view binding activity_pdf_add --> ActivityPdfAddBinding
    private lateinit var binding: ActivityPdfAddBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // progress dialog (show while uploading pdf)
    private lateinit var progressDialog: ProgressDialog

    // arraylist to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    // uri of picked pdf
    private var pdfUri: Uri? = null

    // TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        // setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle click, go back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        // handle click, show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        // handle click, pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        // handle click, start uploading pdf/book
        binding.submitBtn.setOnClickListener {
            // ste1: validate data
            // step2: upload pdf to firebase storage
            // step2: get url of uploaded pdf
            // step4 upload pdf info to firebase db

            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        // validate data
        Log.d(TAG, "validateData: validating data")

        // get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        // validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category...", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null) {
            Toast.makeText(this, "Enter PDF...", Toast.LENGTH_SHORT).show()


        } else {
            // data validated, begin upload
            uploadPdfToStorage()
        }

    }

    private fun uploadPdfToStorage() {
        // step2: upload data to firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...")

        // show progress dialog
        progressDialog.setMessage("Uploading PDF")
        progressDialog.show()

        // timestamp
        val timestamp = System.currentTimeMillis()

        // path of pdf ifn firebase storage
        val filePathAndName = "Books/$timestamp"

        // storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!).addOnSuccessListener { taskSnapshot ->
            Log.d(
                TAG, "uploadPdfToStorage: PDF uploaded now getting url..."
            )
            // step3: get url of uploaded pdf
            val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val uploadedPdfUrl = "${uriTask.result}"

            uploadPdfInfoToDb(uploadedPdfUrl, timestamp)

        }.addOnFailureListener { e ->
            Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // step4: upload pdf info to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info...")

        // uid of current user
        val uid = firebaseAuth.uid

        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewCount"] = 0
        hashMap["downloadsCount"] = 0

        //
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp").setValue(hashMap)
            .addOnSuccessListener {

                Log.d(TAG, "uploadPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "uploaded...", Toast.LENGTH_SHORT)
                    .show()
                pdfUri = null

            }.addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories")
        //init arraylist
        categoryArrayList = ArrayList()
        // db reference to load categories DF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before adding data
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    // get data
                    val model = ds.getValue(ModelCategory::class.java)

                    // add to arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        // get string array of categories from arraylsit
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        // alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { dialog, which ->
                // handle item click
                // get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                // set category to textview
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")

            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()

            }
        }
    )


}