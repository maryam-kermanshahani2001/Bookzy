package com.example.bookappyt

class ModelPdf {
    // variables
    var uid:String = ""
    var id:String = ""
    var title:String = ""
    var description:String = ""
    var categoryId:String = ""
    var url:String = ""
    var timestamp:Long =0
    var viewCount:Long =0
    var downloadsCount:Long = 0

    // empty constructor (required by firebase)
    constructor()

    // parameterized constructor
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        timestamp: Long,
        viewCount: Long,
        downloadsCount: Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.timestamp = timestamp
        this.viewCount = viewCount
        this.downloadsCount = downloadsCount
    }



}