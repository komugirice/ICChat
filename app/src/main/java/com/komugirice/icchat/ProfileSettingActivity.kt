package com.komugirice.icchat

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.komugirice.icchat.ICChatApplication.Companion.isFacebookAuth
import com.komugirice.icchat.ICChatApplication.Companion.isGoogleAuth
import com.komugirice.icchat.databinding.ActivityProfileSettingBinding
import com.komugirice.icchat.databinding.FriendRequestedCellBinding
import com.komugirice.icchat.extension.getDateToString
import com.komugirice.icchat.extension.setRoundedImageView
import com.komugirice.icchat.extension.toggle
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Request
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.util.FireStorageUtil
import com.komugirice.icchat.viewModel.ProfileSettingViewModel
import com.makeramen.roundedimageview.RoundedDrawable
import com.makeramen.roundedimageview.RoundedImageView
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_header.view.*
import kotlinx.android.synthetic.main.activity_profile_setting.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class ProfileSettingActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileSettingBinding
    private lateinit var viewModel: ProfileSettingViewModel

    private lateinit var googleSignInClient: GoogleSignInClient

    // facebookログインで使用
    private lateinit var callbackManager: CallbackManager

    private val auth = FirebaseAuth.getInstance()

    private var uCropSrcUri: Uri? = null

    private var prevSettingUri: String = ""

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
        initBinding()
        initViewModel()
        initFacebook()
        initGoogle()
        initLayout()
        initData()
        initUserIcon()
        initClick()
    }

    /**
     * MVVMのBinding
     *
     */
    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_profile_setting
        )
        binding.lifecycleOwner = this

        binding.isFacebookAuth = isFacebookAuth
        binding.isGoogleAuth = isGoogleAuth
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ProfileSettingViewModel::class.java).apply {
            //
            items_request.observe(this@ProfileSettingActivity,  androidx.lifecycle.Observer{
                displayRequestFriend(it)
            })
        }
    }

    private fun initLayout() {
        // タイトル
        binding.header.titleTextView.text =  getString(R.string.title_profile_setting)
        val myUser = UserManager.myUser
        email.text = myUser.email
        userName.text = if(myUser.name.isNotEmpty()) myUser.name else getString(R.string.no_setting)
        birthDay.text = myUser.birthDay?.getDateToString() ?: getString(R.string.no_setting)
    }

    private fun initData(){
        viewModel.update()
    }


    /**
     * プロフィール画像初期化
     *
     */
    private fun initUserIcon() {

        FireStorageUtil.getUserIconImage(UserManager.myUserId) {
                userIconImageView.setRoundedImageView(it) // UIスレッド
                uCropSrcUri = it
                prevSettingUri = it.toString()
        }

    }


    private fun initClick() {
        binding.header.backImageView.setOnClickListener {
            finish()
        }

        usrIconSelectButton.setOnClickListener {
            selectImage()
        }

        uCropButton.setOnClickListener {
            if (uCropSrcUri == null) {
                Toast.makeText(this, R.string.alert_no_profile_image, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startUCrop()
        }

        usrIconUploadButton.setOnClickListener {
            if (uCropSrcUri == null) {
                Toast.makeText(this, R.string.alert_no_profile_image, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            upload()
        }

        usrIconDeleteButton.setOnClickListener {
            // 元画像削除
            if(prevSettingUri.isEmpty()) {
                Toast.makeText(this, R.string.alert_no_profile_image, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            delete()
        }

        userName.setOnClickListener {
            UserNameActivity.start(this)
        }

        birthDay.setOnClickListener {
            showDateDialog()
        }


        facebookConnectButton.setOnClickListener {
            // 認証済→解除
            if(isFacebookAuth) {
                // Facebook連携を解除しますか？\n※解除する場合は解除後に再度ログインして下さい
                AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_facebook_disconnect)
                    .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            // 解除
                            disconnectFacebook()
                        }
                    })
                    .setNeutralButton(R.string.cancel, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                        }
                    }).show()

            } else
                // 認証なし→認証
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"))
        }
        googleConnectButton.setOnClickListener{
            if(isGoogleAuth) {
                // 認証済→解除
                disconnectGoogle()
            } else
                // 認証なし→認証
                googleSignIn()
        }

        expandLabelView.setOnClickListener {
            val willVisible = requestFriendsParentView.visibility != View.VISIBLE
            requestFriendsParentView.toggle(willVisible)
            expandableImageView.rotation = if (willVisible) 180F else 0F
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
        }, 2000, 0, 1)
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
                        .setTitle(R.string.error)
                        .setMessage(R.string.failed_regist)
                        .setPositiveButton("OK", null)
                        .show()
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
        // 連携失敗用に退避
        val tmpIdToken = auth.currentUser?.getIdToken(true)

        Log.d(TAG, "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Facebook認証成功
                    Log.d(TAG, "signInWithCredential:success")

                    // ※この時点でauth.currentUser.uidがfacebookのuidに変わっている!
                    val uid = auth.currentUser?.uid
                    // 他ユーザチェック
                    UserStore.isExistUidInOtherUser(uid) {

                        if (it) {
                            // 既に他のユーザに連携されています
                            Toast.makeText(
                                this,
                                getString(R.string.alert_already_connect_other),
                                Toast.LENGTH_LONG
                            ).show()
//                            tmpIdToken?.result?.token?.apply{
//                                auth.signInWithCustomToken(this)
//                            }
                            return@isExistUidInOtherUser
                        }

                        UserStore.addUid(
                            {
                                // 既に連携済みです。
                                Toast.makeText(
                                    this,
                                    getString(R.string.alert_already_connect),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        ) {
                            // Facebookに連携しました
                            Toast.makeText(
                                this,
                                getString(R.string.success_facebook_connect),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // 自ユーザに連携済みエラー、新しく連携の場合はFacebook連携済みを画面反映
                        isFacebookAuth = true
                        binding.isFacebookAuth = isFacebookAuth
                    }


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
        // 連携失敗用に退避
        val tmpCurrentUser = auth.currentUser

        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 認証成功
                    Log.d(TAG, "signInWithCredential:success")

                    // 連携済みエラーの場合はGoogle連携済みを画面反映
                    isGoogleAuth = true
                    binding.isGoogleAuth = isGoogleAuth

                    // ※この時点でauth.currentUser.uidがGoogleのuidに変わっている!
                    val uid = auth.currentUser?.uid
                    // 他ユーザチェック
                    UserStore.isExistUidInOtherUser(uid) {

                        if(it) {
                            // 既に他のユーザに連携されています
                            Toast.makeText(
                                this,
                                getString(R.string.alert_already_connect_other),
                                Toast.LENGTH_LONG
                            ).show()
                            return@isExistUidInOtherUser
                        }

                        // 登録
                        UserStore.addUid(
                            {
                                // 既に連携済みです。
                                Toast.makeText(
                                    this,
                                    getString(R.string.alert_already_connect),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        ) {
                            // Googleに連携しました
                            Toast.makeText(
                                this,
                                getString(R.string.success_google_connect),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(container, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    Toast.makeText(
                        applicationContext,
                        "Google連携に失敗しました。",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
    }

    /**
     * Facebook連携解除
     *
     */
    fun disconnectFacebook() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseAuth.getInstance().currentUser?.unlink(FacebookAuthProvider.PROVIDER_ID)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Auth provider unlinked from account
                    Toast.makeText(
                        this,
                        getString(R.string.success_facebook_disconnect)
                        ,Toast.LENGTH_LONG
                    ).show()
                    // uid削除
                    UserStore.removeUid(uid){
                        FirebaseAuth.getInstance().currentUser?.delete()
                        LoginActivity.signOut(this)
                    }

                }
            }
    }

    /**
     * Google連携解除
     *
     */
    fun disconnectGoogle() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseAuth.getInstance().currentUser?.unlink(GoogleAuthProvider.PROVIDER_ID)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Auth provider unlinked from account
                    Toast.makeText(
                        this,
                        getString(R.string.success_google_disconnect)
                        ,Toast.LENGTH_LONG
                    ).show()
                    isGoogleAuth = false
                    binding.isGoogleAuth = isGoogleAuth
                    // uid削除
                    UserStore.removeUid(uid){}
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
                    Toast.makeText(this@ProfileSettingActivity, "Google連携に失敗しました", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "Google sign in failed", e)

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

                    // Picasso onSUccess()はよく失敗するので使うべきではない
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
                uCropSrcUri = null
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
        uCropSrcUri?.apply {
            UCrop.of(this, file.toUri())
                .withAspectRatio(1f, 1f)
                .withOptions(UCrop.Options().apply {
                    setHideBottomControls(true)
                    setCircleDimmedLayer(true)
                    setShowCropGrid(false)
                    setShowCropFrame(false)
                })
                .start(this@ProfileSettingActivity)
        }
    }

    /**
     * プロフィール画像アップロード
     *
     */
    private fun upload() {

        showProgressDialog(this)

        // 前画像削除
        if(prevSettingUri.isNotEmpty())
            //FirebaseStorage.getInstance().reference.child("${UserManager.myUserId}/${FireStorageUtil.USER_ICON_PATH}/${prevSettingUri}").delete()
            FirebaseStorage.getInstance().getReferenceFromUrl(prevSettingUri).delete()
        val imageUrl = "${System.currentTimeMillis()}.jpg"
        val ref = FirebaseStorage.getInstance().reference.child("${FireStorageUtil.USER_ICON_PATH}/${UserManager.myUserId}/${imageUrl}")

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
                dismissProgressDialog()
            }
            .addOnSuccessListener {
                prevSettingUri = ref.toString()

                Toast.makeText(this, "プロフィール画像を設定しました", Toast.LENGTH_SHORT).show()
                Timber.d("Upload成功：${imageUrl}")
                dismissProgressDialog()
                // ImageViewのbitmapだとなぜか落ちる
                //bitmap.recycle()
            }
    }

    /**
     * プロフィール画像削除
     *
     */
    private fun delete() {
        userIconImageView.setRoundedImageView(null)
        //FirebaseStorage.getInstance().reference.child("${UserManager.myUserId}/${FireStorageUtil.USER_ICON_PATH}/${prevSettingUri}").delete()
        FirebaseStorage.getInstance().getReferenceFromUrl(prevSettingUri).delete()
        prevSettingUri = ""
        uCropSrcUri = null
        Toast.makeText(this, "プロフィール画像を削除しました", Toast.LENGTH_SHORT).show()

    }

    /**
     * 友だち申請中リスト表示
     *
     */
    private fun displayRequestFriend(list : List<Request>) {
        requestFriendsParentView.removeAllViews()
        list.forEach {
            val cellBindable =
                FriendRequestedCellBinding.inflate(LayoutInflater.from(this), null, false)
            cellBindable.request = it
            requestFriendsParentView.addView(cellBindable.root)
        }
        if(list.isEmpty()) {
            expandableImageView.visibility = View.GONE
            requestFriendsParentView.visibility = View.VISIBLE
            requestFriendsParentView.addView(TextView(this).apply{
                text = getString(R.string.none)
                gravity = Gravity.START
                textSize = 16.0f

            })
        }
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
