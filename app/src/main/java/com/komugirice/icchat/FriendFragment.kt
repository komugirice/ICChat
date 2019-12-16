package com.komugirice.icchat


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var viewModel: FriendViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_friend, container, false)

        // initBinding
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        // initViewModel
        viewModel = ViewModelProviders.of(this).get(FriendViewModel::class.java).apply {
            // friends情報更新
            items.observe(this@FriendFragment, Observer {
                binding.apply {
                    // items = it
                    FriendsView.customAdapter.refresh(it)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
        }
        binding.FriendsView.customAdapter.roomForChatActivity.observe(this@FriendFragment, Observer {
            ChatActivity.start(activity, it)
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initLayout()
        viewModel.initData(this@FriendFragment)
    }

    private fun initLayout() {
        initClick()
        initSwipeRefreshLayout()
    }

    private fun initClick() {

    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.initData(this@FriendFragment)
        }
    }



}
