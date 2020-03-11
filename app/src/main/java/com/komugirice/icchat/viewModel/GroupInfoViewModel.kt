package com.komugirice.icchat.viewModel

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.GroupInfoActivity
import com.komugirice.icchat.firebase.firestore.model.GroupRequests
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.store.RequestStore

class GroupInfoViewModel  : ViewModel() {
    val groupRequests = MutableLiveData<GroupRequests>()

    fun initRoom(intent: Intent): Boolean {
        intent.getSerializableExtra(GroupInfoActivity.KEY_ROOM).also {
            if (it is Room && it.documentId.isNotEmpty()) {
                RequestStore.getGroupRequests(it){
                    groupRequests.value = it
                }
                return true
            }
            return false
        }

    }
}