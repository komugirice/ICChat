package com.komugirice.icchat.firestore.manager

import com.komugirice.icchat.firestore.model.GroupRequests
import com.komugirice.icchat.firestore.model.Request
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.store.RequestStore

object RequestManager {
    var myUserRequests = listOf<Request>()

    var myGroupsRequests = listOf<GroupRequests>()

    var usersRequestToMe = listOf<Request>()


    var groupsRequestToMe = listOf<GroupRequests>()    // 中のrequestは必ず1件

    fun initRequestManager(onSuccess: () -> Unit) {
        initMyUserRequests(){
            initMyGroupsRequests(){
                initUsersRequestToMe(){
                    initGroupsRequestToMe(){
                        onSuccess()
                    }
                }
            }
        }
    }



    fun initMyUserRequests(onSuccess: () -> Unit) {
        RequestStore.getLoginUserRequests({
            onSuccess.invoke()
        }){
            myUserRequests = it
            onSuccess.invoke()
        }
    }
    fun initMyGroupsRequests(onSuccess: () -> Unit) {
        RequestStore.getLoginUserGroupsRequests {
            myGroupsRequests = it
            onSuccess.invoke()
        }
    }

    fun initUsersRequestToMe(onSuccess: () -> Unit) {
        RequestStore.getUsersRequestToMe {
            usersRequestToMe = it
            onSuccess.invoke()
        }
    }

    fun initGroupsRequestToMe(onSuccess: () -> Unit) {
        RequestStore.getGroupsRequestToMe {
            groupsRequestToMe = it
            onSuccess.invoke()
        }
    }

    fun clear() {
        myUserRequests = listOf<Request>()
        myGroupsRequests = listOf<GroupRequests>()
        usersRequestToMe = listOf<Request>()
        groupsRequestToMe = listOf<GroupRequests>()
    }

}