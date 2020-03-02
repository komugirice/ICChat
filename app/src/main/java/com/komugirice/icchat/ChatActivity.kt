package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityChatBinding
import com.komugirice.icchat.enums.ActivityEnum
import com.komugirice.icchat.extension.makeTempFile
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.FileInfo
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.store.MessageStore
import com.komugirice.icchat.util.DialogUtil
import com.komugirice.icchat.util.FireStorageUtil
import com.komugirice.icchat.viewModel.ChatViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_chat.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel
    private val handler = Handler()
    private lateinit var room: Room
    private var tempImageViewForDownload: ImageView? = null
    private var tempFileForDownload: File? = null
    private var tempDownloadPair : Pair<Message, FileInfo?>? = null

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
            initData(true)
        }
        binding.chatView.customAdapter.onClickDownloadCallBack = {
            // storageのファイルを削除済だとexceptionが発生するバグ対応
            tempImageViewForDownload = null
            tempFileForDownload = null
            tempDownloadPair = it
//            if (it.first.type == MessageType.IMAGE.id)
//                getFireStorageFile(it.first)
            createFile(it.first, it.second)
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
                    if (!viewModel.isNonMove)
                        handler.postDelayed({
                            chatView.scrollToPosition(chatView.customAdapter.itemCount - 1)
                        }, 500L)
                    swipeRefreshLayout.isRefreshing = false
                    viewModel.isNonMove = false
                }
            })
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
    private fun initData(isNonMove: Boolean = false) {

        viewModel.initData(this@ChatActivity, room.documentId, isNonMove)

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
            popup.menu.findItem(R.id.group_withdraw).isVisible = false
        else
            popup.menu.findItem(R.id.group_setting).isVisible = false

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

                    val tmpFile = it.makeTempFile()
                    // ファイルサイズが0バイトなら終了
                    if(tmpFile == null || tmpFile.readBytes()?.size == 0){
                        Toast.makeText(
                            this@ChatActivity,
                            R.string.alert_no_file_size,
                            Toast.LENGTH_LONG).show()
                        return
                    }

                    // 画像登録
                    Timber.d(it.toString())
                    FirebaseFacade.registChatMessageImage(room.documentId, it){
                        Timber.d("画像アップロード成功")
                        tmpFile.delete()
                    }
                }

            }
            // ファイルボタンから
            RC_CHOOSE_FILE -> {
                data.data?.also {
                    val file = it.makeTempFile()
                    if (file == null) {
                        Toast.makeText(
                            this,
                            R.string.alert_failed_upload,
                            Toast.LENGTH_LONG
                        ).show()
                        return@also
                    }

                    var fileSize = file.length() / 1024.0 / 1024.0 // メガバイト

                    if (fileSize > 20) {
                        Toast.makeText(
                            this,
                            R.string.invalid_uplode_file_size,
                            Toast.LENGTH_LONG
                        ).show()
                        file.delete()
                        return@also
                    }

                    FirebaseFacade.registChatMessageFile(this, room.documentId, file, it) {
                        Timber.d("ファイルアップロード成功")
                        file.delete()
                    }

                }

            }
            // ダウンロードリンクから
            RC_WRITE_FILE -> {
                data.data?.also {

                    Timber.d("ダウンロード先URL：$it")
                    showProgressDialog(this)
                    val takeFlags: Int = intent.flags and
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    // Check for the freshest data.
                    contentResolver.takePersistableUriPermission(it, takeFlags)
                    tempDownloadPair?.also { pair ->
                        downloadFile(pair.first, it)
                    }

                } ?: run {
                    // TODO:ダウンロード失敗
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

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as
            // a file (as opposed to a list of contacts or timezones).
            addCategory(Intent.CATEGORY_OPENABLE)

            // Create a file with the requested MIME type.
            type = fileInfo?.mimeType ?: "*/*"  // TODO NULLで落ちるか調査が必要
            putExtra(Intent.EXTRA_TITLE, fileName)
        }

        startActivityForResult(intent, RC_WRITE_FILE)
    }

    /**
     * downloadFile
     * CreateBy Jane
     */
    private fun downloadFile(message: Message, uri: Uri) {
        Timber.d("ここから書き込み開始　結構長いです---------------------------------------------------")
        // まずはtempFileを作ってそこにFileのDownload
        val tempFile = File.createTempFile("${System.currentTimeMillis()}", "temp", cacheDir)
        FireStorageUtil.downloadRoomMessageFile(message, tempFile, { // tempFileに保存成功
            Timber.d("donloadFile complete")
            // uriから保存用のoutputStreamの生成
                    Observable.just(uri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map {// uriからParcelFileDescriptor生成
                    contentResolver.openFileDescriptor(it, "w")
                }.map { // OutPutStream生成
                    FileOutputStream(it!!.fileDescriptor)
                }.map {
                    val inputStream = tempFile.inputStream() // 書き込みソースのstream
                    while(true) { // ひたすら書き込む
                        val data = inputStream.read()
                        if (data == -1)
                            break
                        it.write(data)
                    }
                    inputStream.close()
                    it
                }.map {
                    it.close()
                    true
                }.subscribe({
                    // 書き込み成功
                    Toast.makeText(
                        this@ChatActivity,
                        R.string.alert_complete_download,
                        Toast.LENGTH_LONG).show()
                    Timber.d("書き込み成功---------------------------------------------------")
                }, {
                    Toast.makeText(
                        this@ChatActivity,
                        R.string.alert_failed_download,
                        Toast.LENGTH_LONG).show()
                    // 書き込み失敗
                    Timber.d("書き込み失敗---------------------------------------------------")
                }, { // どっちにしろtempFileは消す
                    tempFile.delete()
                    dismissProgressDialog()
                })

        }, {
            // 失敗
            Timber.d("downloadFile Error")
            Toast.makeText(
                this@ChatActivity,
                R.string.alert_failed_download,
                Toast.LENGTH_LONG).show()
            dismissProgressDialog()
        })
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
