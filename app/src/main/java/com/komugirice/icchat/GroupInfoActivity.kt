package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityGroupInfoBinding
import com.komugirice.icchat.enum.ActivityEnum
import com.komugirice.icchat.firebase.firestore.manager.RequestManager
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.GroupRequests
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.viewModel.GroupInfoViewModel
import kotlinx.android.synthetic.main.activity_group_info.*

class GroupInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupInfoBinding
    private lateinit var viewModel: GroupInfoViewModel
    private lateinit var room: Room
    private var groupRequests: GroupRequests? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBinding()
        initViewModel()
        initData()
        initClick()
    }

    /**
     * MVVMのBinding
     *
     */
    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_group_info
        )
        binding.lifecycleOwner = this
    }

    /**
     * MVVMのViewModel
     *
     */
    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(GroupInfoViewModel::class.java).apply {
            room.observe(this@GroupInfoActivity, Observer {
                binding.apply {
                    room = it
                }
                this@GroupInfoActivity.room = it
                initGroupRequests(it)
                initGroupMemberRecyclerView()
                initInviteUserRecyclerView()
            })
        }
    }

    private fun initData() {
        if(!viewModel.initRoom(intent))
            finish()
    }

    private fun initGroupRequests(room: Room) {
        groupRequests = RequestManager.myGroupsRequests
            .filter{ it.room.documentId == room.documentId}.firstOrNull()

    }


    /**
     * initClickメソッド
     *
     */
    private fun initClick() {
        // <ボタン
        backImageView.setOnClickListener {
            setResult(Activity.RESULT_OK, intent)
            this.onBackPressed()
        }
    }

    /**
     * グループメンバー
     *
     */
    private fun initGroupMemberRecyclerView() {

        val userList = mutableListOf<User>()
        val memberList = room.userIdList

        memberList.forEach { memberId ->
            val user = UserManager.allUsers.filter{it.userId == memberId}.firstOrNull()
            if(user != null) userList.add(user)
        }

        groupMemberRecyclerView.customAdapter.refresh(userList)
    }

    /**
     * 招待中
     *
     */
    private fun initInviteUserRecyclerView() {
        val userList = mutableListOf<User>()

        groupRequests?.requests?.forEach { request ->
            val user = UserManager.allUsers.filter { it.userId == request.beRequestedId }.firstOrNull()
            if (user != null) userList.add(user)
        }
        inviteUserRecyclerView.customAdapter.refresh(userList)

    }

    companion object {

        const val KEY_ROOM = "key_room"
        fun start(context: Context?, room: Room) {
            context?.startActivity(
                Intent(context, GroupInfoActivity::class.java)
                    .putExtra(KEY_ROOM, room)
            )
        }
        fun startActivityForResult(activity: Activity?, room: Room) {
            activity?.also {
                it.startActivityForResult(
                    Intent(it, GroupInfoActivity::class.java)
                        .putExtra(KEY_ROOM, room)
                    , ActivityEnum.GroupInfoActivity.id
                )
            }
        }
    }
}
