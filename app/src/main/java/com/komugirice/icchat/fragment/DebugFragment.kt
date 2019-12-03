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
import com.komugirice.icchat.data.firestore.User
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
            registerUsers()
        }

        // 友だち追加
        buttonAddDebugFriend.setOnClickListener {
            val friendId: String = SpinnerUsers.selectedItem.toString()
            val userId = FireStoreUtil.getLoginUserId()
            FirebaseFirestore.getInstance()
                .collection("friends/$userId/friends")
                .add(Friend().apply{
                    this.userId = friendId
                }).addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "友だち登録が完了しました。ID:$friendId",
                        Toast.LENGTH_LONG
                    ).show()
                }

        }
    }

    private fun registerUsers() {
        FirebaseFirestore.getInstance()
            .collection("user")
            .get()
            .addOnCompleteListener {
                var currentUserPairs = mutableListOf<Pair<String, User>>()
                var userIdDocumentIdMap = mutableMapOf<String, String>()
                if (it.isSuccessful) {
                    it.result?.documents?.forEach { documentSnapshot ->
                        // documentSnapshot.idがデータ更新できる主キー
                        userIdDocumentIdMap[documentSnapshot.get("userId").toString()] = documentSnapshot.id
                    }
                    it.result?.toObjects(User::class.java)?.also {
                        it.map {
                            // Userクラス自身のuserIdで紐付けている
                            val documentId = userIdDocumentIdMap[it.userId]
                            //documentIdが無い場合はmapに含めない
                            if (documentId == null)
                                null
                            else
                                Pair(documentId, it)
                        }.forEach {
                            if (it != null)
                                currentUserPairs.add(it)
                        }
                    }
                }
                registerUsers(currentUserPairs)
            }
    }

    private fun registerUsers(currentUsers: List<Pair<String, User>>) {
        var thisUserlist: MutableList<User> = mutableListOf()
        //userIdが000000〜000009のユーザだけ作成。
        for (i in 0..9) {
            thisUserlist.add(
                User().apply {
                    userId = "00000" + i.toString()
                    name = "ユーザ_" + "00000" + i.toString()
                    val birthString = ("199" + i.toString() + "/" + (i + 1).toString() + "/" + (i+1).toString())
                    birthDay = birthString.toDate("yyyy/MM/dd")
                }
            )
        }
        val currentUserIds = currentUsers.map { it.second.userId }
        thisUserlist.forEach { user ->
            if (currentUserIds.contains(user.userId)) {
                // userIdが一致した先頭レコードのdocumentIdを設定する
                val documentId = currentUsers.firstOrNull { it.second.userId ==  user.userId}?.first ?: UUID.randomUUID().toString()
                // documentIdをキーにして更新
                FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(documentId)
                    .set(user)
            } else {
                // 新規登録（documentIdが主キー）
                FirebaseFirestore.getInstance()
                    .collection("user")
                    .document(user.documentId)
                    .set(user)
            }
        }
        Toast.makeText(
            context,
            "デバッグユーザ登録が完了しました。",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun initSpinner() {
        var adapter: ArrayAdapter<CharSequence>
        val userId = FireStoreUtil.getLoginUserId()
        var notFriendList: MutableList<User> = mutableListOf()
        val friendList = MutableLiveData<MutableList<String>>()
        FireStoreUtil.getFriends(friendList)

        friendList.observe(this, androidx.lifecycle.Observer {
            var friendIdList: MutableList<String> = friendList.value ?: mutableListOf()

            // user情報取得
            FirebaseFirestore.getInstance()
                .collection("user")
                // TODO なぜかwhereがうまくいかない。
                //.whereGreaterThan("userId", userId)
                //.whereLessThan("userId", userId)
                .orderBy(User::userId.name)
                .limit(10)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(User::class.java)?.also {
                            // とりあえずuser全件をnotFriendListに格納
                            notFriendList = it
                        }
                        // notFriendList: MutableList<User> → notFriendIdList: MutableList<String>
                        var notFriendIdList: MutableList<String> = notFriendList.map { it.userId }.toMutableList()
                        // notFriendIdListからfirendIdListを除外
                        notFriendIdList.removeAll(friendIdList)
                        // 自分のIDも除外
                        notFriendIdList.remove(FireStoreUtil.getLoginUserId())

                        // Spinner.adapterがarrayしか受け付けないので変換
                        val userIdArray = notFriendIdList.toTypedArray()
                        context?.also {
                            adapter = ArrayAdapter<CharSequence>(
                                it, R.layout.row_spinner,
                                userIdArray
                            )
                            SpinnerUsers.adapter = adapter
                        }
                    }
                }
        })

    }

}
