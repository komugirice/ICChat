package com.komugirice.icchat


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.data.firestore.model.Room
import com.komugirice.icchat.data.firestore.store.DebugUserStore
import com.komugirice.icchat.data.firestore.store.RoomStore
import com.komugirice.icchat.data.firestore.store.UserStore
import com.komugirice.icchat.util.FireStoreUtil
import kotlinx.android.synthetic.main.fragment_debug.*

/**
 * A simple [Fragment] subclass.
 */
class DebugFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_debug, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initLayout()
        initSpinner()
    }

    private fun initLayout() {
        initClick()
    }

    private fun initClick() {
        // デバッグユーザ追加
        buttonAddDebugUser.setOnClickListener {
            DebugUserStore.registerLoginUser()

            Toast.makeText(
                context,
                "ユーザ情報登録が完了しました。",
                Toast.LENGTH_LONG
            ).show()


        }

        // 友だち追加
        buttonAddDebugFriend.setOnClickListener {
            val friendId: String = SpinnerUsers.selectedItem.toString()
            UserStore.addFriend(context, friendId)

            var rooms = MutableLiveData<MutableList<Room>>()
            RoomStore.getLoginUserRooms(rooms)
            rooms.observe(this, androidx.lifecycle.Observer {
                RoomStore.registerSingleUserRooms(rooms.value, friendId)
            })
        }

        buttonRefresh.setOnClickListener{
            initSpinner()
        }
    }


    private fun initSpinner() {
        var adapter: ArrayAdapter<CharSequence>
        var notFriendIdArray = MutableLiveData<Array<CharSequence>>()

        DebugUserStore.getDebugNotFriendIdArray(notFriendIdArray)

        notFriendIdArray.observe(this, androidx.lifecycle.Observer {
            context?.also {
                val value = notFriendIdArray.value ?: arrayOf()
                adapter = ArrayAdapter<CharSequence>(
                    it, R.layout.row_spinner, value)

                SpinnerUsers.adapter = adapter
            }
        })

    }

}
