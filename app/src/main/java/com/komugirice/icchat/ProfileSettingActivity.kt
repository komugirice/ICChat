package com.komugirice.icchat

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.net.toUri
import com.example.qiitaapplication.extension.getDateToString
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
import com.google.android.gms.common.util.IOUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.komugirice.icchat.extension.setRoundedImageView
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.store.UserStore
import com.komugirice.icchat.util.FireStorageUtil
import com.makeramen.roundedimageview.RoundedDrawable
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_profile_setting.*
import kotlinx.serialization.json.Json.Companion.context
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Exception
import java.util.*

class ProfileSettingActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    // facebookログインで使用
    private lateinit var callbackManager: CallbackManager

    private val auth = FirebaseAuth.getInstance()

    private lateinit var uCropSrcUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setting)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        initLayout()
    }

    private fun initialize() {
        initFacebook()
        initGoogle()
        initLayout()
        initUserIcon()
        initClick()
    }

    private fun initLayout() {
        val myUser = UserManager.myUser
        email.text = FirebaseAuth.getInstance().currentUser?.email
        userName.text = if(myUser.name.isNotEmpty()) myUser.name else "設定なし"
        birthDay.text = myUser.birthDay?.getDateToString() ?: "設定なし"
    }

    /**
     * プロフィール画像初期化
     *
     */
    private fun initUserIcon() {
        val myUser = UserManager.myUser

        FireStorageUtil.getUserIconImage(UserManager.myUserId) {
            it?.result?.apply {
                userIconImageView.setRoundedImageView(this) // UIスレッド
                uCropSrcUri = this
            }
        }

    }


    private fun initClick() {
        backImageView.setOnClickListener {
            finish()
        }

        usrIconSelectButton.setOnClickListener {
            selectImage()
        }

        uCropButton.setOnClickListener {
            startUCrop()
        }

        usrIconUploadButton.setOnClickListener {
            upload()
        }

        usrIconDeleteButton.setOnClickListener {
            delete()
        }

        userName.setOnClickListener {
            UserNameActivity.start(this)
        }

        birthDay.setOnClickListener {
            showDateDialog()
        }

        facebookConnectButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
        }


    }

    /**
     * 日付ダイアログ
     *
     */
    private fun showDateDialog() {
        val dialog = DatePickerDialog(this, object: DatePickerDialog.OnDateSetListener{
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                updateBirthDay(year, month, dayOfMonth)
            }
        }, 2000, 1, 1)
        dialog.show()
    }

    /**
     * 誕生日更新
     *
     */
    fun updateBirthDay(year: Int, month: Int, dayOfMonth: Int) {
        val birthDay = Calendar.getInstance().run {
            set(year, month, dayOfMonth, 0, 0, 0)
            time
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(UserManager.myUser.userId)
            .update("birthDay", birthDay)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    UserManager.myUser.birthDay = birthDay
                    initLayout()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("エラー")
                        .setMessage("登録に失敗しました。")
                        .setPositiveButton("OK", null)
                        .show();
                }
            }
    }

    /**
     * Facebook連携機能初期化
     *
     */
    fun initFacebook() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,
            object: FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Timber.d("facebook:onCancel")
                }

                override fun onError(exception: FacebookException) {
                    // App code
                    Timber.e(exception,"facebook:onError")
                }
            })

    }

    /**
     * Facebook連携機能
     *
     */
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Facebook認証成功
                    Log.d(TAG, "signInWithCredential:success")
                    UserStore.addUid(this)


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
    }

    /**
     * Google連携機能初期化
     *
     */
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
            RC_GOOGLE_SIGN_IN
        )
    }

    /**
     * Google連携のFirebaseAuth認証
     *
     */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    // 認証成功

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(container, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    Toast.makeText(
                        applicationContext,
                        "ログインに失敗しました。",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
    }

    /**
     * ActivityResult
     *
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null)
            return
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        when(requestCode) {
            // Google連携
            RC_GOOGLE_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                    //updateUI(null)
                }
            }
            // Facebook連携
            RC_FACEBOOK_SIGN_IN -> {
                // Pass the activity result back to the Facebook SDK
                callbackManager.onActivityResult(requestCode, resultCode, data)
            }
            // アイコン画像
            RC_CHOOSE_IMAGE -> {

                data.data?.also {
                    // uCrop実行
                    uCropSrcUri = it
                    startUCrop()
                }

            }
            UCrop.REQUEST_CROP -> {
                val resultUri = UCrop.getOutput(data)

                resultUri?.also {
                    Timber.d(it.toString())

                    //userIconImageView.setImageURI(it)

                    userIconImageView.setRoundedImageView(it) // UIスレッド
                    uCropSrcUri = it

                    // Piccaso onSUccess()はよく失敗するので使うべきではない
//                    Picasso.get().load(it).into(userIconImageView, object: Callback {
//                        override fun onSuccess() {
//                            uCropSrcUri = it
//                            upload()
//                        }
//
//                        override fun onError(e: Exception?) {
//                            Toast.makeText(
//                                this@ProfileSettingActivity,
//                                "画像の取得に失敗しました",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    })

                }

            }
            UCrop.RESULT_ERROR -> {
                Timber.d(UCrop.getError(data))
                // TODO エラーダイアログ
                Toast.makeText(this@ProfileSettingActivity, "画像の加工に失敗しました", Toast.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * プロフィール画像選択
     *
     */
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("image/jpeg")
        startActivityForResult(intent, RC_CHOOSE_IMAGE)
    }

    /**
     * uCropActivity表示
     *
     */
    private fun startUCrop() {
        var file = File.createTempFile("${System.currentTimeMillis()}", ".temp", cacheDir)
        UCrop.of(uCropSrcUri, file.toUri())
            .withAspectRatio(1f, 1f)
            .withOptions(UCrop.Options().apply {
                setHideBottomControls(true)
                setCircleDimmedLayer(true)
                setShowCropGrid(false)
                setShowCropFrame(false)
            })
            .start(this)
    }

    /**
     * プロフィール画像アップロード
     *
     */
    private fun upload() {

        // 元画像削除
        if(UserManager.myUser.imageUrl.isNotEmpty())
            FirebaseStorage.getInstance().reference.child("${UserManager.myUserId}/${FireStorageUtil.USER_ICON_PATH}/${UserManager.myUser.imageUrl}").delete()

        val imageUrl = "${System.currentTimeMillis()}.jpg"
        val ref = FirebaseStorage.getInstance().reference.child("${UserManager.myUserId?: "noUser"}/${FireStorageUtil.USER_ICON_PATH}/${imageUrl}")

        // RoundedImageViewの不具合修正
        val bitmap = when (userIconImageView) {
            is RoundedImageView -> (userIconImageView.drawable as RoundedDrawable).toBitmap()
            else -> (userIconImageView.drawable as BitmapDrawable).bitmap
        }
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()
        ref.putBytes(data)
            .addOnFailureListener {
                Toast.makeText(this, "Uploadに失敗しました", Toast.LENGTH_SHORT).show()
                bitmap.recycle()
            }
            .addOnSuccessListener {
                UserStore.updateImageUrl(imageUrl) {
                    // UserManagerの更新は必須
                    UserManager.myUser.imageUrl = imageUrl

                    Toast.makeText(this, "プロフィール画像を設定しました", Toast.LENGTH_SHORT).show()
                    Timber.d("Upload成功：${imageUrl}")
                    // ImageViewだとなぜか落ちる
                    //bitmap.recycle()
                }
            }
    }

    /**
     * プロフィール画像削除
     *
     */
    private fun delete() {
        // 元画像削除
        if(UserManager.myUser.imageUrl.isEmpty()) {
            Toast.makeText(this, "プロフィール画像が設定されていません", Toast.LENGTH_SHORT).show()
            return
        }

        userIconImageView.setRoundedImageView(null)
        FirebaseStorage.getInstance().reference.child("${UserManager.myUserId}/${FireStorageUtil.USER_ICON_PATH}/${UserManager.myUser.imageUrl}").delete()
        Toast.makeText(this, "プロフィール画像を削除しました", Toast.LENGTH_SHORT).show()

    }

    companion object {
        private const val TAG = "ProfileSettingActivity"
        private const val RC_GOOGLE_SIGN_IN = 9001
        private const val RC_FACEBOOK_SIGN_IN = 64206
        private const val RC_CHOOSE_IMAGE = 1000

        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, ProfileSettingActivity::class.java)
            )
    }
}
