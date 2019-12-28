package com.komugirice.icchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.databinding.ChatMessageCellBinding
import com.komugirice.icchat.databinding.FriendCellBinding
import com.komugirice.icchat.databinding.GroupMemberCellBinding
import com.komugirice.icchat.extension.setRoundedImageView
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Message
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.store.UserStore
import com.komugirice.icchat.util.FireStorageUtil
import kotlinx.android.synthetic.main.activity_group_info.*

class GroupInfoActivity : BaseActivity() {

    private lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)

        initData()
        initClick()
        initGroupMemberRecyclerView()
        initInviteUserRecyclerView()
    }

    private fun initData() {
        intent.getSerializableExtra(KEY_ROOM).also {
            if (it is Room && it.documentId.isNotEmpty()) {
                this.room = it
            } else {
                finish()
            }
        }
        // グループ名
        groupNameTextView.text = room.name
        // グループ画像
        FireStorageUtil.getGroupIconImage(this.room.documentId) {
            groupIconImageView.setRoundedImageView(it) // UIスレッド
        }
    }

    /**
     * initClickメソッド
     *
     */
    private fun initClick() {
        // <ボタン
        backImageView.setOnClickListener {
            this.onBackPressed()
        }
    }

    /**
     * グループメンバー
     *
     */
    private fun initGroupMemberRecyclerView() {

        val userList = mutableListOf<User>()
        val memberList = room.userIdList.filter{ it != UserManager.myUserId }

        // グループメンバーが必ずしもログインユーザの友だちとは限らない
        memberList.forEach {
            UserStore.getTargetUser(it){
                userList.add(it)

                if(userList.size == memberList.size)
                    groupMemberRecyclerView.customAdapter.refresh(userList)
            }
        }
    }

    /**
     * 招待中
     *
     */
    private fun initInviteUserRecyclerView() {
        val userList = mutableListOf<User>()

        room.inviteIdList.forEach {
            UserStore.getTargetUser(it) {
                userList.add(it)
                if(userList.size == room.inviteIdList.size)
                    inviteUserRecyclerView.customAdapter.refresh(userList)

            }
        }
    }

    companion object {

        private const val KEY_ROOM = "key_room"
        fun start(context: Context?, room: Room) {
            context?.startActivity(
                Intent(context, GroupInfoActivity::class.java)
                    .putExtra(KEY_ROOM, room)
            )
        }
    }
}
