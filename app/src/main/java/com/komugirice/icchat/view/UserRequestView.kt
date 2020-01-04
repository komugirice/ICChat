package com.komugirice.icchat.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.komugirice.icchat.R
import com.komugirice.icchat.databinding.FriendRequestedCellBinding
import com.komugirice.icchat.firestore.model.Request
import com.thoughtbot.expandablerecyclerview.ExpandCollapseController
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.models.ExpandableList
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.list_user_request.view.*
import timber.log.Timber
import java.util.*


class UserRequestView  : RecyclerView {

    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    val customAdapter by lazy {
        Adapter(
            context,
            listOf(
                ExpandableRequest(context.getString(R.string.friend_request_label)
                , mutableListOf())
            )
        )
    }

    init {
        customAdapter.setOnGroupExpandCollapseListener(object: GroupExpandCollapseListener{

            override fun onGroupExpanded(group: ExpandableGroup<*>?) {

            }
            override fun onGroupCollapsed(group: ExpandableGroup<*>?) {

            }
        })

        adapter = customAdapter
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)

    }


    class Adapter(val context: Context, groups: List<out ExpandableGroup<*>>)
    : ExpandableRecyclerViewAdapter<RequestListViewHolder, RequestCellViewHolder>(groups) {

        override fun onCreateGroupViewHolder(
            parent: ViewGroup?,
            viewType: Int
        ): RequestListViewHolder {
            return RequestListViewHolder(
                LayoutInflater.from(parent?.getContext()).inflate(
                    R.layout.list_user_request,
                    parent,
                    false
                )
            )
        }

        override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): RequestCellViewHolder {
            return RequestCellViewHolder(
                FriendRequestedCellBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
        }

        override fun onBindGroupViewHolder(
            holder: RequestListViewHolder?,
            flatPosition: Int,
            group: ExpandableGroup<*>?
        ) {
            holder?.setListTitle(group)
            onGroupClick(flatPosition)
        }

        override fun onBindChildViewHolder(
            holder: RequestCellViewHolder?,
            flatPosition: Int,
            group: ExpandableGroup<*>?,
            childIndex: Int
        ) {
            val request = (group as ExpandableRequest).items.get(childIndex)
            holder?.binding?.request = request.getRequest()
            // Timber.d(Gson().toJson(request.getRequest()))
        }

        fun refresh_request(list: List<com.komugirice.icchat.firestore.model.Request>) {
            var rList = mutableListOf<PRequest>()
            list.forEach {

                val r = PRequest(
                    documentId = it.documentId,
                    createdAt = it.createdAt,
                    status = it.status,
                    beRequestedId = it.beRequestedId,
                    requestId = it.requestId
                )
                rList.add(r)
            }
            groups.get(GENRE_REQUEST).items.apply {
                clear()
                addAll(rList)
            }
        }

        companion object {
            const val GENRE_REQUEST = 0
        }
    }

    class RequestListViewHolder(v: View) : GroupViewHolder(v) {

        val arrow = v.listItemArrow
        val name = v.listItemName

        override fun onClick(v: View?) {
            super.onClick(v)

        }

        fun setListTitle(request: ExpandableGroup<*>?) {
            if(request is ExpandableRequest){
                name.text = request.title
            }
        }

        override fun collapse() {
            animateCollapse();
        }

        override fun expand() {
            animateExpand();
        }

        private fun animateExpand() {
            val rotate = RotateAnimation(
                360f,
                180f,
                RELATIVE_TO_SELF,
                0.5f,
                RELATIVE_TO_SELF,
                0.5f
            )
            rotate.duration = 300
            rotate.fillAfter = true
            arrow.setAnimation(rotate)
        }

        private fun animateCollapse() {
            val rotate = RotateAnimation(
                180f,
                360f,
                RELATIVE_TO_SELF,
                0.5f,
                RELATIVE_TO_SELF,
                0.5f
            )
            rotate.duration = 300
            rotate.fillAfter = true
            arrow.setAnimation(rotate)
        }
    }

    class RequestCellViewHolder(val binding: FriendRequestedCellBinding) : com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder(binding.root)

    class ExpandableRequest(title: String, items: MutableList<PRequest>): ExpandableGroup<PRequest>(title, items)

    @Parcelize
    data class PRequest(var documentId: String, var requestId:String, var beRequestedId: String
        ,var status: Int, var createdAt: Date): Parcelable {

        fun getRequest(): com.komugirice.icchat.firestore.model.Request {
            return Request().apply{
                this.documentId = this@PRequest.documentId
                this.requestId = this@PRequest.requestId
                this.beRequestedId = this@PRequest.beRequestedId
                this.status = this@PRequest.status
                this.createdAt = this@PRequest.createdAt
            }
        }
    }
}