package com.komugirice.icchat.viewModel

import android.content.Intent
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.extension.getDateToString
import com.komugirice.icchat.extension.toDate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.komugirice.icchat.InterestFragment
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.store.InterestStore
import com.komugirice.icchat.view.InterestView
import kotlinx.serialization.PrimitiveKind
import timber.log.Timber
import java.util.*

class InterestViewModel: ViewModel() {
    val items = MutableLiveData<List<InterestView.InterestViewData>>()
    val isException = MutableLiveData<Throwable>()

    var userId: String = ""
    var updateMode = false
    private var interestListener: ListenerRegistration? = null

    fun initData() {

        // interest情報 昇順ソート済
        InterestStore.getInterests(userId) { interests ->

            // InterestViewData作成
            val list = createInterestViewData(interests)
            items.postValue(list)

            // 監視
            val lastCreatedAt = interests.map { it.createdAt }.max() ?: Date()
            initSubscribe(lastCreatedAt)

            // interest0件の対応
            if (interests.isEmpty()) {
                // 監視
                val lastCreatedAt = Date()
                initSubscribe(lastCreatedAt)
            }
        }

    }

    fun updateUserId(newUserId: String) {
        if (userId == newUserId)
            return
        userId = newUserId
        // 更新モード
        updateMode = UserManager.myUserId == userId

        initData()
    }

    /**
     * 監視メソッド
     *
     */
    private fun initSubscribe(lastCreatedAt: Date) {
        interestListener = FirebaseFirestore
            .getInstance()
            .collection("users/$userId/interests")
            .orderBy(Interest::createdAt.name, Query.Direction.DESCENDING)
            .whereGreaterThan(Interest::createdAt.name, lastCreatedAt)
            .limit(1L)
            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                Timber.d("initSubscribe snapshot:$snapshot firebaseFirestoreException:$firebaseFirestoreException")
                if (firebaseFirestoreException != null) {
                    firebaseFirestoreException.printStackTrace()
                    isException.postValue(firebaseFirestoreException)
                    return@addSnapshotListener
                }
                snapshot?.toObjects(Interest::class.java)?.firstOrNull()?.also {
                    val tmp: MutableList<InterestView.InterestViewData>? = items.value?.toMutableList()
                    tmp?.add(
                        InterestView.InterestViewData(
                        it,
                        InterestView.VIEW_TYPE_INTEREST
                    ))
                    // TODO 本日日付の追加
                    items.postValue(tmp)
                }
            }
    }

    private fun createInterestViewData(interests: List<Interest>): List<InterestView.InterestViewData>{
        val interestViewData = mutableListOf<InterestView.InterestViewData>()

        // interestsを詰め込む
        val onlyInterestList = mutableListOf<InterestView.InterestViewData>()
        interests.forEach {
            onlyInterestList.add(
                InterestView.InterestViewData(
                    it,
                    InterestView.VIEW_TYPE_INTEREST
                )
            )
        }

        var targetDate: Date? = "9999/12/31".toDate()    // ダミー値設定

        // interestViewDataにinterestsを格納していく
        // その間に日付をチェックしながら、違いがあった場合、日付データを追加する。
        onlyInterestList.forEach {

            // 先に日付データを格納
            val thisDate = it.interest?.createdAt
            // compareTo : (thisTime<anotherTime ? -1 : (thisTime==anotherTime ? 0 : 1));
            if(thisDate != null && thisDate.getDateToString() !=  targetDate?.getDateToString()) {
                targetDate = thisDate // targetDate更新
                // 違いがあった場合、日付データを追加
                interestViewData.add(
                    InterestView.InterestViewData(
                        thisDate,
                        InterestView.VIEW_TYPE_DATE
                    )
                )
            }
            // 次にinterestsを格納
            interestViewData.add(it)
        }

        return interestViewData
    }
}