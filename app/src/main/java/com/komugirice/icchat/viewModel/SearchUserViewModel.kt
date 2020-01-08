package com.komugirice.icchat.viewModel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.firebase.firestore.model.User

class SearchUserViewModel  : ViewModel() {

    val searchEditText = MutableLiveData<String>()
    val _requestUser = mutableListOf<User>()
    val requestUser = MutableLiveData<List<User>>()

    val canSubmit = MediatorLiveData<Boolean>().also { result->
        result.addSource(requestUser) {result.value = canSubmit()}
    }

    private fun canSubmit(): Boolean {
        if(_requestUser.size == 0) return false
        return true
    }
}