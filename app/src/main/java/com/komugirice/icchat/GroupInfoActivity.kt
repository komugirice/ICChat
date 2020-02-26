package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityGroupInfoBinding
import com.komugirice.icchat.enums.ActivityEnum
import com.komugirice.icchat.enums.RequestStatus
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.viewModel.GroupInfoViewModel
import kotlinx.android.synthetic.main.activity_group_info.*

class GroupInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupInfoBinding
    private lateinit var viewModel: GroupInfoViewModel
    //private lateinit var room: Room

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
            groupRequests.observe(this@GroupInfoActivity, Observer {
                binding.apply {
                    room = it.room
                }
                initGroupMemberRecyclerView()
            })
        }
    }

    private fun initData() {
        if(!viewModel.initRoom(intent))
            finish()
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
     * GroupMemberRecyclerView設定
     *
     */
    private fun initGroupMemberRecyclerView() {

        val memberList = mutableListOf<User>()
        val inviteList = mutableListOf<User>()

        //グループメンバー
        viewModel.groupRequests.value?.room?.userIdList?.forEach { memberId ->
            val user = UserManager.allUsers.filter{it.userId == memberId}.firstOrNull()
            if(user != null) memberList.add(user)
        }
        // 招待中
        viewModel.groupRequests.value?.requests?.forEach { request ->
            val user = UserManager.allUsers.filter { it.userId == request.beRequestedId && request.status == RequestStatus.REQUEST.id }.firstOrNull()
            if (user != null) inviteList.add(user)
        }

        groupMemberRecyclerView.customAdapter.refresh(memberList, inviteList)
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
