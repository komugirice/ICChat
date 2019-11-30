package com.komugirice.icchat.data

import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.getIdFromEmail
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.R
import com.komugirice.icchat.data.model.LoggedInUser
import com.komugirice.icchat.ui.login.LoggedInUserView
import com.komugirice.icchat.ui.login.LoginResult
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(userId: String, password: String, _loginResult: MutableLiveData<LoginResult>) {
        try {
            // TODO: handle loggedInUser authentication
            val convEmailType = userId + "@example.com"
            var user: LoggedInUser? = null
            FirebaseAuth.getInstance().signInWithEmailAndPassword(convEmailType, password)
                .addOnCompleteListener {
                    user = updateView()
                    if(user != null)
                        _loginResult.value =
                            LoginResult(success = LoggedInUserView(userId = userId, displayName = user?.displayName ?: ""))
                    else {
                        _loginResult.value = LoginResult(error = R.string.login_failed)
                    }
                }
        } catch (e: IOException) {
            // TODO Exceptionをcatchしない。.addOnCompleteListener{の中だから？
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
    }

    private fun updateView(): LoggedInUser? {
        var user = FirebaseAuth.getInstance().currentUser
        user?.also {
        } ?: run {
            //throw IOException()
            return null
        }
        // TODO FirebaseAuthからユーザ名取得する
        return LoggedInUser(user.email?.also{it.getIdFromEmail()} ?: "", "Dummy")
    }

    fun logout() {
        // TODO: revoke authentication
        FirebaseAuth.getInstance().signOut()
    }
}

