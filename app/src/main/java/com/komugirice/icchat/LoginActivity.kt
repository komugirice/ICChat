package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import com.komugirice.icchat.ICChatApplication.Companion.isFacebookAuth
import com.komugirice.icchat.ICChatApplication.Companion.isGoogleAuth
import com.komugirice.icchat.extension.afterTextChanged
import com.komugirice.icchat.extension.getDomainFromEmail
import com.komugirice.icchat.extension.getIdFromEmail
import com.komugirice.icchat.extension.loggingSize
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.ui.login.LoginViewModel
import com.komugirice.icchat.ui.login.LoginViewModelFactory
import com.komugirice.icchat.util.FcmUtil
import com.komugirice.icchat.util.Prefs
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.container
import timber.log.Timber
import java.util.*


class LoginActivity : BaseActivity() {

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var googleSignInClient: GoogleSignInClient

    // facebookログインで使用
    private lateinit var callbackManager: CallbackManager

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        // TODO 最適な位置に移動する必要がある
        FirebaseAuth.getInstance().signOut()
        // 表示前の処理
        init()

        // 表示後の処理
        val email = findViewById<EditText>(R.id.email)
        if(BuildConfig.DEBUG)
            email.setText("000000@example.com")

        val password = findViewById<EditText>(R.id.password)
        val login = findViewById<Button>(R.id.login)
        val loading = findViewById<ProgressBar>(R.id.loading)

        loginViewModel = ViewModelProviders.of(this,
            LoginViewModelFactory()
        )
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both email / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        // loginViewModel.loginSuccess更新後
        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            if (loginResult.error != null) {
                loading.visibility = View.GONE
                showLoginFailed(loginResult.error)
                return@Observer
            }

            // 認証成功
            if (loginResult.success != null) {

                // ログインユーザチェック
                isValidLoginUser()

            }

        })

        email.afterTextChanged {
            loginViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            // フォーカス外れた場合に実行される
//            setOnEditorActionListener { _, actionId, _ ->
//                when (actionId) {
//                    EditorInfo.IME_ACTION_DONE ->
//                        loginViewModel.login(
//                            email.text.toString(),
//                            password.text.toString()
//                        )
//                }
//                false
//            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(email.text.toString(), password.text.toString())

            }
        }
        //デバッグ
        //loginViewModel.login("000000", "000000")
    }

    fun init() {
        initFacebook()
        initGoogle()
        initLayout()
        initClick()
    }

    fun initFacebook() {
        callbackManager = CallbackManager.Factory.create()
        facebookLoginButton.registerCallback(callbackManager,
            object: FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult ) {
                    loading.visibility = View.VISIBLE
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Timber.d("facebook:onCancel")
                }

                override fun onError(exception: FacebookException ) {
                    // App code
                    Timber.e(exception,"facebook:onError")
                }
            })

    }

    fun initGoogle() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    /**
     * Google SignIn
     */
    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent,
            RC_SIGN_IN
        )
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                account?.apply{
                    firebaseAuthWithGoogle(this)
                }
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                loading.visibility = View.GONE
                signOutProvider()
                //updateUI(null)
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // ログイン成功
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    // プロフィール設定画面で使う
                    isFacebookAuth = true
                    loginViewModel.loginSuccess(user?.email?.also{it.getIdFromEmail()}, user?.displayName)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    loading.visibility = View.GONE
                    //updateUI(null)
                }
            }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = FirebaseAuth.getInstance().currentUser
                    // currentUserは取得出来てるけど、uidを紐付いてない場合がある
                    // プロフィール設定画面で使う
                    isGoogleAuth = true
                    // ログイン成功
                    loginViewModel.loginSuccess(user?.email?.also{it.getIdFromEmail()}, user?.displayName)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(container, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    Toast.makeText(
                        applicationContext,
                        "ログインに失敗しました。",
                        Toast.LENGTH_LONG
                    ).show()
                    loading.visibility = View.GONE
                }

            }
    }

    fun initLayout() {
        createUserTextView.paintFlags = createUserTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun initClick() {
        email.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus)
                hideKeybord(v)
        }
        password.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus)
                hideKeybord(v)
        }

        googleLoginButton.setOnClickListener{
            loading.visibility = View.VISIBLE
            googleSignIn()
        }
        container.setOnClickListener {
            hideKeybord(it)
        }
        // ユーザ新規作成
        createUserTextView.setOnClickListener {
            CreateUserActivity.start(this)
        }
        // パスワードを忘れた方へ
        SendPasswordActivityTextView.setOnClickListener{
            SendPasswordActivity.start(this)
        }
    }

    /**
     * ログインユーザチェック
     * @param user: User
     */
    private fun isValidLoginUser() {

        // Google, FacebookログインでAuthenticationにアカウントはあるが、
        // ユーザ情報に 連携されていず、ボタン押下するとonSuccessしないので、onFailureの定義
        val onFailure = {
            // ログインできませんでした。ユーザ情報が存在しません。\n別の方法でログインして下さい
            Toast.makeText(
                applicationContext,
                applicationContext.getString(R.string.login_failed_no_user),
                Toast.LENGTH_LONG
            ).show()
            signOutProvider()
            loading.visibility = View.GONE
        }

        // 多重ログインチェック
        UserStore.isAlreadyLogin(onFailure) {

            if (it) {
                val mail = FirebaseAuth.getInstance().currentUser?.email
                // 暫定対応としてテストユーザのみ多重ログイン制御
                if(mail?.getDomainFromEmail() == "example.com") {
                    // 別のユーザがログイン済みです
                    Toast.makeText(
                        this,
                        getString(R.string.login_failed_already),
                        Toast.LENGTH_LONG
                    ).show()
                    loading.visibility = View.GONE
                    FirebaseAuth.getInstance().signOut()
                    signOutProvider()
                    return@isAlreadyLogin
                }
            }
            // ログイン日時更新
            UserStore.updateLoginDateTime(Date()){
                // 正常
                updateUiWithUser()
            }


        }
    }


    /**
     * ログイン成功したら呼ばれる
     * @param user: User
     */
    private fun updateUiWithUser() {
        val welcome = getString(R.string.welcome)

        // Manager初期設定
        FirebaseFacade.initManager({}) {
            // FCM初期化
            FcmUtil.initFcm()
            val displayName = UserManager.myUser.name

            Toast.makeText(
                applicationContext,
                "$welcome $displayName",
                Toast.LENGTH_LONG
            ).show()

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            Timber.d("uid:$uid)")

            loading.visibility = View.GONE
            MainActivity.start(this)

            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            // 一瞬アプリが消えるバグの為、削除
            //finish()
        }

    }


    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    /**
     * デバッグ用
     *
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        emailEditText?.loggingSize()
        login.loggingSize()
        facebookLoginButton.loggingSize()
        googleLoginButton.loggingSize()

    }

    companion object {
        private val TAG = "LoginActivity"
        private val RC_SIGN_IN = 9001 // Google

        fun start(activity: Activity) = activity.apply {
            finishAffinity()
            activity.startActivity(Intent(activity, LoginActivity::class.java))
        }

        fun signOut(activity: BaseActivity) {
            activity.apply {
                UserStore.updateLoginDateTime(null){
                    UserStore.updateFcmToken(null){
                        Prefs().fcmToken.remove()
                        Prefs().hasToUpdateFcmToken.put(true)

                        signOutProvider()
                        FirebaseAuth.getInstance().signOut()

                        FirebaseFacade.clearManager()

                        finishAffinity()
                        activity.startActivity(Intent(activity, SplashActivity::class.java))
                    }
                }
            }
        }

        fun signOutProvider() {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(applicationContext.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            GoogleSignIn.getClient(applicationContext, gso).signOut()

            LoginManager.getInstance().logOut()

            isFacebookAuth = false  // プロフィール設定画面で使う
            isGoogleAuth = false    // プロフィール設定画面で使う
        }
    }
}


