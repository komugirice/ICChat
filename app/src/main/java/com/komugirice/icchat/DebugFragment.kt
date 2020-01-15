package com.komugirice.icchat


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.komugirice.icchat.interfaces.Update
import com.komugirice.icchat.firebase.fcm.FcmApi
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.firebase.firestore.store.DebugUserStore
import com.komugirice.icchat.firebase.firestore.store.RoomStore
import com.komugirice.icchat.firebase.firestore.store.UserStore
import kotlinx.android.synthetic.main.fragment_debug.*

/**
 * A simple [Fragment] subclass.
 */
class DebugFragment : Fragment(), Update {

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
            val friendId: String = SpinnerAddUsers.selectedItem.toString()
            var rooms: MutableList<Room> = mutableListOf()
            FirebaseFacade.addFriend(friendId,{}){
                Toast.makeText(
                    context,
                    "友だち登録が完了しました。",
                    Toast.LENGTH_LONG
                ).show()
            }

        }

        // 友だち削除
        buttonDelDebugFriend.setOnClickListener {
            val friendId: String = SpinnerDelUsers.selectedItem.toString()

            // User削除
            UserStore.delFriend(friendId) {

                // Room削除
                RoomStore.getLoginUserRooms() {
                    RoomStore.delSingleUserRooms(it, friendId)
                    Toast.makeText(
                        context,
                        "友だち削除が完了しました。",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        // FCM通知
        buttonfcmSend.setOnClickListener {
            val friendId: String = SpinnerDelUsers.selectedItem.toString()
            sendFcm(friendId)
        }


        buttonRefresh.setOnClickListener{
            initSpinner()
        }
    }


    private fun initSpinner() {
        var adapter: ArrayAdapter<CharSequence>
        var tmpList: List<User>

        // NotFriend
        DebugUserStore.getDebugNotFriendIdArray() {
            if (it.isSuccessful) {
                it.result?.toObjects(User::class.java)?.also {
                    // とりあえずuser全件をnotFriendListに格納
                    tmpList = it

                    var notFriendIdList: MutableList<String> =
                        tmpList.map { it.userId }.toMutableList()
                    // notFriendIdListからfirendIdListを除外
                    notFriendIdList.removeAll(UserManager.myUser.friendIdList)
                    // 自分のIDも除外
                    notFriendIdList.remove(UserManager.myUserId)

                    //notFriendIdList = notFriendIdList.map { it.substring(0, 10) }.toMutableList()

                    context?.also {
                        adapter = ArrayAdapter<CharSequence>(
                            // Spinner.adapterがarrayしか受け付けないので変換
                            it, R.layout.row_spinner, notFriendIdList.toTypedArray()
                        )
                        SpinnerAddUsers.adapter = adapter
                    }

                }
            }
        }
        // Friend
        context?.also {
            //var friendIdList: MutableList<String> =
            //    UserManager.myUser.friendIdList.map { it.substring(0, 10) }.toMutableList()
            val friendIdList = UserManager.myUser.friendIdList
            adapter = ArrayAdapter<CharSequence>(
                // Spinner.adapterがarrayしか受け付けないので変換

                it, R.layout.row_spinner, friendIdList.toTypedArray()
            )
            SpinnerDelUsers.adapter = adapter
        }

    }

    private fun sendFcm(friendId: String) {
        val  friend = UserManager.getMyFriend(friendId)
        friend?.fcmToken?.apply{
            val message = getString(R.string.fcm_friend_request, friend.name)
            val token = friend.fcmToken
            val type = "0"
            FcmApi.sendMessage(token, message, type)
        } ?: run{
            Toast.makeText(
                context,
                "FCMトークンがnullです",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun update() {

    }

}
