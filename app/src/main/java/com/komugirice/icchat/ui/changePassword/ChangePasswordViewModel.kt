package com.komugirice.icchat.ui.changePassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import com.komugirice.icchat.R

class ChangePasswordViewModel  : ViewModel() {

    private val _changePasswordForm = MutableLiveData<ChangePasswordFormState>()
    val changePasswordFormState: LiveData<ChangePasswordFormState> = _changePasswordForm

    val newPassword = MutableLiveData<String>()
    val newPasswordConfirm = MutableLiveData<String>()
    val canSubmit = MediatorLiveData<Boolean>().also { result ->
        result.addSource(newPassword) { result.value = canSubmit() }
        result.addSource(newPasswordConfirm) { result.value = canSubmit() }
    }

    private fun canSubmit(): Boolean {
        if (!isPasswordValid(newPassword.value)) {
            _changePasswordForm.value =
                ChangePasswordFormState(newPasswordError =
                    applicationContext.getString(
                        R.string.invalid_input,
                        applicationContext.getString(R.string.new_password_label)
                    )
                )
            return false
        } else if (!isPasswordValid(newPasswordConfirm.value)) {
            _changePasswordForm.value =
                ChangePasswordFormState(
                    newPasswordConfirmError =
                    applicationContext.getString(
                        R.string.invalid_input,
                        applicationContext.getString(R.string.new_password_confirm_label)
                    )
                )
            return false
        } else if (!isMatchPassword(newPassword.value, newPasswordConfirm.value)) {
            _changePasswordForm.value =
                ChangePasswordFormState(newPasswordConfirmError =
                    applicationContext.getString(
                        R.string.invalid_match_input,
                        applicationContext.getString(R.string.new_password_confirm_label)
                    )
                )
            return false
        }
        _changePasswordForm.value = ChangePasswordFormState(isDataValid = true)
        return true
    }


    private fun isPasswordValid(password: String?): Boolean {
        if (password == null) return false
        return password.length > 5 && password.length < 65
    }


    private fun isMatchPassword(password: String?, passwordConfirm: String?) =
        password != null && passwordConfirm != null &&
                password == passwordConfirm
}