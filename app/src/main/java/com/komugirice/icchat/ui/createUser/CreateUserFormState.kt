package com.komugirice.icchat.ui.createUser

data class CreateUserFormState (
    val userNameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val passwordConfirmError: Int? = null,
    val isDataValid: Boolean = false
)