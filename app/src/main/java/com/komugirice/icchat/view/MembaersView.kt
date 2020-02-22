package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.firebase.firestore.model.Request
import com.komugirice.icchat.firebase.firestore.model.User

class MembaersView : RecyclerView {
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
        private val inviteRequests = mutableListOf<Request>()

        private fun getGroupUsersPositionOffset(): Int = 1

        private fun getInviteRequestsOffset(): Int {
            var result = 2
            result += if (groupUsers.isEmpty()) 1 else groupUsers.size
            return result
        }

        fun refresh(users: List<User>, requests: List<Request>) {
            groupUsers.apply {
                clear()
                addAll(users)
            }
            inviteRequests.apply {
                clear()
                addAll(requests)
            }
            notifyDataSetChanged()
        }

        // groupUsers[position - getGroupUsersPositionOffset()]

        override fun getItemCount(): Int {
            var count = 2 // メンバーセクションと招待中セクション
            count += if (groupUsers.isEmpty()) 1 else groupUsers.size
            count += if (inviteRequests.isEmpty()) 1 else inviteRequests.size
            return count
        }

        override fun getItemViewType(position: Int): Int {
            if (position == 0)
                return ViewType.MEMBER_SECTION.code
            if (position < getInviteRequestsOffset() - 1) {
                return if (groupUsers.isEmpty())
                    ViewType.MEMVER_EMPTY.code
                else
                    ViewType.MEMBER_VIEW.code
            }
            if (position == getInviteRequestsOffset() - 1)
                return ViewType.INVITE_SCETION.code
            if (inviteRequests.isEmpty())
                return ViewType.INVITE_EMPTY.code
            return ViewType.INVITE_VIEW.code
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            when(ViewType.values().first { it.code == viewType }) {
                ViewType.MEMBER_SECTION -> {}
                ViewType.MEMBER_VIEW -> {}
                ViewType.MEMVER_EMPTY -> {}
                ViewType.INVITE_SCETION -> {}
                ViewType.INVITE_VIEW -> {}
                else -> {}
            }


        enum class ViewType(val code: Int) {
            MEMBER_SECTION(0),
            MEMBER_VIEW(1),
            MEMVER_EMPTY(2),
            INVITE_SCETION(3),
            INVITE_VIEW(4),
            INVITE_EMPTY(5)
        }

    }

}