package com.komugirice.icchat.firebase

import android.net.Uri
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import com.komugirice.icchat.R
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.fcm.FcmStore
import com.komugirice.icchat.firebase.firestore.manager.RequestManager
import com.komugirice.icchat.firebase.firestore.manager.RoomManager
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.GroupRequests
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.firebase.firestore.store.MessageStore
import com.komugirice.icchat.firebase.firestore.store.RequestStore
import com.komugirice.icchat.firebase.firestore.store.RoomStore
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.util.FireStorageUtil

/**
 * FireStoreのCRUDとManagerの更新をまとめたFunctionを提供する
 *
 */
object FirebaseFacade {

    /**
     * 全Managerの初期化
     *
     * @param onSuccess
     *
     */
    fun initManager(onSuccess: () -> Unit) {

        UserManager.initUserManager() {

            RoomManager.initRoomManager() {

                RequestManager.initRequestManager() {

                    // fcmトークン更新処理
                    if (UserManager.myUser.fcmToken == null) {
                        FcmStore.getLoginUserToken {
                            UserStore.updateFcmToken(it) {
                                UserManager.myUser.fcmToken = it
                                onSuccess.invoke()
                            }
                        }
                    } else {
                        onSuccess.invoke()
                    }

                }
            }
        }
    }

    fun clearManager() {
        UserManager.clear()
        RoomManager.clear()
        RequestManager.clear()
    }

    /**
     * 友だち追加　Users.friendListを更新、Requestの友だち申請を削除
     *
     * @param targetUserId
     * @param onFailed
     * @param onSuccess
     *
     */
    fun addFriend(targetUserId: String, onFailed: () -> Unit, onSuccess: () -> Unit) {
        // Users更新
        UserStore.addFriend(targetUserId, onFailed) {
            // Room登録
            RoomStore.registerSingleRoom(targetUserId, onFailed) {
                // Request 自分→target 削除
                RequestStore.deleteUsersRequest(UserManager.myUserId, targetUserId)
                // Request target→自分 削除
                RequestStore.deleteUsersRequest(targetUserId, UserManager.myUserId)

                initManager {

                    onSuccess.invoke()

                }

            }
        }
    }

    /**
     * Requestに友だち申請を追加
     *
     * @param list
     * @param onSuccess
     *
     */
    fun requestFriend(list: List<User>, onSuccess: () -> Unit) {
        var index = 0

        list.forEach {
            index++
            RequestStore.requestFriend(it.userId) {
                if (list.size == index) {
                    // 再設定
                    RequestManager.initMyUserRequests {
                        onSuccess.invoke()
                    }
                }
            }
        }
    }

    /**
     * GroupSettingActivity　グループ登録
     *
     * @param room
     * @param groupRequest
     * @param delRequests
     * @param onFailed
     * @param onSuccess
     *
     */
    fun registerGroupRoom(
        room: Room
        , groupRequest: GroupRequests? // チェックボックスにチェック有り
        , delRequests: List<String>   // チェックあり→チェックなし
        , onFailed: () -> Unit
        , onSuccess: () -> Unit
    ) {

        // Room登録
        RoomStore.registerGroupRoom(room) {
            if (it.isSuccessful) {
                //チェックありのRequest登録
                RequestStore.registerGroupRequest(groupRequest) {
                    // チェックを外したRequest削除
                    RequestStore.deleteGroupRequest(room.documentId, delRequests) {
                        // RoomManager更新
                        RoomManager.initRoomManager {
                            // RequestManager更新
                            RequestManager.initMyGroupsRequests {
                                onSuccess.invoke()
                            }
                        }
                    }
                }
            } else {
                onFailed.invoke()
            }
        }
    }

    /**
     * 友だち申請を拒否する
     *
     * @param requesterId
     * @param onSuccess
     *
     */
    fun denyUserRequest(requesterId: String, onSuccess: () -> Unit) {
        // Request更新
        RequestStore.denyUserRequest(requesterId) {
            RequestManager.initUsersRequestToMe {
                onSuccess.invoke()
            }
        }
    }

    /**
     * 友だち申請の拒否をキャンセルする
     *
     * @param requesterId
     * @param onSuccess
     *
     */
    fun cancelDenyUserRequest(requesterId: String, onSuccess: () -> Unit) {
        // Request更新
        RequestStore.cancelDenyUserRequest(requesterId) {
            RequestManager.initUsersRequestToMe {
                onSuccess.invoke()
            }
        }
    }

    /**
     * 友だちを解除
     *
     * @param friendId
     * @param roomId
     * @param onSuccess
     *
     */
    fun deleteFriend(friendId: String, roomId: String, onSuccess: () -> Unit) {
        // User削除(中でUserManagerも更新している)
        UserStore.delFriend(friendId) {
            // Room削除
            RoomStore.deleteRoom(roomId) {
                RoomManager.initRoomManager {
                    onSuccess.invoke()
                }
            }
        }
    }

    /**
     * グループ招待を拒否
     *
     * @param roomId
     * @param userId
     * @param onSuccess
     *
     */
    fun denyGroupRequest(roomId: String, userId: String, onSuccess: () -> Unit) {
        // Request更新
        RequestStore.denyGroupRequest(roomId, userId) {
            RequestManager.initGroupsRequestToMe {
                onSuccess.invoke()
            }
        }
    }

    /**
     * グループ招待の拒否を解除
     *
     * @param roomId
     * @param userId
     * @param onSuccess
     *
     */
    fun cancelDenyGroupRequest(roomId: String, userId: String, onSuccess: () -> Unit) {
        // Request更新
        RequestStore.cancelDenyGroupRequest(roomId, userId) {
            RequestManager.initGroupsRequestToMe {
                onSuccess.invoke()
            }
        }
    }

    /**
     * グループを削除
     *
     * @param roomId
     * @param onSuccess
     *
     */
    fun deleteRoom(roomId: String, onSuccess: () -> Unit) {
        // Room削除
        RoomStore.deleteRoom(roomId) {
            // Roomアイコン削除
            FireStorageUtil.deleteGroupIconImage(roomId) {
                // Room内Request削除
                val list = RequestManager.myGroupsRequests.filter { it.room.documentId == roomId }
                    .map { it.requests }
                    .firstOrNull()?.map { it.documentId }?.toList()
                RequestStore.deleteGroupRequest(roomId, list ?: listOf()) {
                    RoomManager.initRoomManager {
                        RequestManager.initMyGroupsRequests() {
                            onSuccess.invoke()
                        }

                    }
                }

            }
        }
    }

    /**
     * グループメンバーから退会する
     *
     * @param room
     * @parak userId
     * @param onSuccess
     *
     */
    fun withdrawGroupMember(room: Room, userId: String, onSuccess: () -> Unit) {
        RoomStore.removeGroupMember(room, userId) {
            RequestStore.deleteGroupRequest(room.documentId, listOf(userId)) {
                // Messageに「〜さんが退会しました。」を登録
                MessageStore.registerMessage(
                    room.documentId, userId,
                    applicationContext.getString(
                        R.string.message_group_withdraw,
                        UserManager.myUser.name
                    ),
                    MessageType.SYSTEM.id
                ) {

                    RoomManager.initRoomManager {
                        onSuccess.invoke()

                    }
                }
            }
        }
    }

    /**
     * FCMトークンの更新
     *
     * @param token
     *
     */
    fun updateFcmToken(token: String) {
        UserStore.updateFcmToken(token) {
            UserManager.myUser.fcmToken = token
        }
    }

    /**
     * 招待されているグループを承認する
     *
     *
     */
    fun acceptGroup(room: Room, onSuccess: () -> Unit) {
        // Room更新
        RoomStore.acceptGroupMember(room, UserManager.myUserId) {
            // Request更新
            RequestStore.acceptGroupRequest(
                room.documentId,
                UserManager.myUserId
            ) {
                // Messageに「〜さんが参加しました。」を登録
                MessageStore.registerMessage(
                    room.documentId, UserManager.myUserId,
                    applicationContext.getString(
                        R.string.message_group_accept,
                        UserManager.myUser.name
                    ),
                    MessageType.SYSTEM.id
                ) {

                    RoomManager.initRoomManager {
                        RequestManager.initGroupsRequestToMe {
                            onSuccess.invoke()
                        }
                    }
                }

            }

        }
    }

    fun registChatMessageImage(roomId: String, uri: Uri, onSuccess: () -> Unit){

        FireStorageUtil.registRoomMessageImage(roomId, uri){
            MessageStore.registerMessage(roomId, UserManager.myUserId, it, MessageType.IMAGE.id){
                onSuccess.invoke()
            }

        }
    }

    fun deleteMessage(message: Message, onSuccess: () -> Unit) {
        MessageStore.deleteMessage(message){
            // storageの保存ファイル削除
            FireStorageUtil.deleteRoomMessageFile(message) {
                onSuccess.invoke()
            }
        }
    }

}