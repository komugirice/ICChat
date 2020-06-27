package com.komugirice.icchat


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.interfaces.Update
import com.komugirice.icchat.databinding.FragmentRoomBinding
import com.komugirice.icchat.firebase.firestore.manager.RoomManager
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.store.MessageStore
import com.komugirice.icchat.view.RoomsView
import com.komugirice.icchat.viewModel.RoomViewModel
import kotlinx.android.synthetic.main.fragment_friend.*

/**
 * A simple [Fragment] subclass.
 */
class RoomFragment : Fragment(), Update {

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

    /**
     * 遷移先のActivityから戻ってきた場合にリロードする
     */
    override fun update() {
        // チャット画面から戻る場合の為、messageだけ更新する
        binding.RoomsView.customAdapter.getRoomCellBindingList().forEach { roomCellBinding->
            roomCellBinding.room?.apply {
                MessageStore.getLastMessage(this.documentId) {
                    if (it.isSuccessful) {
                        val message = it.result?.toObjects(Message::class.java)?.firstOrNull()
                        roomCellBinding.message = message
                        binding.RoomsView.customAdapter.updateItemMessage(this, message)
                    }
                }
            }
        }

    }

}
