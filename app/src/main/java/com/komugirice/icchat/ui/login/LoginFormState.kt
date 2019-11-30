package com.komugirice.icchat.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val userIdError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)
