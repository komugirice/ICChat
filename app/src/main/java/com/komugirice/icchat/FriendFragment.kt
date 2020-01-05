package com.komugirice.icchat


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.FragmentFriendBinding
import com.komugirice.icchat.viewModel.FriendViewModel
import kotlinx.android.synthetic.main.fragment_friend.*

/**
 * A simple [Fragment] subclass.
 */
class FriendFragment : Fragment() {

    private lateinit var binding: FragmentFriendBinding
    private lateinit var friendsViewModel: FriendViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_friend, container, false)

        // initBinding
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        // initFriendView
        binding.FriendsView.customAdapter.onClickCallBack = {
            friendsViewModel.initData(this@FriendFragment)
        }

        friendsViewModel = ViewModelProviders.of(this).get(FriendViewModel::class.java).apply {
            // friends情報更新
            items.observe(this@FriendFragment, Observer {
                binding.apply {
                    FriendsView.customAdapter.refresh(it)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    override fun onResume() {
        super.onResume()
        if(friendsViewModel.initFlg == false)
            friendsViewModel.initData(this@FriendFragment)
    }

    private fun initialize() {
        initLayout()

    }

    private fun initLayout() {
        initClick()
        initSwipeRefreshLayout()
    }

    private fun initClick() {

    }

    private val onClickCallBack: () ->Unit = {
        friendsViewModel.initData(this@FriendFragment)
    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            friendsViewModel.initData(this@FriendFragment)
        }
    }



}
