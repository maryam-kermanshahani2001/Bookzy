package com.example.bookappyt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.bookappyt.databinding.ActivityPdfViewBinding
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {
    // View binding
    private lateinit var binding: ActivityPdfViewBinding

    // book id
    private var bookId = ""

    //TAG
    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get book id back
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        // handle click, goback
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get Pdf URL from db")
        // Database reference to get book details e.g. get book url using book id
        // Step (1) Get Book Url using Book Id
        val ref = FirebaseDatabase.getInstance().getReference("books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")

                    // Step (2) load pdf using url from firebase storage
                    loadBookFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadBookFromUrl: Get Pdf from firebase storage using URL")
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes ->
                Log.d(TAG, "loadBookFromUrl: pdf got from url")

                // load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange{page, pageCount ->
                        // set current and total pages in toolbar subtitle
                        val currentPage = page + 1
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError {t ->
                        Log.d(TAG, "loadBookFormUrl: ${t.message}")
                    }
                    .onPageError {_ , t ->
                        Log.d(TAG, "loadBookFormUrl: ${t.message}")
                    }.load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "loadBookFromUrl: Failed to get due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}