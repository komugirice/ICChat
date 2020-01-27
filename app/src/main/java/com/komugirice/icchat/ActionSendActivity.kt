package com.komugirice.icchat

import android.content.Intent
import android.os.Bundle
import com.komugirice.icchat.extension.extractURL
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.komugirice.icchat.data.model.OgpData
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.store.InterestStore
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
            getJsoupDocument(this,{
                ogpData = OgpData().apply{
                    this.url = this@ActionSendActivity.url ?: ""
                    this.title = _getTitle(it)
                    this.imageUrl = _getImage(it, url)
                    this.description = _getDescription(it)
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

    /**
     * 非同期RxでJsoup実行
     * @param url
     * @param onSuccess
     * @param onError
     *
     */
    private fun getJsoupDocument(url: String, onSuccess : (Document) -> Unit, onError: (Throwable) -> Unit) {

        Single.fromCallable {
            val getData = Jsoup.connect(url).get()
            return@fromCallable getData
        }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            onSuccess.invoke(it)
        }, {
            onError.invoke(it)
        })

        // こっちでも可
//        Observable.just(url)
//            .map {
//                Jsoup.connect(url).get()
//            }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                onSuccess.invoke(it)
//            }, {
//                onError.invoke(it)
//            })

    }

    /**
     * og:title取得
     * https://qiita.com/shikato/items/40ce3955cb7975ad4e2b
     *
     */
    private fun _getTitle(aDocument: Document): String? { // OGPタイトル取得
        val elements =
            aDocument.getElementsByAttributeValue("property", "og:title")
        return if (elements.hasAttr("content")) {
            elements.attr("content")
        } else aDocument.title() ?: return ""
        // OGPセットされてない場合はtitleタグの内容を返す
    }

    /**
     * og:description取得
     * https://qiita.com/shikato/items/40ce3955cb7975ad4e2b
     *
     */
    private fun _getDescription(aDocument: Document): String? { // OGP description取得
        var elements =
            aDocument.getElementsByAttributeValue("property", "og:description")
        if (elements.hasAttr("content")) {
            return elements.attr("content")
        }
        // OGPセットされてない場合はdescriptionタグの内容を返す
        elements = aDocument.getElementsByAttributeValue("property", "description")
        return if (elements.hasAttr("content")) {
            elements.attr("content")
        } else ""
    }

    /**
     * og:description取得
     * https://qiita.com/shikato/items/40ce3955cb7975ad4e2b
     *
     */
    private fun _getImage(
        aDocument: Document,
        aUrl: String
    ): String? { // OGP image取得
        var elements =
            aDocument.getElementsByAttributeValue("property", "og:image")
        if (elements.hasAttr("content")) {
            var imgPath = elements.attr("content")
            // http or httpsで始まるフルパスを取得する
            imgPath = _getFullPath(imgPath, aUrl)
            if (imgPath != null) {
                return imgPath
            }
        }
        // OGPない場合はitemprop属性を見る
        elements = aDocument.getElementsByAttributeValue("itemprop", "image")
        for (element in elements) {
            var imgPath = element.attr("content")
            imgPath = _getFullPath(imgPath, aUrl)
            if (imgPath == null) {
                continue
            }
            return imgPath
        }
        // itemprop属性も無い場合はimgタグを見る
        // リクエスト増えるけどサイズやMIMEタイプ見たりしても良いかも
        elements = aDocument.getElementsByTag("img")
        for (element in elements) {
            var imgPath = element.attr("src")
            imgPath = _getFullPath(imgPath, aUrl)
            if (imgPath == null) {
                continue
            }
            return imgPath
        }
        return ""
    }

    /**
     * FullPath取得
     * https://qiita.com/shikato/items/40ce3955cb7975ad4e2b
     *
     */
    private fun _getFullPath(
        aImagePathStr: String,
        aOgpUrlStr: String
    ): String? {
        return if (aImagePathStr.indexOf("http://") == 0 || aImagePathStr.indexOf("https://") == 0) {
            aImagePathStr
        } else try {
            val ogpUri = URI(aOgpUrlStr)
            val imgUri: URI = ogpUri.resolve(aImagePathStr)
            imgUri.toString()
        } catch (e: URISyntaxException) {
            null
        }
    }


}