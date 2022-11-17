package com.example.bookappyt

import android.widget.Filter

/*Used to filter data from recyclerview | search pdf from pdf list in recyclerview*/

class FilterPdfAdmin : Filter {
    // arraylist in which we want to search
    var filterList: ArrayList<ModelPdf>

    // adapter in which filter need to be implemented
    var adapterPdfAdmin: AdapterPdfAdmin

    // constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) : super() {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint // value to search
        val results = FilterResults()

        // value to be searched should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()) {
            // change to upper case or lower case to avoid case sensitivity
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for (i in filterList.indices) {
                // validate if match
                if (filterList[i].title.lowercase().contains(constraint)) {
                    // searched value is similar to value in list, add to filtered list
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            // searched value is either null or empty, return all data
            results.count = filterList.size
            results.values = filterList
        }
        return results // don't muss
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        // apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        // notify change
        adapterPdfAdmin.notifyDataSetChanged()
    }
}