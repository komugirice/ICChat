package com.komugirice.icchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityDeleteInterestBinding
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.store.InterestStore
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

        // 削除メニュー押下
        binding.deleteInterestView.customAdapter.onClickDeleteCallBack = {
            viewModel.initData(isNonMove = true)
        }
        // URL記事押下
        binding.deleteInterestView.customAdapter.onClickUrlCallBack = {
            val intent = Intent(Intent.ACTION_VIEW, it)
            startActivity(intent)
        }
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

        // 削除ボタン
        binding.deleteButton.setOnClickListener {
            deleteComplete()
        }
        // 復元ボタン
        binding.restoreButton.setOnClickListener {
            restore()
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

    private fun deleteComplete() {
        var completeCnt: MutableLiveData<Int> = MutableLiveData(0)

        var targets = viewModel.items.value?.filter{it.isChecked}

        if(targets == null || targets.isEmpty()){
            // データが選択されていません
            Toast.makeText(this,
                getString(R.string.invalid_check),
                Toast.LENGTH_LONG)
                .show()
            return
        }

        targets?.forEach {
            // 物理削除
            it.interest?.apply {
                FirebaseFacade.deleteCompleteInterest(this) {
                    completeCnt.postValue(completeCnt.value?.plus(1))
                }
            }
        }

        completeCnt.observe(this@DeleteInterestActivity, Observer {
            if (completeCnt.value == targets?.size) {
                // 完了メッセージ
                Toast.makeText(
                    this,
                    getString(R.string.delete_complete),
                    Toast.LENGTH_LONG
                ).show()
                initData()
            }
        })
    }

    private fun restore() {
        var completeCnt: MutableLiveData<Int> = MutableLiveData(0)

        var targets = viewModel.items.value?.filter{it.isChecked}

        if(targets == null || targets.isEmpty()){
            // データが選択されていません
            Toast.makeText(this,
                getString(R.string.invalid_check),
                Toast.LENGTH_LONG)
                .show()
            return
        }

        targets?.forEach {
            // 復元
            it.interest?.apply {
                InterestStore.restoreInterest(this) {
                    completeCnt.postValue(completeCnt.value?.plus(1))
                }
            }

        }

        completeCnt.observe(this@DeleteInterestActivity, Observer {
            if (completeCnt.value == targets?.size) {
                // 完了メッセージ
                Toast.makeText(
                    this,
                    getString(R.string.restore_complete),
                    Toast.LENGTH_LONG
                ).show()
                initData()
            }
        })
    }


    companion object {

        fun start(context: Context?) {
            context?.startActivity(
                Intent(context, DeleteInterestActivity::class.java)
            )
        }

    }
}
