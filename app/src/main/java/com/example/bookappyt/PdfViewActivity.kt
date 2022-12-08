package com.example.bookappyt

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import com.example.bookappyt.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pdftron.pdf.config.ViewerConfig
import com.pdftron.pdf.controls.DocumentActivity
import java.io.File

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
        loadBookDetails(this)

        // handle click, goback
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails(context: Context) {
        Log.d(TAG, "loadBookDetails: Get Pdf URL from db")
        // Database reference to get book details e.g. get book url using book id
        // Step (1) Get Book Url using Book Id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")

                    // Step (2) load pdf using url from firebase storage
                    //loadBookFromUrl("$pdfUrl")
                    //val pdfUrl2 = "https://pdftron.s3.amazonaws.com/downloads/pl/PDFTRON_mobile_about.pdf"
                    openHttpDocument(context, "$pdfUrl")

                    // Open our sample document in the 'res/raw' resource folder
                    //openRawResourceDocument(context, R.raw.sample)
                    //finish()
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


    /**
     * Open a local document given a path
     *
     * @param context the context to start the document reader
     * @param localFilePath local path to a document
     */
    private fun openLocalDocument(context: Context, localFilePath: String) {
        val localFile = Uri.fromFile(File(localFilePath))
        presentDocument(localFile)
    }

    /**
     * Open a document given a Content Uri
     *
     * @param context the context to start the document reader
     * @param contentUri a content URI that references a document
     */
    private fun openContentUriDocument(context: Context, contentUri: Uri) {
        presentDocument(contentUri)
    }

    /**
     * Open a document from an HTTP/HTTPS url
     *
     * @param context the context to start the document reader
     * @param url an HTTP/HTTPS url to a document
     */
    private fun openHttpDocument(context: Context, url: String) {
        val config = ViewerConfig.Builder().openUrlCachePath(this.getCacheDir().getAbsolutePath()).build()
        val fileLink = Uri.parse(url)
        presentDocument(fileLink, config)
    }

    /**
     *
     * @param context the context to start the document reader
     * @param fileResId resource id to a document in res/raw
     */
    private fun openRawResourceDocument(context: Context, @RawRes fileResId: Int) {
        val intent =
            DocumentActivity.IntentBuilder.fromActivityClass(this, DocumentActivity::class.java)
                .withFileRes(fileResId)
                .usingNewUi(true)
                .build()
        startActivity(intent)
    }

    private fun presentDocument(uri: Uri) {
        presentDocument(uri, null)
    }

    private fun presentDocument(uri: Uri, config: ViewerConfig?) {
        var config = config
        if (config == null) {
            config = ViewerConfig.Builder().saveCopyExportPath(this.cacheDir.absolutePath).build()
        }
        val intent =
            DocumentActivity.IntentBuilder.fromActivityClass(this, DocumentActivity::class.java)
                .withUri(uri)
                .usingConfig(config)
                .usingNewUi(true)
                .build()
        startActivity(intent)
    }
}