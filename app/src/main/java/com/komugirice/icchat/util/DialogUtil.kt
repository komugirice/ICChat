package com.komugirice.icchat.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import com.komugirice.icchat.R
import com.komugirice.icchat.firebase.firebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Request
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.store.UserStore

class DialogUtil {
    companion object {

        /**
         * ユーザ申請の返事
         * @param context
         * @param request
         *
         */
        fun confirmUserRequestDialog(context: Context, request: Request, onSuccess: () -> Unit) {
            AlertDialog.Builder(context)
                .setMessage(R.string.confirm_user_request)
                .setPositiveButton(R.string.accept, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        firebaseFacade.addFriend(request.requesterId,
                            { Toast.makeText(context, R.string.alert_already_accept, Toast.LENGTH_LONG).show()}
                        ) {
                            Toast.makeText(
                                context,
                                R.string.alert_accept,
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess.invoke()
                        }

                    }
                })
                .setNegativeButton(R.string.deny, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        firebaseFacade.denyUserRequest(request.requesterId) {
                            Toast.makeText(
                                context,
                                R.string.alert_deny,
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess.invoke()
                        }
                    }
                })
                .setNeutralButton(R.string.cancel, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                    }
                }).show()
        }

        /**
         * 友だち申請の拒否をキャンセルするか
         * @param context
         * @param request
         *
         */
        fun cancelUserDenyDialog(context: Context, request: Request, onSuccess: () -> Unit) {
            AlertDialog.Builder(context)
                .setMessage(R.string.confirm_cancel_user_deny)
                .setPositiveButton(R.string.ok, object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        // 友だち申請の拒否をキャンセル
                        firebaseFacade.cancelDenyUserRequest(request.requesterId){
                            Toast.makeText(
                                context,
                                R.string.alert_cancel_deny,
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess.invoke()
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        /**
         * ユーザ削除確認ダイアログ
         * @param context
         * @param room
         * @param onSuccess
         *
         */
        fun confirmDeleteUserDialog(context: Context, room: Room, onSuccess: () -> Unit) {
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.confirm_user_delete))
                .setPositiveButton("OK", object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        val friendId = room.userIdList.filter{!it.equals(UserManager.myUserId)}.first()
                        // 友だちを解除
                        firebaseFacade.deleteFriend(friendId, room.documentId) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.success_user_delete),
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess.invoke()
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        /**
         * グループ招待の承認ダイアログ
         * @param context
         * @param room
         * @param onSuccess
         *
         */
        fun confirmGroupRequestDialog(context: Context, room: Room, onSuccess: () -> Unit) {
            AlertDialog.Builder(context)
                .setMessage(R.string.confirm_group_request)
                .setPositiveButton(R.string.accept, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        // 招待されているグループを承認する
                        firebaseFacade.acceptGroup(room) {

                             Toast.makeText(
                                 context,
                                 R.string.alert_accept,
                                 Toast.LENGTH_LONG
                             ).show()
                             onSuccess.invoke()

                         }
                    }
                })
                .setNegativeButton(R.string.deny, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        firebaseFacade.denyGroupRequest(room.documentId, UserManager.myUserId) {
                            Toast.makeText(
                                context,
                                R.string.alert_deny,
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess.invoke()
                        }
                    }
                })
                .setNeutralButton(R.string.cancel, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                    }
                }).show()
        }

        /**
         * 拒否したグループ招待の取消確認ダイアログ
         * @param context
         * @param room
         * @param onSuccess
         *
         */
        fun cancelGroupDenyDialog(context: Context, room: Room, onSuccess: () -> Unit) {
            AlertDialog.Builder(context)
                .setMessage(R.string.confirm_cancel_group_deny)
                .setPositiveButton(R.string.ok, object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        firebaseFacade.cancelDenyGroupRequest(room.documentId, UserManager.myUserId){
                            Toast.makeText(
                                context,
                                R.string.alert_cancel_deny,
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess.invoke()
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        /**
         * グループ削除確認ダイアログ
         * @param context
         * @param room
         * @param onSuccess
         *
         */
        fun confirmDeleteGroupDialog(context: Context, room: Room, onSuccess: () -> Unit) {
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.confirm_group_delete))
                .setPositiveButton("OK", object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        firebaseFacade.deleteRoom(room.documentId) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.success_group_delete),
                                Toast.LENGTH_LONG
                            ).show()
                            onSuccess.invoke()
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        /**
         * グループ退会確認ダイアログ
         * @param context
         * @param room
         * @param onSuccess
         *
         */
        fun withdrawGroupDialog(context: Context, room: Room, onSuccess: () ->Unit) {
            AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.confirm_group_withdraw))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, object: DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        firebaseFacade.withdrawGroupMember(room, UserManager.myUserId) {
                            // グループを退会しました
                            AlertDialog.Builder(context)
                                .setMessage(context.getString(R.string.success_group_withdraw))
                                .setPositiveButton(R.string.ok, object: DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        onSuccess.invoke()
                                    }
                                })
                                .setOnDismissListener (object: DialogInterface.OnDismissListener {
                                    override fun onDismiss(dialog: DialogInterface?) {
                                        onSuccess.invoke()
                                    }
                                })
                                .show()
                        }
                    }
                })
                .show()
        }

        /**
         * 友だち追加確認ダイアログ
         * @param context
         * @param room
         * @param onSuccess
         *
         */
        fun addFriendDialog(context: Context, targetUserId: String) {
            UserStore.getTargetUser(targetUserId) {
                AlertDialog.Builder(context)
                    .setTitle("${it.name}")
                    .setMessage(R.string.confirm_add_friend)
                    .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {

                            val onFailed = {
                                Toast.makeText(
                                    context,
                                    R.string.already_exist,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            // Users更新
                            firebaseFacade.addFriend(targetUserId, onFailed) {
                                AlertDialog.Builder(context)
                                    .setMessage(R.string.alert_success_add_friend)
                                    .setPositiveButton(R.string.ok, null)
                                    .show()
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }

    }

}