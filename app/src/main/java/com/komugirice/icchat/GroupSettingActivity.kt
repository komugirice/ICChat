package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.qiitaapplication.extension.getDateToString
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.komugirice.icchat.databinding.ActivityCreateUserBinding
import com.komugirice.icchat.databinding.ActivityGroupSettingBinding
import com.komugirice.icchat.extension.afterTextChanged
import com.komugirice.icchat.extension.setRoundedImageView
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.store.RoomStore
import com.komugirice.icchat.firestore.store.UserStore
import com.komugirice.icchat.ui.createUser.CreateUserViewModel
import com.komugirice.icchat.ui.groupSetting.GroupSettingViewModel
import com.komugirice.icchat.util.FireStorageUtil
import com.makeramen.roundedimageview.RoundedDrawable
import com.makeramen.roundedimageview.RoundedImageView
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_create_user.*
import kotlinx.android.synthetic.main.activity_group_setting.*
import kotlinx.android.synthetic.main.activity_group_setting.backImageView
import kotlinx.android.synthetic.main.activity_group_setting.container
import kotlinx.android.synthetic.main.activity_group_setting.saveButton
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_profile_setting.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class GroupSettingActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupSettingBinding
    private lateinit var viewModel: GroupSettingViewModel

    private lateinit var room: Room

    private var uCropSrcUri: Uri? = null

    private var prevSettingUri: String = ""

    private val displayFlg by lazy { intent.getIntExtra(KEY_DISPLAY_FLG, DISPLAY_FLAG_INSERT)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()
        initViewModel()
        initLayout()
        if (displayFlg == DISPLAY_FLAG_UPDATE) initData()
        initClick()
        initCheckBox()

    }

    /**
     * MVVMのBinding
     *
     */
    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_group_setting
        )
        binding.lifecycleOwner = this
    }

    /**
     * MVVMのViewModel
     *
     */
    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GroupSettingViewModel::class.java).apply {
            this.groupSettingFormState.observe(this@GroupSettingActivity, Observer {
                val formState = it ?: return@Observer

                saveButton.isEnabled = formState.isDataValid

                if (formState.groupNameError != null) {
                    groupNameEditText.error = getString(formState.groupNameError)
                }

            })
            canSubmit.observe(this@GroupSettingActivity, Observer {
                binding.canSubmit = it
            })
            binding.name = this.name
            // TODO CHECKBOX

            // グループ名の文字数表示
            groupNameEditText.afterTextChanged {
                groupNameLength.text = "${groupNameEditText.length()}/20"
            }
        }
    }

    private fun initLayout() {
        if(displayFlg == DISPLAY_FLAG_INSERT) {
            imageDeleteButton.visibility = View.GONE
        }

    }

    private fun initData() {
        intent.getSerializableExtra(KEY_ROOM).also {
            if (it is Room && it.documentId.isNotEmpty()) {
                this.room = it
            } else {
                finish()
            }
        }
        initGroupIcon()
    }

    /**
     * グループ画像初期化
     *
     */
    private fun initGroupIcon() {
        FireStorageUtil.getGroupIconImage(this.room.documentId) {
            groupIconImageView.setRoundedImageView(it) // UIスレッド
            uCropSrcUri = it
            prevSettingUri = it.toString()
        }

    }


    /**
     * initClickメソッド
     *
     */
    private fun initClick() {
        // <ボタン
        backImageView.setOnClickListener {
            this.onBackPressed()
        }

        binding.imageSelectButton.setOnClickListener {
            selectImage()
        }

        binding.uCropButton.setOnClickListener {
            if (uCropSrcUri == null) {
                Toast.makeText(this, "プロフィール画像が設定されていません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startUCrop()
        }

        binding.imageDeleteButton.setOnClickListener {
            // 元画像削除
            if(prevSettingUri.isEmpty()) {
                Toast.makeText(this, "プロフィール画像が設定されていません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            delete()
        }


        container.setOnClickListener {
            hideKeybord(it)
        }

        saveButton.setOnClickListener {
            createGroup()
        }
    }

    /**
     * 招待者チェックボックス
     *
     */
    private fun initCheckBox() {
        val friends = UserManager.myFriends
        friends.forEach {
            val checkBox = CheckBox(this)
            checkBox.text = it.name
            checkBox.textSize = 16f

            if(displayFlg ==DISPLAY_FLAG_UPDATE && room.userIdList.contains(it.userId))
                checkBox.isChecked = true

            checkBox.setOnCheckedChangeListener { v, isChecked ->
                viewModel.apply {
                    if(isChecked) {
                        _inviteUser.add(it)
                    } else {
                        _inviteUser.remove(it)
                    }
                    inviteUser.postValue(_inviteUser)
                }
            }
            inviteCheckBoxContainer.addView(checkBox)
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

                    groupIconImageView.setRoundedImageView(it) // UIスレッド
                    uCropSrcUri = it

                }

            }
            UCrop.RESULT_ERROR -> {
                uCropSrcUri = null
                Timber.d(UCrop.getError(data))
                // TODO エラーダイアログ
                Toast.makeText(this@GroupSettingActivity, "画像の加工に失敗しました", Toast.LENGTH_SHORT).show()
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
                .start(this@GroupSettingActivity)
        }
    }

    /**
     * プロフィール画像アップロード
     *
     */
    private fun upload(room: Room, onSuccess: (UploadTask.TaskSnapshot) -> Unit) {

        // 前画像削除
        if(prevSettingUri.isNotEmpty())
            FirebaseStorage.getInstance().getReferenceFromUrl(prevSettingUri).delete()

        val imageUrl = "${System.currentTimeMillis()}.jpg"
        val ref = FirebaseStorage.getInstance().reference.child("${FireStorageUtil.ROOM_PATH}/${room.documentId}/${FireStorageUtil.ROOM_ICON_PATH}/${imageUrl}")

        // RoundedImageViewの不具合修正
        val bitmap = when (userIconImageView) {
            is RoundedImageView -> (groupIconImageView.drawable as RoundedDrawable).toBitmap()
            else -> (groupIconImageView.drawable as BitmapDrawable).bitmap
        }
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val data = baos.toByteArray()
        ref.putBytes(data)
            .addOnFailureListener {
                Toast.makeText(this, "グループ登録に失敗しました", Toast.LENGTH_SHORT).show()
                Timber.tag(TAG)
                Timber.d("画像のUploadに失敗しました")
                Timber.e(it)
                bitmap.recycle()
            }
            .addOnSuccessListener {
                prevSettingUri = ref.toString()
                onSuccess.invoke(it)
            }
    }

    /**
     * プロフィール画像削除
     *
     */
    private fun delete() {
        groupIconImageView.setRoundedImageView(null)
        //FirebaseStorage.getInstance().getReferenceFromUrl(prevSettingUri).delete()
        prevSettingUri = "";
        uCropSrcUri = null
        Toast.makeText(this, "プロフィール画像を削除しました", Toast.LENGTH_SHORT).show()

    }

    /**
     * グループ作成（全画面の中でここだけ）
     *
     */
    private fun createGroup() {
        var tmpRoom: Room = Room()
        // Room作成
        if(displayFlg == DISPLAY_FLAG_INSERT) {
            tmpRoom.apply {
                documentId =  UUID.randomUUID().toString()
                name = groupNameEditText.text.toString()
                userIdList.add(UserManager.myUserId)
                viewModel._inviteUser.forEach {
                    userIdList.add(it.userId)
                }
                isGroup = true
                ownerId = UserManager.myUserId
            }
        } else {
            this.room.name = groupNameEditText.text.toString()
            this.room.userIdList.clear()
            this.room.userIdList.add(UserManager.myUserId)
            viewModel._inviteUser.forEach { user->
                this.room.userIdList.add(user.userId)
            }

            this.room.isGroup = true

            tmpRoom = this.room
        }
        // Room登録
        RoomStore.registerGroupRoom(tmpRoom) {
            if(it.isSuccessful) {
                // 画像登録
                upload(tmpRoom){
                    Toast.makeText(this, "グループを登録しました", Toast.LENGTH_SHORT).show()
                    Timber.tag(TAG)
                    Timber.d("グループ登録成功：${tmpRoom.documentId}")

                    // RoomManager更新
                    RoomManager.initRoomManager {
                        finish()
                    }

                }
            } else {
                Toast.makeText(this, "グループ登録に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }

    }


    companion object {
        private const val KEY_ROOM = "key_room"
        private const val KEY_DISPLAY_FLG = "key_display_flg"
        private const val RC_CHOOSE_IMAGE = 1000
        private const val DISPLAY_FLAG_INSERT = 0
        private const val DISPLAY_FLAG_UPDATE = 1
        private const val TAG = "GroupSettingActivity"

        fun start(context: Context?) {
            context?.startActivity(
                Intent(context, GroupSettingActivity::class.java)
                    .putExtra(KEY_DISPLAY_FLG, DISPLAY_FLAG_INSERT)
            )
        }

        fun update(context: Context?, room: Room) =
            context?.startActivity(
                Intent(context, GroupSettingActivity::class.java)
                    .putExtra(KEY_ROOM, room)
                    .putExtra(KEY_DISPLAY_FLG, DISPLAY_FLAG_UPDATE)
            )
    }
}
