package com.komugirice.icchat

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.komugirice.icchat.databinding.ActivitySearchUserBinding
import com.komugirice.icchat.extension.afterTextChanged
import com.komugirice.icchat.firestore.firebaseFacade
import com.komugirice.icchat.firestore.manager.RequestManager
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.store.RequestStore
import com.komugirice.icchat.firestore.store.UserStore
import com.komugirice.icchat.ui.groupSetting.GroupSettingViewModel
import com.komugirice.icchat.viewModel.SearchUserViewModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.backImageView
import kotlinx.android.synthetic.main.activity_group_setting.*
import kotlinx.android.synthetic.main.activity_search_user.*
import kotlinx.android.synthetic.main.activity_search_user.container
import timber.log.Timber
import java.net.URLEncoder

class SearchUserActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchUserBinding
    private lateinit var viewModel: SearchUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBinding()
        initViewModel()
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
    fun initEditText() {
        // 検索キーワード
        this@SearchUserActivity.searchEditText.apply{
            // フォーカスアウト
            this.setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus && v is EditText) {
                    val inputText = v.text.toString()
                    search()
                }
            }
            // キーボードEnter
            this.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE && v is EditText) {
                    val inputText = v.text.toString()
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
        backImageView.setOnClickListener {
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
        firebaseFacade.requestFriend(viewModel._requestUser) {
            AlertDialog.Builder(this)
                .setMessage("友だち申請しました")
                .setPositiveButton("OK", object : DialogInterface.OnClickListener {
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
