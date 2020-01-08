package com.komugirice.icchat.viewModel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.GroupInfoActivity
import com.komugirice.icchat.firebase.firestore.model.Room

class GroupInfoViewModel  : ViewModel() {
    val room = MutableLiveData<Room>()

    fun initRoom(intent: Intent): Boolean {
        intent.getSerializableExtra(GroupInfoActivity.KEY_ROOM).also {
            if (it is Room && it.documentId.isNotEmpty()) {
                this.room.postValue(it)
                return true
            }
            return false
        }

    }
}