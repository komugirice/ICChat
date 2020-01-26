package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komugirice.icchat.ChatActivity
import com.komugirice.icchat.databinding.DateBorderCellBinding
import com.komugirice.icchat.databinding.InterestCellBinding
import com.komugirice.icchat.databinding.RoomCellBinding
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room
import java.util.*

class InterestView : RecyclerView {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        ctx,
        attrs,
        defStyleAttr
    )

    val customAdapter by lazy {
        InterestView.Adapter(
            context
        )
    }

    init {
        adapter = customAdapter
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
    }
    class Adapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val items = mutableListOf<InterestViewData>()

        fun refresh(list: List<InterestViewData>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

        fun clear() {
            items.clear()
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        /**
         * itemsの数によってVIEW_TYPEを振り分け
         *
         * @param position
         * @return VIEW_TYPE: Int
         */

        override fun getItemViewType(position: Int): Int {
            return items[position].viewType
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            InterestCellViewHolder(
                InterestCellBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is InterestCellViewHolder)
                onBindViewHolder(holder, position)
            else if (holder is DateBorderCellViewHolder)
                onBindViewHolder(holder, position)
        }

        /**
         * InterestCellViewHolderのonBindViewHolder
         *
         * @param holder
         * @param position
         */
        private fun onBindViewHolder(holder: InterestCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.interest = data.interest
            holder.binding.isLeft = position % 2 == 0

            holder.binding.root.setOnClickListener {

            }
        }

        /**
         * DateBorderCellViewHolderのonBindViewHolder
         *
         * @param holder
         * @param position
         */
        private fun onBindViewHolder(holder: DateBorderCellViewHolder, position: Int) {
            val data = items[position]
            holder.binding.date = data.date


        }
    }

    class InterestCellViewHolder(val binding: InterestCellBinding) : RecyclerView.ViewHolder(binding.root)
    class DateBorderCellViewHolder(val binding: DateBorderCellBinding) : RecyclerView.ViewHolder(binding.root)

    class InterestViewData {
        var interest: Interest? = null
        var date: Date? = null
        var viewType: Int

        constructor(interest: Interest, viewType: Int) {
            this.interest = interest
            this.viewType = viewType
        }
        constructor(date: Date, viewType: Int) {
            this.date = date
            this.viewType = viewType
        }
        constructor(viewType: Int) {
            this.viewType = viewType
        }
    }
    companion object {
        const val VIEW_TYPE_INTEREST = 0
        const val VIEW_TYPE_DATE = 1
    }
}