package com.komugirice.icchat.fragment


import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.getIdFromEmail
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.R
import com.komugirice.icchat.data.firestore.Friend
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.data.firestore.manager.FriendManager
import com.komugirice.icchat.data.firestore.manager.RoomManager
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.ui.login.LoginActivity
import com.komugirice.icchat.ui.login.LoginViewModel
import com.komugirice.icchat.util.FireStoreUtil
import kotlinx.android.synthetic.main.fragment_debug.*
import java.util.*

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
            //UserManager.registerDebugUsers(context)
            var rooms = MutableLiveData<MutableList<Pair<Room, List<Friend>>>>()
            RoomManager.getLoginUserRooms(rooms)
            rooms.observe(this, androidx.lifecycle.Observer {
                Log.d("rooms", it.toString())
            })
        }

        // 友だち追加
        buttonAddDebugFriend.setOnClickListener {
            val friendId: String = SpinnerUsers.selectedItem.toString()
            FriendManager.addFriend(context, friendId)

        }
    }


    private fun initSpinner() {
        var adapter: ArrayAdapter<CharSequence>
        var notFriendIdArray = MutableLiveData<Array<CharSequence>>()
        val friendList = MutableLiveData<MutableList<String>>()
        FireStoreUtil.getFriends(friendList)

        friendList.observe(this, androidx.lifecycle.Observer {
            var friendIdList: MutableList<String> = friendList.value ?: mutableListOf()

            UserManager.getDebugNotFriendIdArray(context, friendIdList, notFriendIdArray)

            notFriendIdArray.observe(this, androidx.lifecycle.Observer {
                context?.also {
//                    adapter = ArrayAdapter<CharSequence>(
//                        it, R.layout.row_spinner, notFriendIdArray?.value)
//
//                    SpinnerUsers.adapter = adapter
                }
            })

        })

    }

}
