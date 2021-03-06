package com.komugirice.icchat.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komugirice.icchat.InputInterestActivity
import com.komugirice.icchat.R
import com.komugirice.icchat.databinding.DateBorderCellBinding
import com.komugirice.icchat.databinding.DeleteInterestCellBinding
import com.komugirice.icchat.databinding.ImageViewDialogBinding
import com.komugirice.icchat.databinding.InterestCellBinding
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.util.DialogUtil
import java.util.*

class DeleteInterestView : RecyclerView {
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
    class Adapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        lateinit var onClickDeleteCallBack: () -> Unit
        lateinit var onClickUrlCallBack: (uri: Uri) -> Unit
        private val items = mutableListOf<DeleteInterestViewData>()

        // スワイプ更新中に「検索結果が0件です」を出さない為の対応
        private var hasCompletedFirstRefresh = false

        fun getViewTypeDateOffset(position: Int): Int {
            var result = 0
            for (i in 0 until position ) {
                if(items[i].viewType == VIEW_TYPE_DATE)
                    result++
            }
            return result
        }

        fun refresh(list: List<DeleteInterestViewData>) {
            // リフレッシュ実行フラグON
            hasCompletedFirstRefresh = true
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

        override fun getItemCount(): Int {
            // リストデータ中の件数をリターン。
            return if (items.isEmpty()) {
                if (hasCompletedFirstRefresh)
                    1
                else
                    0
            } else items.size
        }

        /**
         * itemsの数によってVIEW_TYPEを振り分け
         *
         * @param position
         * @return VIEW_TYPE: Int
         */

        override fun getItemViewType(position: Int): Int {
            // 0件対策
            return if(items.isEmpty()) VIEW_TYPE_EMPTY else items[position].viewType
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            // 興味セル
            if(viewType == VIEW_TYPE_INTEREST) {
                return InterestCellViewHolder(
                    DeleteInterestCellBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            } else if(viewType == VIEW_TYPE_DATE) {
                // 日付セル
                return DateBorderCellViewHolder(
                    DateBorderCellBinding.inflate(
                        LayoutInflater.from(context),
                        parent,
                        false
                    )
                )
            } else {
                // Emptyセル
                return EmptyViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.empty_cell, parent, false))
            }
        }

        /**
         * onBindViewHolder
         *
         * @param holder
         * @param position
         */
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is InterestCellViewHolder)
                onBindViewHolder(holder, position)
            else if (holder is DateBorderCellViewHolder)
                onBindViewHolder(holder, position)
            else if (holder is EmptyViewHolder)
                onBindEmptyViewHolder(holder, position)
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
            holder.binding.userId = UserManager.myUserId
            holder.binding.isLeft = (position - getViewTypeDateOffset(position)) % 2 == 0

            // チェックボックス復元
            holder.binding.isChecked = data.isChecked

            // チェックボックスの切り替え
            holder.binding.checkbox.setOnCheckedChangeListener { v, isChecked ->
                data.isChecked = isChecked
            }

            // 長押しのClickListener
            val onLongClickListener = object: View.OnLongClickListener {

                override fun onLongClick(v: View?): Boolean {

                    val menuList = listOf(
                        Pair(0, R.string.restore),
                        Pair(1, R.string.delete_message)
                    )


                    MaterialDialog(context).apply {
                        listItems(items = listOf(
                            context.getString(menuList.get(0).second),
                            context.getString(menuList.get(1).second)
                        ),
                            selection = { dialog, index, text ->
                                when (index) {
                                    menuList.get(0).first -> {
                                        // 復元
                                        data.interest?.apply{
                                            DialogUtil.confirmRestoreInterestDialog(context, this) {
                                                onClickDeleteCallBack.invoke()
                                            }
                                        }
                                    }
                                    menuList.get(1).first -> {
                                        // 削除
                                        data.interest?.apply{
                                            DialogUtil.confirmDeleteCompleteInterestDialog(context, this) {
                                                onClickDeleteCallBack.invoke()
                                            }
                                        }
                                    }
                                    else -> return@listItems
                                }
                            })
                    }.show()
                    return true
                }
            }

            // 長押し
            holder.binding.longClickView.setOnLongClickListener(onLongClickListener)
            holder.binding.ogpWrapLayout.setOnLongClickListener(onLongClickListener)
            holder.binding.imageView.setOnLongClickListener(onLongClickListener)
            // URL記事クリック
            holder.binding.ogpWrapLayout.setOnClickListener {
                data.interest?.apply{
                    onClickUrlCallBack.invoke(Uri.parse(this.ogpUrl))
                }
            }

            // 画像クリック
            holder.binding.imageView.setOnClickListener {
                // 画像プレビュー表示
                MaterialDialog(context).apply {
                    cancelable(true)
                    val dialogBinding = ImageViewDialogBinding.inflate(
                        LayoutInflater.from(context),
                        null,
                        false
                    )
                    dialogBinding.imageView.setImageDrawable(holder.binding.imageView.drawable)
                    setContentView(dialogBinding.root)
                }.show()
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

        /**
         * itemsが0件のViewHolder
         *
         * @param holder
         * @param position
         */
        private fun onBindEmptyViewHolder(holder: EmptyViewHolder, position: Int) {
        }
    }

    // 興味セル
    class InterestCellViewHolder(val binding: DeleteInterestCellBinding) : RecyclerView.ViewHolder(binding.root)
    // 日付セル
    class DateBorderCellViewHolder(val binding: DateBorderCellBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * EmptyViewHolderクラス
     * 検索結果が0件の場合のViewHolder
     *
     * @param itemView
     */
    class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var searchZeroText = itemView.findViewById(R.id.registZeroText) as TextView
    }

    class DeleteInterestViewData: InterestView.InterestViewData {
        var isChecked = false

        constructor(interest: Interest, viewType: Int): super(interest, viewType)
        constructor(date: Date, viewType: Int): super(date, viewType)
        constructor(viewType: Int): super(viewType)
    }

    companion object {
        const val VIEW_TYPE_INTEREST = 0
        const val VIEW_TYPE_DATE = 1
        const val VIEW_TYPE_EMPTY = -1
    }
}