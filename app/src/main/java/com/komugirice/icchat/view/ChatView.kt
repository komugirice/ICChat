package com.komugirice.icchat.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komugirice.icchat.R
import com.komugirice.icchat.databinding.ChatMessageLeftCellBinding
import com.komugirice.icchat.databinding.ChatMessageRightCellBinding
import com.komugirice.icchat.databinding.ChatMessageSystemCellBinding
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.FirebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.FileInfo
import com.komugirice.icchat.firebase.firestore.model.Message


class ChatView : RecyclerView {

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
        lateinit var onClickRefreshCallBack: () -> Unit
        lateinit var onClickDownloadCallBack: (Pair<Message, FileInfo?>) -> Unit
        private val items = mutableListOf<Pair<Message, FileInfo?>>()
        // private val usersMap = mutableMapOf<String, User>()

        fun refresh(list: List<Pair<Message, FileInfo?>>) {
            items.apply {
                clear()
                addAll(list)
            }
            notifyDataSetChanged()
        }

//        fun setUsers(list: List<User>) {
//            val map = list.map { it.userId to it }.toMap()
//            usersMap.apply {
//                clear()
//                putAll(map)
//            }
//            notifyDataSetChanged()
//        }

        override fun getItemCount(): Int = items.size

        /**
         * itemsの数によってVIEW_TYPEを振り分け
         *
         * @param position
         * @return VIEW_TYPE: Int
         */

        override fun getItemViewType(position: Int): Int {
            val message = items[position].first
            val file = items[position].second
            return if (message.type == MessageType.SYSTEM.id) {
                VIEW_TYPE_SYSTEM
            } else if (message.userId.equals(UserManager.myUserId)) {
                VIEW_TYPE_LOGIN_USER
            } else
                VIEW_TYPE_OTHER_USER
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder: RecyclerView.ViewHolder?
            when (viewType) {
                VIEW_TYPE_LOGIN_USER -> {
                    holder = ChatMessageCellViewHolder(
                        ChatMessageRightCellBinding.inflate(
                            LayoutInflater.from(
                                context
                            ), parent, false
                        )
                    )
                    holder.binding.root.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            hideKeyboard(v)
                        }
                    })
                }
                VIEW_TYPE_OTHER_USER -> {
                    holder = ChatMessageOtheruserCellViewHolder(
                        ChatMessageLeftCellBinding.inflate(
                            LayoutInflater.from(context),
                            parent,
                            false
                        )
                    )
                    holder.binding.root.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            hideKeyboard(v)
                        }
                    })
                }
                // VIEW_TYPE_SYSTEM
                else -> {
                    holder = ChatMessageSystemCellViewHolder(
                        ChatMessageSystemCellBinding.inflate(
                            LayoutInflater.from(context),
                            parent,
                            false
                        )
                    )
                    holder.binding.root.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            hideKeyboard(v)
                        }
                    })
                }
            }
            return holder
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ChatMessageCellViewHolder)
                onBindLoginUserViewHolder(holder, position)
            else if (holder is ChatMessageOtheruserCellViewHolder)
                onBindOtherUserViewHolder(holder, position)
            else if (holder is ChatMessageSystemCellViewHolder)
                onBindSystemViewHolder(holder, position)
        }

        /**
         * ログインユーザ用
         *
         * @param holder
         * @param position
         */
        private fun onBindLoginUserViewHolder(holder: ChatMessageCellViewHolder, position: Int) {
            val message = items[position].first
            val file = items[position].second
            holder.binding.message = message
            holder.binding.file = file
            holder.binding.type = MessageType.getValue(message.type)

            // 長押し
            holder.binding.root.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    val menuList = listOf(
                        //Pair(0, R.string.quote_message),
                        Pair(0, R.string.delete_message)
                    )
                    MaterialDialog(context).apply {
                        listItems(
                            items = listOf(
                                //context.getString(menuList.get(0).second),
                                context.getString(menuList.get(0).second)
                            ),
                            selection = { dialog, index, text ->
                                when (index) {
//                                    menuList.get(0).first -> {
//                                        // メッセージ引用
//                                    }
                                    menuList.get(0).first -> {
                                        // メッセージ削除
                                        FirebaseFacade.deleteMessage(message, file){
                                            onClickRefreshCallBack.invoke()
                                        }
                                    }
                                    else -> return@listItems
                                }
                            }
                        )
                    }.show()
                    return true
                }
            })
            // 画像タイプ ダウンロードクリック
            holder.binding.imageCell.downloadTextView.setOnClickListener {
                onClickDownloadCallBack.invoke(Pair(message, file))
            }
            // ファイルタイプ ダウンロードクリック
            holder.binding.fileCell.downloadTextView.setOnClickListener {
                onClickDownloadCallBack.invoke(Pair(message, file))
            }

        }

        /**
         * ログインユーザ以外用
         *
         * @param holder
         * @param position
         */
        private fun onBindOtherUserViewHolder(
            holder: ChatMessageOtheruserCellViewHolder,
            position: Int
        ) {
            val message = items[position].first
            val file = items[position].second
            holder.binding.message = message
            holder.binding.file = file
            holder.binding.type = MessageType.getValue(message.type)

            // UserManagerのfriends以外から取得する可能性がある
            // 退会したらgroup.userListから消えるのでusresMapが使えないバグの対応
            // holder.binding.user = usersMap[data.userId]
            holder.binding.user = UserManager.getTargetUser(message.userId)

            // 画像タイプ ダウンロードクリック
            holder.binding.imageCell.downloadTextViewOther.setOnClickListener {
                onClickDownloadCallBack.invoke(Pair(message, file))
            }
            holder.binding.fileCell.downloadTextView.setOnClickListener {
                onClickDownloadCallBack.invoke(Pair(message, file))
            }
        }

        /**
         * システム用
         *
         * @param holder
         * @param position
         */
        private fun onBindSystemViewHolder(holder: ChatMessageSystemCellViewHolder, position: Int) {
            val message = items[position].first
            holder.binding.message = message
        }

        /**
         * hideKeyboard
         *
         * @param v: View?
         */
        private fun hideKeyboard(v: View?) {
            (v?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                v.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

    }

    class ChatMessageCellViewHolder(val binding: ChatMessageRightCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ChatMessageOtheruserCellViewHolder(val binding: ChatMessageLeftCellBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ChatMessageSystemCellViewHolder(val binding: ChatMessageSystemCellBinding) :
        RecyclerView.ViewHolder(binding.root)


    companion object {
        private const val VIEW_TYPE_LOGIN_USER = 0
        private const val VIEW_TYPE_OTHER_USER = 1
        private const val VIEW_TYPE_SYSTEM = 2
    }
}