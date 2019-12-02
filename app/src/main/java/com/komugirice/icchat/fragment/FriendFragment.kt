package com.komugirice.icchat.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.FriendsView
import com.komugirice.icchat.R
import com.komugirice.icchat.data.firestore.Friend
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.util.FireStoreUtil
import kotlinx.android.synthetic.main.fragment_friend.*

/**
 * A simple [Fragment] subclass.
 */
class FriendFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initLayout()
        initData()
    }

    private fun initLayout() {
        initClick()
        initSwipeRefreshLayout()
    }

    private fun initClick() {

    }

    private fun initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener {
            initData()
        }
    }
    private fun initData() {
        val myUserId = FireStoreUtil.getLoginUserId()
        var userList: MutableList<User> = mutableListOf()
        val friendList = MutableLiveData<MutableList<String>>()
        FireStoreUtil.getFriend(friendList)

        friendList.observe(this, androidx.lifecycle.Observer {

                        // ユーザ情報取得
            friendList.value?.also {
                it.forEach {
                    FirebaseFirestore.getInstance()
                        .collection("user")
                        .whereEqualTo("userId", it)
                        .get()
                        .addOnCompleteListener {
                            // IOスレッドなので、レイアウト関係を扱えないので注意
                            if (!it.isSuccessful)
                                return@addOnCompleteListener
                            // TODO 取得は複数件しか扱えないのか
                            it.result?.toObjects(User::class.java)?.also { users ->
                                userList.add(users[0])
                            }
                            // 後処理
                            swipeRefreshLayout.isRefreshing = false
                            FriendsView.customAdapter.refresh(userList)

                        }
                }
            }
        })
    }


}
