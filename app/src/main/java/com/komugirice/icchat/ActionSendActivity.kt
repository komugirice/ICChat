package com.komugirice.icchat

import android.content.Intent
import android.os.Bundle
import com.komugirice.icchat.extension.extractURL
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.komugirice.icchat.data.model.OgpData
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.store.InterestStore
import com.komugirice.icchat.services.JsoupService
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber
import java.net.URI
import java.net.URISyntaxException


class ActionSendActivity: BaseActivity() {

    var url: String? = null
    lateinit var ogpData: OgpData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ログインしていない場合、終了
        if(FirebaseAuth.getInstance().currentUser == null) {
            finish()
            return
        }


        // url取得処理
        val intent = intent
        val action = intent.action
        if (Intent.ACTION_SEND == action) {
            val extras = intent.extras
            if (extras != null) {
                val ext = extras.getCharSequence(Intent.EXTRA_TEXT)
                if (ext != null) {
                    url = ext.toString().extractURL()
                    Timber.d(url)
                }
            }
        }

        url?.apply {
            // ogp取得処理
            JsoupService.getJsoupDocument(this,{
                ogpData = OgpData().apply{
                    this.ogpUrl = this@ActionSendActivity.url ?: ""
                    this.ogpTitle = JsoupService._getTitle(it)
                    this.ogpImageUrl = JsoupService._getImage(it, url.toString())
                    this.ogpDescription = JsoupService._getDescription(it)
                }
                Timber.d(Gson().toJson(ogpData))

                // FireStoreに登録
                InterestStore.registerInterestWithOgp(ogpData){
                    finish()
                }


            },{
                Timber.e(it)
            })

        }

        finish()
    }




}