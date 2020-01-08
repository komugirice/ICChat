package com.komugirice.icchat.firebase

import com.komugirice.icchat.firebase.fcm.FcmStore
import com.komugirice.icchat.firebase.firestore.manager.RequestManager
import com.komugirice.icchat.firebase.firestore.manager.RoomManager
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.GroupRequests
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.firebase.firestore.store.RequestStore
import com.komugirice.icchat.firebase.firestore.store.RoomStore
import com.komugirice.icchat.firebase.firestore.store.UserStore
import com.komugirice.icchat.util.FireStorageUtil

/**
 * FireStoreのCRUDとManagerの更新をまとめたFunctionを提供する
 *
 */
object firebaseFacade {

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
                    if(UserManager.myUser.fcmToken == null) {
                       FcmStore.getLoginUserToken {
                           UserStore.updateFcmToken(it){
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
     * Users.friendListを更新、Requestの友だち申請を削除
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
    fun registerGroupRoom(room: Room
                          , groupRequest: GroupRequests? // チェックボックスにチェック有り
                          , delRequests: List<String>   // チェックあり→チェックなし
                          , onFailed: () -> Unit
                          , onSuccess: () -> Unit) {

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
    fun deleteFriend(friendId: String, roomId: String, onSuccess: () -> Unit){
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
     * 友だちを解除
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
        RoomStore.deleteRoom(roomId) {
            FireStorageUtil.deleteGroupIconImage(roomId) {
                RoomManager.initRoomManager {
                    onSuccess.invoke()
                }
            }
        }
    }

    /**
     * グループメンバーから除外する
     *
     * @param room
     * @parak userId
     * @param onSuccess
     *
     */
    fun removeGroupMember(room: Room, userId: String, onSuccess: () -> Unit) {
        RoomStore.removeGroupMember(room, userId) {
            RoomManager.initRoomManager {
                onSuccess.invoke()
            }
        }
    }

    fun updateFcmToken(token: String){
        UserStore.updateFcmToken(token){
            UserManager.myUser.fcmToken = token
        }
    }

}