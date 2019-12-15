package com.komugirice.icchat.ui.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val email: String,
    val displayName: String
    //... other data fields that may be accessible to the UI
)
