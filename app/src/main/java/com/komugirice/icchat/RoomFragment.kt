package com.komugirice.icchat


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.FragmentRoomBinding
import com.komugirice.icchat.viewModel.RoomViewModel
import kotlinx.android.synthetic.main.fragment_friend.*

/**
 * A simple [Fragment] subclass.
 */
class RoomFragment : Fragment() {

    private lateinit var binding: FragmentRoomBinding
    private lateinit var viewModel: RoomViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_room, container, false)
        binding = FragmentRoomBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        viewModel = ViewModelProviders.of(this).get(RoomViewModel::class.java).apply {

            items.observe(this@RoomFragment, Observer {
                binding.apply{
                    RoomsView.customAdapter.refresh(it)
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
        if(viewModel.initFlg == false)
            viewModel.initData(this)
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

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.initData(this)
        }
    }

}
