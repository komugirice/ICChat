package com.komugirice.icchat.ui.login

/**
 * Authentication userId : success (user details) or error message.
 */
data class LoginResult(
    val success: LoggedInUserView? = null,
    val error: Int? = null
)
