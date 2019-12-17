package com.komugirice.icchat.ui.createUser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.qiitaapplication.extension.isDateStr
import com.example.qiitaapplication.extension.toDate
import com.komugirice.icchat.R
import com.komugirice.icchat.ui.login.LoginFormState
import java.util.regex.Pattern


class CreateUserViewModel  : ViewModel() {

    private val _createUserForm = MutableLiveData<CreateUserFormState>()
    val createUserFormState: LiveData<CreateUserFormState> = _createUserForm

    fun createUserDataChanged(userName: String, email: String, birthDay: String, password: String, passwordConfirm: String) {
        if (!isEmptyValid(userName)) {
            _createUserForm.value = CreateUserFormState(userNameError = R.string.invalid_userName)
        } else if (!isEmptyValid(email)) {
            _createUserForm.value = CreateUserFormState(emailError = R.string.invalid_email)
        } else if (!isMailaddress(email)) {
            _createUserForm.value = CreateUserFormState(emailError = R.string.invalid_pattern_email)
        } else if (!isValidDate(birthDay)) {
            _createUserForm.value = CreateUserFormState(birthDayError = R.string.invalid_birthDay)
        } else if (!isPasswordValid(password)) {
            _createUserForm.value = CreateUserFormState(passwordError = R.string.invalid_password_create_user)
        } else if (!isPasswordValid(passwordConfirm)) {
            _createUserForm.value = CreateUserFormState(passwordConfirmError = R.string.invalid_password_create_user)
        } else if (!isMatchPassword(password, passwordConfirm)) {
            _createUserForm.value = CreateUserFormState(passwordConfirmError = R.string.invalid_match_password)
        } else {
            _createUserForm.value = CreateUserFormState(isDataValid = true)
        }
    }

    private fun isEmptyValid(string: String): Boolean {
        return string.isNotBlank()
    }

    /**
     * メールアドレス形式チェック.
     *
     *
     * 形式のみのチェックなので、ドメイン存在チェックは行っていません.
     *
     *
     * @param mailaddress
     * メールアドレス
     * @return メールアドレス形式の場合はtrue
     */
    fun isMailaddress(mailaddress: String): Boolean {
        val PATTERN_MAIL_ADDRESS: Pattern = Pattern
            .compile("^[a-zA-Z0-9!$&*.=^`|~#%\\\'+\\/?_{}-]+@([a-zA-Z0-9_-]+\\.)+[a-zA-Z]+$")
        if (PATTERN_MAIL_ADDRESS.matcher(mailaddress).matches()) {
            return true
        }
        return false
    }

    /**
     * 日付形式チェック.
     *
     * @param birthDay
     * 誕生日
     * @return 日付形式の場合はtrue
     */
    fun isValidDate(birthDay: String): Boolean {
        if(birthDay.isEmpty()) {
            return true
        } else if(birthDay.toDate() == null
            // SimpleDateFormatの末尾英字対応
            || !birthDay.isDateStr()) {
            return false
        }
        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5 && password.length < 65
    }

    private fun isMatchPassword(password: String, passwordConfirm: String) = password == passwordConfirm
}