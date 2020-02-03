package com.komugirice.icchat.firebase.firestore.model

import com.komugirice.icchat.data.model.OgpData
import com.komugirice.icchat.enums.MessageType
import java.io.Serializable
import java.util.*

class Interest : OgpData, Serializable {
    var documentId: String = ""
    var comment: String? = null
    var image: String? = null
    var isOgp = false
    var createdAt: Date = Date()

    constructor()
    constructor(ogpData: OgpData): super(ogpData)

    fun setOgpData(ogpData: OgpData) {
        this.ogpUrl = ogpData.ogpUrl
        this.ogpTitle = ogpData.ogpTitle
        this.ogpImageUrl = ogpData.ogpImageUrl
        this.ogpDescription = ogpData.ogpDescription
    }
}