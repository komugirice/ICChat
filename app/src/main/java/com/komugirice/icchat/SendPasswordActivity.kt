package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.komugirice.icchat.databinding.ActivitySendPasswordBinding
import kotlinx.android.synthetic.main.activity_header.view.*

class SendPasswordActivity : BaseActivity() {

    private lateinit var binding: ActivitySendPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_password)
        initialize()
    }

    private fun initialize() {
        initBinding()
        initLayout()
        initClick()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_send_password
        )
        binding.lifecycleOwner = this
    }

    private fun initLayout() {
        // タイトル
        binding.header.titleTextView.text = getString(R.string.send_password_activity_title)
    }

    private fun initClick() {
        // 戻る
        binding.header.backImageView.setOnClickListener {
            finish()
        }

        // 戻るボタン
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.root.setOnClickListener {
            hideKeybord(it)
        }
        binding.contents.setOnClickListener {
            hideKeybord(it)
        }
    }

    companion object {
        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, SendPasswordActivity::class.java)
            )
    }
}
