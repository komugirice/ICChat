package com.komugirice.icchat.ui.createUser

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.extension.isDateStr
import com.komugirice.icchat.extension.toDate
import com.komugirice.icchat.R
import com.komugirice.icchat.ui.login.LoginFormState
import java.util.regex.Pattern


class CreateUserViewModel  : ViewModel() {

    private val _createUserForm = MutableLiveData<CreateUserFormState>()
    val createUserFormState: LiveData<CreateUserFormState> = _createUserForm

    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val birthDay = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val passwordConfirm = MutableLiveData<String>()
    val canSubmit = MediatorLiveData<Boolean>().also { result->
        result.addSource(name) {result.value = canSubmit()}
        result.addSource(email) {result.value = canSubmit()}
        result.addSource(birthDay) {result.value = canSubmit()}
        result.addSource(password) {result.value = canSubmit()}
        result.addSource(passwordConfirm) {result.value = canSubmit()}
    }

    private fun canSubmit(): Boolean {
        if(!isEmptyValid(name.value)) {
            _createUserForm.value = CreateUserFormState(userNameError = R.string.invalid_userName)
            return false
        } else if (!isEmptyValid(email.value)) {
            _createUserForm.value = CreateUserFormState(emailError = R.string.invalid_email)
            return false
        } else if (!isMailaddress(email.value)) {
            _createUserForm.value = CreateUserFormState(emailError = R.string.invalid_pattern_email)
            return false
        } else if (!isValidDate(birthDay.value)) {
            _createUserForm.value = CreateUserFormState(birthDayError = R.string.invalid_birthDay)
            return false
        } else if (!isPasswordValid(password.value)) {
            _createUserForm.value = CreateUserFormState(passwordError = R.string.invalid_password_create_user)
            return false
        } else if (!isPasswordValid(passwordConfirm.value)) {
            _createUserForm.value = CreateUserFormState(passwordConfirmError = R.string.invalid_password_create_user)
            return false
        } else if (!isMatchPassword(password.value, passwordConfirm.value)) {
            _createUserForm.value = CreateUserFormState(passwordConfirmError = R.string.invalid_match_password)
            return false
        }
        _createUserForm.value = CreateUserFormState(isDataValid = true)
        return true
    }

    // MediatorLiveDataの使用前
//    fun createUserDataChanged(userName: String, email: String, birthDay: String, password: String, passwordConfirm: String) {
//        if (!isEmptyValid(userName)) {
//            _createUserForm.value = CreateUserFormState(userNameError = R.string.invalid_userName)
//        } else if (!isEmptyValid(email)) {
//            _createUserForm.value = CreateUserFormState(emailError = R.string.invalid_email)
//        } else if (!isMailaddress(email)) {
//            _createUserForm.value = CreateUserFormState(emailError = R.string.invalid_pattern_email)
//        } else if (!isValidDate(birthDay)) {
//            _createUserForm.value = CreateUserFormState(birthDayError = R.string.invalid_birthDay)
//        } else if (!isPasswordValid(password)) {
//            _createUserForm.value = CreateUserFormState(passwordError = R.string.invalid_password_create_user)
//        } else if (!isPasswordValid(passwordConfirm)) {
//            _createUserForm.value = CreateUserFormState(passwordConfirmError = R.string.invalid_password_create_user)
//        } else if (!isMatchPassword(password, passwordConfirm)) {
//            _createUserForm.value = CreateUserFormState(passwordConfirmError = R.string.invalid_match_password)
//        } else {
//            _createUserForm.value = CreateUserFormState(isDataValid = true)
//        }
//    }

    private fun isEmptyValid(string: String?): Boolean {
        return string?.isNotBlank() == true
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
    fun isMailaddress(mailaddress: String?): Boolean {
        if (mailaddress == null) return false
        return Patterns.EMAIL_ADDRESS.matcher(mailaddress).matches()
    }

    /**
     * 日付形式チェック.
     *
     * @param birthDay
     * 誕生日
     * @return 日付形式の場合はtrue
     */
    fun isValidDate(birthDay: String?): Boolean {
        if(birthDay == null) {
            return true
        } else if(birthDay.toDate() == null
            // SimpleDateFormatの末尾英字対応
            || !birthDay.isDateStr()) {
            return false
        }
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