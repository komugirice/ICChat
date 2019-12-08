package com.komugirice.icchat


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.User
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
            UserStore.registerDebugUsers()
            var rooms = MutableLiveData<MutableList<Room>>()
            RoomStore.getLoginUserRooms(rooms)
            rooms.observe(this, androidx.lifecycle.Observer {
                RoomStore.registerDebugRooms(rooms.value, UserStore.getDebugUserList())
                Toast.makeText(
                    context,
                    "デバッグユーザ登録が完了しました。",
                    Toast.LENGTH_LONG
                ).show()

            })
        }

        // 友だち追加
        buttonAddDebugFriend.setOnClickListener {
            val friendId: String = SpinnerUsers.selectedItem.toString()
            UserStore.addFriend(context, friendId)
        }

        buttonRefresh.setOnClickListener{
            initSpinner()
        }
    }


    private fun initSpinner() {
        var adapter: ArrayAdapter<CharSequence>
        var notFriendIdArray = MutableLiveData<Array<CharSequence>>()
        val friendList = MutableLiveData<MutableList<String>>()
        FireStoreUtil.getFriends(friendList)

        friendList.observe(this, androidx.lifecycle.Observer {
            var friendIdList: MutableList<String> = friendList.value ?: mutableListOf()

            UserStore.getDebugNotFriendIdArray(friendIdList, notFriendIdArray)

            notFriendIdArray.observe(this, androidx.lifecycle.Observer {
                context?.also {
                    val value = notFriendIdArray.value ?: arrayOf()
                    adapter = ArrayAdapter<CharSequence>(
                        it, R.layout.row_spinner, value)

                    SpinnerUsers.adapter = adapter
                }
            })

        })

    }

}
