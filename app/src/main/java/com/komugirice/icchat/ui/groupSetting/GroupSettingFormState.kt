package com.komugirice.icchat.ui.groupSetting

data class GroupSettingFormState (
    val groupNameError: Int? = null,
    val inviteUserError: Int? = null,
    val isDataValid: Boolean = false
)
