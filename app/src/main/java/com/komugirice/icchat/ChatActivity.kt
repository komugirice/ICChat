package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.qiitaapplication.extension.getSuffix
import com.komugirice.icchat.databinding.ActivityChatBinding
import com.komugirice.icchat.enums.ActivityEnum
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.FileInfo
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.store.MessageStore
import com.komugirice.icchat.util.DialogUtil
import com.komugirice.icchat.util.FIleUtil
import com.komugirice.icchat.util.FireStorageUtil
import com.komugirice.icchat.viewModel.ChatViewModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import timber.log.Timber


class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel
    private val handler = Handler()
    private lateinit var room: Room
    private lateinit var tempImageViewForDownload: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // bindingがある場合は不要
        //setContentView(R.layout.activity_chat)
        initBinding()
        initViewModel()
        initRoom()
    }

    /**
     * GroupSettingActivityから戻った時にRoomが更新されていないバグ対応
     *
     */
    override fun onRestart() {
        Timber.d("ChatActivity onRestart")
        super.onRestart()
    }

    private fun initRoom(){
        if (!viewModel.initRoom(intent))
            onBackPressed()
    }

    /**
     * MVVMのBinding
     *
     */
    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_chat
        )
        binding.lifecycleOwner = this

        // 削除処理の後
        binding.chatView.customAdapter.onClickRefreshCallBack = {
            initData()
        }
        binding.chatView.customAdapter.onClickDownloadCallBack = {
            createFile(it.first, it.second)
            getFireStorageImage(it.first)
        }
    }

    /**
     * MVVMのViewModel
     *
     */
    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ChatViewModel::class.java).apply {
            room.observe(this@ChatActivity, Observer {
                binding.apply {
                    room = it
                }
                this@ChatActivity.room = it
                // ここに置かないとroom設定前でヌルポする
                initLayout()
                initData()
            })
            items.observe(this@ChatActivity, Observer {
                binding.apply {
                    chatView.customAdapter.refresh(it)
                    // 一番下へ移動
                    handler.postDelayed({
                        chatView.scrollToPosition(chatView.customAdapter.itemCount - 1)
                    }, 100L)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
//            users.observe(this@ChatActivity, Observer {
//                binding.apply {
//                    chatView.customAdapter.setUsers(it)
//                }
//            })
        }
    }

    /**
     * initLayoutメソッド
     *
     */
    private fun initLayout() {
        if(!room.isGroup)
            settingImageView.visibility = View.GONE

        initEditText()
        initClick()
        initSwipeRefreshLayout()
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
        // 設定アイコン
        settingImageView.setOnClickListener {
            showGroupSettingMenu(it)
        }
        // 送信ボタン
        sendImageView.setOnClickListener {
            if(inputEditText.text.isNotEmpty()) {
                MessageStore.registerMyMessage(room.documentId, inputEditText.text.toString())
                hideKeybord(it)
                inputEditText.text.clear()
            }
        }
        // 画像ボタン
        imageImageView.setOnClickListener {
            selectImage()
        }
        // ファイル
        fileImageView.setOnClickListener {
            selectFile()
        }

    }

    /**
     * initEditTextメソッド
     *
     */
    private fun initEditText() {
        // キーボードEnter実行
        inputEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // EditTextに値がある場合
                if(textView.text.toString().isNotEmpty()) {
                    MessageStore.registerMyMessage(room.documentId, textView.text.toString())
                }
                //true
            }
            false
        }

        // swipeRefreshLayoutの場合はクリックしてもフォーカスが変わらないので下の処理は適用されない
        inputEditText.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus)
                hideKeybord(v)
        }
    }

    /**
     * initDataメソッド
     *
     */
    private fun initData() {

        viewModel.initData(this@ChatActivity, room.documentId)

    }

    /**
     * initSwipeRefreshLayoutメソッド
     *
     */
    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.initData(this@ChatActivity, room.documentId)
        }
    }

    /**
     * 設定アイコンのグループオプションメニュー
     * @param v: View
     * @return Boolean
     *
     */
    fun showGroupSettingMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.chat_group_setting)

        if(room.ownerId == UserManager.myUserId)
            popup.menu.findItem(R.id.group_withdraw).setVisible(false)
        else
            popup.menu.findItem(R.id.group_setting).setVisible(false)

        popup.setOnMenuItemClickListener ( object: PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.group_info -> {
                        GroupInfoActivity.startActivityForResult(this@ChatActivity, room)
                        return true
                    }
                    R.id.group_setting -> {
                        GroupSettingActivity.updateActivityForResult(this@ChatActivity, room)
                        return true
                    }
                    R.id.group_withdraw -> {
                        // グループを退会しますか？
                        DialogUtil.withdrawGroupDialog(this@ChatActivity, room){
                            finish()
                        }
                        return true
                    }
                    else -> return false

                }
            }
        })
        popup.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null)
            return
        when(requestCode) {
            ActivityEnum.GroupSettingActivity.id -> {
                // GroupSettingActivityの更新内容をRoomに反映
                if (!viewModel.initRoom(this.room))
                    onBackPressed()
            }
            // 画像ボタンから
            RC_CHOOSE_IMAGE -> {

                data.data?.also {
                    // 画像登録
                    Timber.d(it.toString())
                    FirebaseFacade.registChatMessageImage(room.documentId, it){
                        Timber.d("画像アップロード成功")
                    }
                }

            }
            // ファイルボタンから
            RC_CHOOSE_FILE -> {

                data.data?.also {
                    // ファイル登録
                    Timber.d(it.toString())
                    FirebaseFacade.registChatMessageFile(room.documentId, it){
                        Timber.d("ファイルアップロード成功")
                    }
                }

            }
            // ダウンロードリンクから
            RC_WRITE_FILE -> {

                data.data?.also {
                    Timber.d("ダウンロード先URL：$it")
                    val takeFlags: Int = intent.flags and
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    // Check for the freshest data.
                    contentResolver.takePersistableUriPermission(it, takeFlags)
                    downloadImage(it)

                }
            }
            else -> {
            }

        }
    }

    /**
     * 画像選択
     *
     */
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("image/*")
        startActivityForResult(intent, RC_CHOOSE_IMAGE)
    }

    /**
     * ファイル選択
     *
     */
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
        startActivityForResult(intent, RC_CHOOSE_FILE)
    }

    /**
     * ファイル作成
     * mimeType: image/jpg等
     * fileName: Uri設定
     *
     */
    private fun createFile(message: Message, fileInfo: FileInfo?) {
        val fileName = fileInfo?.name ?: getString(R.string.no_file_name)
        var mimeType = ""
        when(message.type) {
            MessageType.IMAGE.id -> mimeType = "image/${message.message.getSuffix()}"
            else-> mimeType = ""
        }

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            addCategory(Intent.CATEGORY_OPENABLE)

            // Create a file with the requested MIME type.
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        startActivityForResult(intent, RC_WRITE_FILE)
    }

    private fun getFireStorageImage(message: Message) {
        var fileName = message.message
        FireStorageUtil.downloadRoomMessageImageUri(message.roomId, fileName) {

            it?.apply {
                tempImageViewForDownload = ImageView(this@ChatActivity)
                Picasso.get().load(this).into(tempImageViewForDownload)
            }
        }
    }

    /**
     * 画像タイプ ダウンロード
     *
     * @param uri
     */
    private fun downloadImage(uri: Uri) {

        val bmp = tempImageViewForDownload.drawable.toBitmap()
        val outputStream = getContentResolver().openOutputStream(uri)
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        Toast.makeText(
            this@ChatActivity,
            R.string.alert_complete_download,
            Toast.LENGTH_LONG).show()
    }


    companion object {
        private const val RC_CHOOSE_IMAGE = 1000
        private const val RC_CHOOSE_FILE = 1001
        private const val RC_WRITE_FILE: Int = 1002
        const val KEY_ROOM = "key_room"
        fun start(context: Context?, room: Room) =
            context?.startActivity(
                Intent(context, ChatActivity::class.java)
                    .putExtra(KEY_ROOM, room)
            )
    }
}
