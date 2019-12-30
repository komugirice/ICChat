package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.komugirice.icchat.databinding.ActivityGroupInfoBinding
import com.komugirice.icchat.enum.ActivityEnum
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.store.UserStore
import com.komugirice.icchat.viewModel.GroupInfoViewModel
import kotlinx.android.synthetic.main.activity_group_info.*

class GroupInfoActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupInfoBinding
    private lateinit var viewModel: GroupInfoViewModel
    private lateinit var room: Room

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
                initGroupMemberRecyclerView()
                initInviteUserRecyclerView()
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
