package com.komugirice.icchat.firestore.manager

import com.komugirice.icchat.firestore.model.Request
import com.komugirice.icchat.firestore.store.RequestStore

object RequestManager {
    var myUserRequest = Request()
        set(value) {
            field = value
        }

    var myUserGroupsRequest = listOf<Request>()

    fun initRequestManager(onSuccess: () -> Unit) {
        initMyUserRequest(){
            initMyUserGroupRequest(){
                onSuccess()
            }
        }
    }

    fun initMyUserRequest(onSuccess: () -> Unit) {
        RequestStore.getLoginUserRequest({
            myUserRequest = Request().apply {
                isGroup = false
                documentId = UserManager.myUserId
            }
            onSuccess.invoke()
        }){
            myUserRequest = it
            onSuccess.invoke()
        }
    }
    fun initMyUserGroupRequest(onSuccess: () -> Unit) {
        RequestStore.getLoginUserGroupsRequest {
            myUserGroupsRequest = it
            onSuccess.invoke()
        }
    }
}