package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.data.firestore.Friend
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.data.firestore.manager.MessageManager
import com.komugirice.icchat.databinding.ActivityChatBinding
import com.komugirice.icchat.databinding.FragmentFriendBinding
import com.komugirice.icchat.viewModel.ChatViewModel
import com.komugirice.icchat.viewModel.FriendViewModel
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_message_cell.*

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel

    lateinit var room: Room
    lateinit var friend: Friend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        initialize()
    }

    /**
     * initializeメソッド
     *
     */
    private fun initialize() {
        // roomId設定
        intent.getSerializableExtra(KEY_ROOM).also {
            if(it is Room)
                room = it
            else
                this.onBackPressed()
        }
        // TODO userID必要？
        initBinding()
        initViewModel()
        initLayout()
        initData()
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
    }

    /**
     * MVVMのViewModel
     *
     */
    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(ChatViewModel::class.java).apply {
            // QiitaApiが実行されて正常終了した
            items.observe(this@ChatActivity, Observer {
                binding.apply {
                    ChatView.customAdapter.refresh(it)
                    //swipeRefreshLayout.isRefreshing = false
                }
            })
        }
    }

    /**
     * initLayoutメソッド
     *
     */
    private fun initLayout() {
        initText()
        initEditText()
        initClick()
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
                MessageManager.registerMessage(room.documentId, inputEditText.text.toString())
                hideKeybord()
                inputEditText.text.clear()
            }


        }
    }

    /**
     * initTextメソッド
     *
     */
    private fun initText() {
        // タイトル設定
        chatTitleTextView.text = room.name
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
                    MessageManager.registerMessage(room.documentId, textView.text.toString())
                }
                //true
            }
            false
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
     * hideKeybordメソッド
     *
     */
    private fun hideKeybord() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(inputEditText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    companion object {
        private const val KEY_FRIEND = "key_friend"
        private const val KEY_ROOM = "key_room"
        fun start(context: Context?, room: Room, friend: Friend) =
            context?.startActivity(
                Intent(context, ChatActivity::class.java)
                    .putExtra(KEY_FRIEND, friend)
                    .putExtra(KEY_ROOM, room)
            )
    }
}
