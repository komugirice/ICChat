package com.komugirice.icchat.ui.groupSetting

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.GroupInfoActivity
import com.komugirice.icchat.R
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.model.User


class GroupSettingViewModel  : ViewModel() {

    val room = MutableLiveData<Room>()

    private val _groupSettingForm = MutableLiveData<GroupSettingFormState>()
    val groupSettingFormState: LiveData<GroupSettingFormState> = _groupSettingForm

    val name = MutableLiveData<String>()
    val _requestUser = mutableListOf<User>()
    val requestUser = MutableLiveData<List<User>>()

    val canSubmit = MediatorLiveData<Boolean>().also { result->
        result.addSource(name) {result.value = canSubmit()}
        result.addSource(requestUser) {result.value = canSubmit()}
    }

    fun initRoom(intent: Intent): Boolean {
        intent.getSerializableExtra(GroupInfoActivity.KEY_ROOM).also {
            if (it is Room && it.documentId.isNotEmpty()) {
                this.room.postValue(it)
                return true
            }
            return false
        }

    }

    private fun canSubmit(): Boolean {
        if(!isEmptyValid(name.value)) {
            _groupSettingForm.value = GroupSettingFormState(groupNameError = R.string.invalid_groupName)
            return false
        } else if (!isGroupNameLengthValid(name.value)) {
            _groupSettingForm.value = GroupSettingFormState(groupNameError = R.string.invalid_length_groupName)
            return false
        } else if (!hasTwoInviteUser(requestUser.value)) {
            return false
        }
        _groupSettingForm.value = GroupSettingFormState(isDataValid = true)
        return true
    }


    private fun isEmptyValid(string: String?): Boolean {
        return string?.isNotBlank() == true
    }

    private fun isGroupNameLengthValid(string: String?): Boolean {
        if (string == null) return false
        return string.length <= 20
    }

    private fun hasTwoInviteUser(requestUser: List<User>?): Boolean {
        if (requestUser == null) return false
        return requestUser.size > 0
    }

}