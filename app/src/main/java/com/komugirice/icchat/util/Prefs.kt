package com.komugirice.icchat.util

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import io.reactivex.Observable
import timber.log.Timber

class Prefs {


    //val isHighQuality by lazy { BooleanEntry("is_height_quality") }
    //val settingData by lazy { SettingDataEntry("setting_data") }
    val fcmToken by lazy { StringEntry("fcm_token")}
    val hasToUpdateFcmToken by lazy {BooleanEntry("has_to_update_fcm_token")}

    interface Entry<T> {
        fun put(value: T)
        fun get(): Observable<T>
        fun remove()
    }

    class StringEntry(private val key: String, private val defaultValue: String = "") : Entry<String> {
        override fun put(value: String) {
            Timber.d("put $key -> $value")
            getSharedPreference().edit().putString(key, value).apply()
        }

        override fun get(): Observable<String> {
            return createObservable(getSharedPreference(), key) {
                it.getString(key, defaultValue) ?: defaultValue
            }.onErrorReturn {
                defaultValue
            }
        }

        override fun remove() = getSharedPreference().edit().remove(key).apply()

    }

    class BooleanEntry(private val key: String, private val defaultValue: Boolean = false) : Entry<Boolean> {

        override fun put(value: Boolean) {
            Timber.d("put $key -> $value")
            getSharedPreference().edit().putBoolean(key, value).apply()
        }

        override fun get(): Observable<Boolean> {
            return createObservable(getSharedPreference(), key) {
                it.getBoolean(key, defaultValue)
            }.onErrorReturn {
                put(defaultValue)
                defaultValue
            }
        }

        override fun remove() = getSharedPreference().edit().remove(key).apply()
    }

    class IntEntry(private val key: String, private val defaultValue: Int = 0) : Entry<Int> {
        override fun put(value: Int) {
            Timber.d("put $key -> $value")
            getSharedPreference().edit().putInt(key, value).apply()
        }

        override fun get(): Observable<Int> {
            return createObservable(getSharedPreference(), key) {
                it.getInt(key, defaultValue)
            }.onErrorReturn {
                put(defaultValue)
                defaultValue
            }
        }

        override fun remove() = getSharedPreference().edit().remove(key).apply()
    }

    class LongEntry(private val key: String, private val defaultValue: Long = 0L) : Entry<Long> {
        override fun put(value: Long) {
            Timber.d("put $key -> $value")
            getSharedPreference().edit().putLong(key, value).apply()
        }

        override fun get(): Observable<Long> {
            return createObservable(getSharedPreference(), key) {
                it.getLong(key, defaultValue)
            }.onErrorReturn {
                put(defaultValue)
                defaultValue
            }
        }

        override fun remove() = getSharedPreference().edit().remove(key).apply()
    }

    class SettingDataEntry(private val key: String, private val defaultValue: SettingData = SettingData()): Entry<SettingData> {
        override fun put(value: SettingData) {
            Timber.d("put $key -> $value")
            getSharedPreference().edit().putString(key, Gson().toJson(value)).apply()
        }
        override fun get(): Observable<SettingData> {
            return createObservable(getSharedPreference(), key) {
                Gson().fromJson(it.getString(key, ""), SettingData::class.java)
            }.onErrorReturn {
                defaultValue
            }
        }
        override fun remove() = getSharedPreference().edit().remove(key).apply()
    }

    class EntryNotFoundException : Exception {
        constructor(detailMessage: String?) : super(detailMessage)
    }

    companion object {
        fun getSharedPreference(): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(applicationContext)
        }

        fun <T> createObservable(preferences: SharedPreferences, key: String, valueF: (SharedPreferences) -> T): Observable<T> {
            return Observable.create { subscriber ->
                try {
                    if (preferences.contains(key)) {
                        subscriber.onNext(valueF(preferences))
                        subscriber.onComplete()
                    } else {
                        subscriber.onError(EntryNotFoundException("Not found $key in SharedPreferences"))
                    }
                } catch (e: Exception) {
                    subscriber.onError(e)
                }
            }
        }

    }
}