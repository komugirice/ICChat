package com.komugirice.icchat.data.model

import com.komugirice.icchat.firebase.firestore.model.Interest
import java.io.Serializable

open class OgpData: Serializable{
    var ogpUrl: String? = null
    var ogpTitle: String? = null
    var ogpImageUrl: String? = null
    var ogpDescription: String? = null

    constructor()
    constructor(ogpData: OgpData) {
        this.ogpUrl = ogpData.ogpUrl
        this.ogpTitle = ogpData.ogpTitle
        this.ogpImageUrl = ogpData.ogpImageUrl
        this.ogpDescription = ogpData.ogpDescription
    }

    constructor(interest: Interest) {
        this.ogpUrl = interest.ogpUrl
        this.ogpTitle = interest.ogpTitle
        this.ogpImageUrl = interest.ogpImageUrl
        this.ogpDescription = interest.ogpDescription
    }

}