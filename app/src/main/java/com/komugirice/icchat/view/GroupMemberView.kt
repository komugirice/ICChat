package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.R
import com.komugirice.icchat.databinding.GroupMemberCellBinding
import com.komugirice.icchat.databinding.TitleCellBinding
import com.komugirice.icchat.firebase.firestore.model.Request
import com.komugirice.icchat.firebase.firestore.model.User

class GroupMemberView : RecyclerView {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    val customAdapter by lazy {
        Adapter(
            context
        )
    }

    init {
        adapter = customAdapter
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
    }

    class Adapter(val context: Context) : RecyclerView.Adapter<ViewHolder>() {


        private val groupUsers = mutableListOf<User>()
        private val inviteUsers = mutableListOf<User>()

        private fun getGroupUsersPositionOffset(): Int = 1

        private fun getInviteRequestsOffset(): Int {
            var result = 2
            result += if (groupUsers.isEmpty()) 1 else groupUsers.size
            return result
        }

        fun refresh(members: List<User>, invites: List<User>) {
            groupUsers.apply {
                clear()
                addAll(members)
            }
            inviteUsers.apply {
                clear()
                addAll(invites)
            }
            notifyDataSetChanged()
        }

        // groupUsers[position - getGroupUsersPositionOffset()]

        override fun getItemCount(): Int {
            var count = 2 // メンバーセクションと招待中セクション
            count += if (groupUsers.isEmpty()) 1 else groupUsers.size
            count += if (inviteUsers.isEmpty()) 1 else inviteUsers.size
            return count
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0)
                return ViewType.MEMBER_SECTION.code
            if (position < getInviteRequestsOffset() - 1) {
                return if (groupUsers.isEmpty())
                    ViewType.MEMBER_EMPTY.code
                else
                    ViewType.MEMBER_VIEW.code
            }
            if (position == getInviteRequestsOffset() - 1)
                return ViewType.INVITE_SCETION.code
            if (inviteUsers.isEmpty())
                return ViewType.INVITE_EMPTY.code
            return ViewType.INVITE_VIEW.code
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            when(ViewType.values().first { it.code == viewType }) {
                ViewType.MEMBER_SECTION, ViewType.INVITE_SCETION -> {
                    SectionCellViewHolder(
                        TitleCellBinding.inflate(
                            LayoutInflater.from(
                                context
                            ), parent, false
                        )
                    )
                }
                ViewType.MEMBER_VIEW -> {
                    GroupMemberCellViewHolder(
                        GroupMemberCellBinding.inflate(
                            LayoutInflater.from(
                                context
                            ), parent, false
                        )
                    )
                }
                ViewType.INVITE_VIEW -> {
                    InviteUserCellViewHolder(
                        GroupMemberCellBinding.inflate(
                            LayoutInflater.from(
                                context
                            ), parent, false
                        )
                    )
                }
                // MEMBER_EMPTY, INVITE_EMPTY
                else -> {
                    EmptyCellViewHolder(
                        TitleCellBinding.inflate(
                            LayoutInflater.from(
                                context
                            ), parent, false
                        )
                    )
                }
            }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            if(holder is GroupMemberCellViewHolder) {
                // データあり
                onBindViewHolder(holder, position)
            } else if(holder is InviteUserCellViewHolder) {
                // データあり
                onBindViewHolder(holder, position)
            } else if(holder is EmptyCellViewHolder) {
                // 0件
                onBindViewHolder(holder, position)
            } else if(holder is SectionCellViewHolder) {
                onBindViewHolder(holder, position)
            }
        }

        private fun onBindViewHolder(holder: GroupMemberCellViewHolder, position: Int) {
            val data = groupUsers[position - getGroupUsersPositionOffset()]
            holder.binding.user = data
        }

        private fun onBindViewHolder(holder: InviteUserCellViewHolder, position: Int) {
            val data = inviteUsers[position - getInviteRequestsOffset()]
            holder.binding.user = data
        }



        private fun onBindViewHolder(holder: SectionCellViewHolder, position: Int) {
            if(position == 0)
                holder.binding.title = context.getString(R.string.group_member_label)
            else
                holder.binding.title = context.getString(R.string.invite_user_label)

            holder.binding.titleTextView.textSize = 20.0f
        }

        private fun onBindViewHolder(holder: EmptyCellViewHolder, position: Int) {
            holder.binding.title = context.getString(R.string.none)
        }




        enum class ViewType(val code: Int) {
            MEMBER_SECTION(0),
            MEMBER_VIEW(1),
            MEMBER_EMPTY(2),
            INVITE_SCETION(3),
            INVITE_VIEW(4),
            INVITE_EMPTY(5)
        }

    }

    class SectionCellViewHolder(val binding: TitleCellBinding) : RecyclerView.ViewHolder(binding.root)
    class GroupMemberCellViewHolder(val binding: GroupMemberCellBinding) : RecyclerView.ViewHolder(binding.root)
    class InviteUserCellViewHolder(val binding: GroupMemberCellBinding) : RecyclerView.ViewHolder(binding.root)
    class EmptyCellViewHolder(val binding: TitleCellBinding) : RecyclerView.ViewHolder(binding.root)

}