package com.komugirice.icchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.store.MessageStore
import com.komugirice.icchat.databinding.ActivityChatBinding
import com.komugirice.icchat.viewModel.ChatViewModel
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : BaseActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel

    lateinit var room: Room

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
            if(it is Room && it.documentId.isNotEmpty())
                room = it
            else
                this.onBackPressed()
        }
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
            items.observe(this@ChatActivity, Observer {
                binding.apply {
                    ChatView.customAdapter.refresh(it)
                    // 一番下へ移動
                    ChatView.scrollToPosition(ChatView.customAdapter.itemCount - 1)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
            users.observe(this@ChatActivity, Observer {
                binding.apply {
                    ChatView.customAdapter.setUsers(it)
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
        // RecyclerCiewクリックしても発火しない…
//        binding.root.setOnClickListener {
//            hideKeybord(it)
//        }

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
                    MessageStore.registerMessage(room.documentId, textView.text.toString())
                }
                //true
            }
            false
        }

        // swipeRefreshLayoutはクリックしてもフォーカスが変わらない。
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

    companion object {
        private const val KEY_ROOM = "key_room"
        fun start(context: Context?, room: Room) =
            context?.startActivity(
                Intent(context, ChatActivity::class.java)
                    .putExtra(KEY_ROOM, room)
            )
    }
}
