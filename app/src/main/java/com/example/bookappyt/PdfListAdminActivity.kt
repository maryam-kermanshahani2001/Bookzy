package com.example.bookappyt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.bookappyt.databinding.ActivityPdfListAdminBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    // category id, title
    private var categoryId = ""
    private var category = ""

    //arraylist to hold books
    private lateinit var pdfArrayList: ArrayList<ModelPdf>

    //adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get from intent, that we passed from adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        // set pdf category
        binding.subtitleTv.text = category

        // load pdf/books
        loadPdfList()

        // handle click, goback
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // filter data
                try {

                    adapterPdfAdmin.filter!!.filter(s)
                }
                catch (e:Exception) {
                    Log.d(TAG, "onTextChanged: ${e.message}")

                }
            }

                override fun afterTextChanged(s: Editable?) {
                    TODO("Not yet implemented")
                }
            })
        }

                private fun loadPdfList() {
            // init arraylist
            pdfArrayList = ArrayList()

            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // clear list before start adding data into it
                        pdfArrayList.clear()
                        for (ds in snapshot.children) {
                            // get data
                            val model = ds.getValue(ModelPdf::class.java)

                            // add to list
                            if (model != null) {
                                pdfArrayList.add(model)
                                Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")

                            }
                        }

                        //setup adapter
                        adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity, pdfArrayList)
                        binding.booksRv.adapter = adapterPdfAdmin
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

        }
    }