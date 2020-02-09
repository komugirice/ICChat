package com.komugirice.icchat.ui.changePassword

data class ChangePasswordFormState (
    val newPasswordError: String? = null,
    val newPasswordConfirmError: String? = null,
    val isDataValid: Boolean = false
)