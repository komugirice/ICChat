package com.komugirice.icchat


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.FragmentInterestBinding
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.interfaces.Update
import com.komugirice.icchat.viewModel.InterestViewModel
import kotlinx.android.synthetic.main.fragment_friend.swipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_interest.*

/**
 * A simple [Fragment] subclass.
 */
class InterestFragment : Fragment(), Update {

    private lateinit var binding: FragmentInterestBinding
    private lateinit var interestViewModel: InterestViewModel
    private val handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_interest, container, false)

        // initBinding
        binding = FragmentInterestBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        // 削除メニュー押下
        binding.interestView.customAdapter.onClickDeleteCallBack = {
            interestViewModel.initData(isNonMove = true)
        }
        // URL記事押下
        binding.interestView.customAdapter.onClickUrlCallBack = {
            val intent = Intent(Intent.ACTION_VIEW, it)
            startActivity(intent)
        }

        interestViewModel = ViewModelProviders.of(this).get(InterestViewModel::class.java).apply {
            // interest情報更新
            items.observe(this@InterestFragment, Observer {
                binding.apply {
                    interestView.customAdapter.refresh(it)
                    // 一番下へ移動
                    if (!interestViewModel.isNonMove)
                        handler.postDelayed({
                            interestView.scrollToPosition(interestView.customAdapter.itemCount - 1)
                        }, 500L)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
            // userId
            mutableUserId.observe(this@InterestFragment, Observer{
                interestView.customAdapter.updateUserId(it)
            })
            // 編集モード
            isEditMode.observe(this@InterestFragment, Observer{
                binding.isMyUser = it
                interestView.customAdapter.updateEditMode(it)
            })
            // initDataでmutableLiveDataがnullになったので分離
            //userId = arguments?.getString(KEY_USER_ID) ?: UserManager.myUserId
            mutableUserId.value = arguments?.getString(KEY_USER_ID) ?: UserManager.myUserId
            isEditMode.postValue(UserManager.myUserId == mutableUserId.value)

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        initData()
        interestViewModel.initData()
    }

    /**
     * ユーザID更新
     */
    fun updateUserId(userId: String) {
        interestViewModel.updateUserId(userId)
        initData()
    }

    private fun initialize() {
        initClick()
        initSwipeRefreshLayout()
    }

    private fun initData(){
        // ユーザ名設定
        nameTextView.text = UserManager.getTargetUser(interestViewModel.mutableUserId.value ?: "")?.name ?: ""
    }

    private fun initClick() {
        binding.inputButton.setOnClickListener {
            InputInterestActivity.start(context)
        }
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            interestViewModel.initData(true)
        }
    }

    /**
     * 遷移先のActivityから戻ってきた場合にリロードする
     */
    override fun update() {
        initData()
        interestViewModel.initData()
    }

    companion object {
        const val KEY_USER_ID = "key_user_id"
    }
}
