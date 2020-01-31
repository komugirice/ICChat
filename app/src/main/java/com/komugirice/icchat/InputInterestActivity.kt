package com.komugirice.icchat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityInputInterestBinding
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.viewModel.InputInterestViewModel
import com.komugirice.icchat.viewModel.InterestViewModel
import kotlinx.android.synthetic.main.activity_header.view.*

class InputInterestActivity : BaseActivity() {

    private lateinit var binding: ActivityInputInterestBinding
    private lateinit var viewModel: InputInterestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initBinding()
        initViewModel()
        initLayout()
        initClick()
    }

    private fun initBinding(){
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_input_interest
        )
        binding.lifecycleOwner = this
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(InputInterestViewModel::class.java).apply {

        }
    }


    private fun initLayout(){
        // タイトル
        binding.header.titleTextView.text = getString(R.string.input_interest_activity_title)
    }

    private fun initClick(){
        // 戻る
        binding.header.backImageView.setOnClickListener {
            finish()
        }

        binding.container.setOnClickListener {
            hideKeybord(it)
        }
    }


    companion object {
        private const val KEY_INTEREST = "key_interest"

        fun start(context: Context?) {
            context?.startActivity(
                Intent(context, InputInterestActivity::class.java)
            )
        }

        fun update(context: Context?, interest: Interest?) =
            context?.startActivity(
                Intent(context, InputInterestActivity::class.java)
                    .putExtra(KEY_INTEREST, interest)
            )

    }
}
