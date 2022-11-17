package com.example.bookappyt

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

import java.util.*

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        // created a static method to convert timestamp to proper date format, so we can use it everywhere in project, no need to rewrite again
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            // format dd/mm/yy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        // function to get pdf size
        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            // using url we can get file and its metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata.addOnSuccessListener { storageMetaData ->
                Log.d(TAG, "loadPdfSize: got metadata")
                val bytes = storageMetaData.sizeBytes.toDouble()
                Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                // convert bytes to KB/MB
                val kb = bytes / 1024
                val mb = kb / 1024
                if (mb >= 1) {
                    sizeTv.text = "${String.format("%.2f", mb)} MB"
                } else if (kb >= 1) {
                    sizeTv.text = "${String.format("%.2f", kb)} KB"
                } else {
                    sizeTv.text = "${
                        String.format("%.2f", bytes)
                    } bytes"

                }

            }.addOnFailureListener { e ->
                // failed to get metadata
                Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
            }

        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            // load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get category
                    val category = "${snapshot.child("category").value}"

                    // set category
                    categoryTv.text = category
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        /*instead of making new function loadPdfPageCount() to just load pages count it would be more good to use some existing function to do that
        * i.e. loadPdfFromUrlSinglePage
        * We will add another parameter of type TextView e.g. pageTv
        * whenever we call that function
        * 1) if we require page numbers we will pass pagesTv(TextView)
        * 2)if we don't require page number we will pass null
        * and in function if pagesTv(TextView parameter is not null we will set the page number count*/

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_THUMBNAIL_TAG"

            // using url we can get file and its metadata from firebase storage
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF).addOnSuccessListener { bytes ->

                Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                // set to pdfview
                pdfView.fromBytes(bytes)
                    .pages(0) // show first page only
                    .spacing(0)
                    .swipeHorizontal(false)
                    .enableSwipe(false)
                    .onError { t ->
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "loadPdfFromSinglePage: ${t.message}")
                    }.onPageError { page, t ->
                        progressBar.visibility = View.INVISIBLE
                        Log.d(TAG, "loadPdfFromSinglePage: ${t.message}")
                    }
                    .onLoad { nbPages ->
                        Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")
                        // pdf loaded, we can set page count, pdf thumbnail
                        progressBar.visibility = View.INVISIBLE

                        // if pagesTv param is not null then set page numbers
                        if (pagesTv != null) {
                            pagesTv.text = "$nbPages"
                        }
                    }
                    .load()
            }.addOnFailureListener { e ->
                // failed to get metadata
                Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
            }
        }
    }


}