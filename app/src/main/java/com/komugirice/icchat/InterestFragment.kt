package com.komugirice.icchat


import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_friend.*

/**
 * A simple [Fragment] subclass.
 */
class InterestFragment : Fragment(), Update {

    private lateinit var binding: FragmentInterestBinding
    private lateinit var interestViewModel: InterestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_interest, container, false)

        // initBinding
        binding = FragmentInterestBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        interestViewModel = ViewModelProviders.of(this).get(InterestViewModel::class.java).apply {
            // interest情報更新
            items.observe(this@InterestFragment, Observer {
                binding.apply {
                    InterestView.customAdapter.refresh(it)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
            userId = arguments?.getString(KEY_USER_ID) ?: UserManager.myUserId
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        // TODO userId取得
        interestViewModel.initData()
    }

    fun updateUserId(userId: String) {
        interestViewModel.updateData(userId)
    }

    private fun initialize() {
        initSwipeRefreshLayout()
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            interestViewModel.initData()
        }
    }

    /**
     * 遷移先のActivityから戻ってきた場合にリロードする
     */
    override fun update() {

    }

    companion object {
        const val KEY_USER_ID = "key_user_id"
    }
}
