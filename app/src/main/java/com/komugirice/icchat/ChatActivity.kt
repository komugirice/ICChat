package com.komugirice.icchat

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityChatBinding
import com.komugirice.icchat.enum.ActivityEnum
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.store.MessageStore
import com.komugirice.icchat.firestore.store.RoomStore
import com.komugirice.icchat.viewModel.ChatViewModel
import kotlinx.android.synthetic.main.activity_chat.*
import timber.log.Timber

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel
    private val handler = Handler()
    private lateinit var room: Room

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

    /**
     * initializeメソッド
     *
     */
//    private fun initialize() {
//        Timber.d("ChatActivity initialize")
//        initBinding()
//        initViewModel()
//        initLayout()
//        initData()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            ActivityEnum.GroupSettingActivity.id -> {
                // GroupSettingActivityの更新内容をRoomに反映
                if (!viewModel.initRoom(this.room))
                    onBackPressed()
            }
            else -> {
            }

        }
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
        //binding.room = Room()
        binding.lifecycleOwner = this
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
            users.observe(this@ChatActivity, Observer {
                binding.apply {
                    chatView.customAdapter.setUsers(it)
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
        // 送信ボタン
        sendImageView.setOnClickListener {
            if(inputEditText.text.isNotEmpty()) {
                MessageStore.registerMessage(room.documentId, inputEditText.text.toString())
                hideKeybord(it)
                inputEditText.text.clear()
            }


        }
        // 設定アイコン
        settingImageView.setOnClickListener {
            showGroupSettingMenu(it)
        }

    }

    /**
     * initEditTextメソッド
     *
     */
    private fun initEditText() {
        // 検索実行
        inputEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // EditTextに値がある場合
                if(textView.text.toString().isNotEmpty()) {
                    MessageStore.registerMessage(room.documentId, textView.text.toString())
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
                        AlertDialog.Builder(this@ChatActivity)
                            .setMessage(getString(R.string.confirm_group_withdraw))
                            .setNegativeButton("キャンセル", null)
                            .setPositiveButton("OK", object: DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {

                                    RoomStore.removeGroupMember(room, UserManager.myUserId) {
                                        // グループを退会しました
                                        AlertDialog.Builder(this@ChatActivity)
                                            .setMessage(getString(R.string.success_group_withdraw))
                                            .setPositiveButton("OK", object: DialogInterface.OnClickListener {
                                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                                    finish()
                                                }
                                            })
                                            .setOnDismissListener (object: DialogInterface.OnDismissListener {
                                                override fun onDismiss(dialog: DialogInterface?) {
                                                    finish()
                                                }
                                            })
                                            .show()
                                    }
                                }
                            })
                            .show()
                        return true
                    }
                    else -> return false

                }
            }
        })
        popup.show()
    }

    companion object {
        const val KEY_ROOM = "key_room"
        fun start(context: Context?, room: Room) =
            context?.startActivity(
                Intent(context, ChatActivity::class.java)
                    .putExtra(KEY_ROOM, room)
            )
    }
}
