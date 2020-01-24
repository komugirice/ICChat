package com.komugirice.icchat.viewModel

import android.content.Intent
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.komugirice.icchat.InterestFragment
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.store.InterestStore
import timber.log.Timber
import java.util.*

class InterestViewModel: ViewModel() {
    val items = MutableLiveData<List<Interest>>()
    val userId = MutableLiveData<String>()
    val isException = MutableLiveData<Throwable>()
    private var interestListener: ListenerRegistration? = null

    /**
     * intent用（他画面遷移）
     */
    fun initUserId(intent: Intent): Boolean {
        intent.getStringExtra(InterestFragment.KEY_USER_ID).also {
            this.userId.postValue(it)
            return true
        }
        return false

    }

    /**
     * intentを使用しない場合（次画面のリロード）
     */
    fun initUserId(userId: String): Boolean {
        this.userId.postValue(userId)
        return true
    }

    fun initData(@NonNull owner: LifecycleOwner, userId: String) {

        // interest情報
        InterestStore.getInterests(userId) { interests ->

            items.postValue(interests)
            // 監視
            val lastCreatedAt = interests.map { it.createdAt }.max() ?: Date()
            initSubscribe(userId, lastCreatedAt)

            // message0件の不具合対応
            if (interests.isEmpty()) {
                // 監視
                val lastCreatedAt = Date()
                initSubscribe(userId, lastCreatedAt)
            }
        }

    }

    /**
     * 監視メソッド
     *
     */
    private fun initSubscribe(userId: String, lastCreatedAt: Date) {
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
                    val tmp: MutableList<Interest>? = items.value?.toMutableList()
                    tmp?.add(it)
                    items.postValue(tmp)
                }
            }
    }
}