package com.komugirice.icchat

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivitySearchUserBinding
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.RequestManager
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.viewModel.SearchUserViewModel
import kotlinx.android.synthetic.main.activity_chat.backImageView
import kotlinx.android.synthetic.main.activity_header.view.*
import kotlinx.android.synthetic.main.activity_search_user.*

class SearchUserActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchUserBinding
    private lateinit var viewModel: SearchUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViewModel()
        initLayout()
        initEditText()
        initClick()
    }

    /**
     * MVVMのBinding
     *
     */
    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_search_user
        )
        binding.lifecycleOwner = this
    }

    /**
     * MVVMのViewModel
     *
     */
    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(SearchUserViewModel::class.java).apply {
            canSubmit.observe(this@SearchUserActivity, Observer {
                binding.canSubmit = it
            })
        }
    }

    private fun initLayout() {
        binding.header.titleTextView.text = getString(R.string.user_search_title)
    }
    private fun initEditText() {
        // 検索キーワード
        binding.searchEditText.apply{
            // フォーカスアウト
            this.setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus && v is EditText) {
                    search()
                }
            }
            // キーボードEnter
            this.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE && v is EditText) {
                    search()
                    true
                }
                false
            }

        }
    }

    fun search() {
        val inputText = searchEditText.text.toString()
        if(mailRadioButton.isChecked == true) {
            searchEmail(inputText)
        } else {
            searchName(inputText)
        }
    }

    /**
     * EMail検索
     *
     */
    fun searchEmail(inputText: String){
        UserStore.searchNotFriendUserEmail(inputText, { initCheckBox(null) }) {
            initCheckBox(it)
        }
    }

    /**
     * ユーザ名検索
     *
     */
    fun searchName(inputText: String){
        UserStore.searchNotFriendUserName(inputText, { initCheckBox(null) }) {
            initCheckBox(it)
        }
    }

    fun initClick() {
        // <ボタン
        binding.header.backImageView.setOnClickListener {
            this.onBackPressed()
        }

        searchButton.setOnClickListener {
            search()
        }

        friendRequestButton.setOnClickListener {
            requestFriend()
        }
        container.setOnClickListener {
            hideKeybord(it)
        }
        contents.setOnClickListener {
            hideKeybord(it)
        }
    }

    fun initCheckBox(userList: List<User>?) {
        userCheckBoxContainer.removeAllViews()
        userList?.also{
            it.forEach {
                val checkBox = CheckBox(this)
                checkBox.text = it.name
                checkBox.textSize = 16f

                // 対象"外"にRequest 0:申請中, 1:承認, 2:拒否の全てを含める（承認も結局User.friendListに含まれるため）
                val requesterIds = RequestManager.myUserRequests.map{it.beRequestedId}
                    .plus(RequestManager.usersRequestToMe.map{it.requesterId})
                if(requesterIds.contains(it.userId)) {
                    checkBox.isEnabled = false
                }

                checkBox.setOnCheckedChangeListener { _v, isChecked ->
                    viewModel.apply {
                        if(isChecked) {
                            _requestUser.add(it)
                        } else {
                            _requestUser.remove(it)
                        }
                        requestUser.postValue(_requestUser)
                    }
                }
                userCheckBoxContainer.addView(checkBox)
            }
        } ?: run {
            val textView = TextView(this)
            textView.text = getString(R.string.invalid_search_result)
            userCheckBoxContainer.addView(textView)
        }
    }

    fun requestFriend() {
        FirebaseFacade.requestFriend(viewModel._requestUser) {
            AlertDialog.Builder(this)
                .setMessage(R.string.success_user_request)
                .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        finish()
                    }
                })
                .setOnDismissListener(object : DialogInterface.OnDismissListener {
                    override fun onDismiss(dialog: DialogInterface?) {
                        finish()
                    }
                })
                .show()
        }
    }

    companion object {
        fun start(context: Context?) =
            context?.startActivity(
                Intent(context, SearchUserActivity::class.java)
            )
    }
}
