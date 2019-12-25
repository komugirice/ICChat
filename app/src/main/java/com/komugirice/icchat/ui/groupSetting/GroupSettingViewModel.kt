package com.komugirice.icchat.ui.groupSetting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.R
import com.komugirice.icchat.firestore.model.User


class GroupSettingViewModel  : ViewModel() {

    private val _groupSettingForm = MutableLiveData<GroupSettingFormState>()
    val groupSettingFormState: LiveData<GroupSettingFormState> = _groupSettingForm

    val name = MutableLiveData<String>()
    val _inviteUser = mutableListOf<User>()
    val inviteUser = MutableLiveData<List<User>>()

    val canSubmit = MediatorLiveData<Boolean>().also { result->
        result.addSource(name) {result.value = canSubmit()}
        result.addSource(inviteUser) {result.value = canSubmit()}
    }

    private fun canSubmit(): Boolean {
        if(!isEmptyValid(name.value)) {
            _groupSettingForm.value = GroupSettingFormState(groupNameError = R.string.invalid_groupName)
            return false
        } else if (!isGroupNameLengthValid(name.value)) {
            _groupSettingForm.value = GroupSettingFormState(groupNameError = R.string.invalid_length_groupName)
            return false
        } else if (!hasTwoInviteUser(inviteUser.value)) {
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

    private fun hasTwoInviteUser(inviteUser: List<User>?): Boolean {
        if (inviteUser == null) return false
        return inviteUser.size > 1
    }

}