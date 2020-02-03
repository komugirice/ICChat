package com.komugirice.icchat.viewModel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.data.model.OgpData
import com.komugirice.icchat.firebase.firestore.model.Interest

class InputInterestViewModel: ViewModel() {
    var interestData = Interest()
    var isUpdateMode = false
    var ogpData: OgpData? = null
    var imageUri: Uri? = null
    var isImageUpdate = false
}