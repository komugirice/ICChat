package com.komugirice.icchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityDeleteInterestBinding
import com.komugirice.icchat.viewModel.DeleteInterestViewModel
import kotlinx.android.synthetic.main.activity_delete_interest.*
import kotlinx.android.synthetic.main.activity_header.view.*

class DeleteInterestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteInterestBinding
    private lateinit var viewModel: DeleteInterestViewModel
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }


    private fun initialize() {
        initBinding()
        initViewModel()
        initLayout()
        initClick()
        initSwipeRefreshLayout()
        initData()
    }

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_delete_interest
        )
        binding.lifecycleOwner = this
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(DeleteInterestViewModel::class.java).apply {
            // interest情報更新
            items.observe(this@DeleteInterestActivity, Observer {
                binding.apply {
                    deleteInterestView.customAdapter.refresh(it)
                    // 一番下へ移動
                    if (!viewModel.isNonMove)
                        handler.postDelayed({
                            deleteInterestView.scrollToPosition(deleteInterestView.customAdapter.itemCount - 1)
                        }, 500L)
                    swipeRefreshLayout.isRefreshing = false
                }
            })

        }
    }
    private fun initLayout() {
        // タイトル
        binding.header.titleTextView.text = getString(R.string.delete_interest_activity_title)
    }

    private fun initClick() {
        // 戻る
        binding.header.backImageView.setOnClickListener {
            finish()
        }
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.initData()
        }
    }

    private fun initData(){
        viewModel.initData()
    }

    companion object {

        fun start(context: Context?) {
            context?.startActivity(
                Intent(context, DeleteInterestActivity::class.java)
            )
        }

    }
}
