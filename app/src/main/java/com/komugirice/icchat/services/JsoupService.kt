package com.komugirice.icchat.services

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.net.URISyntaxException

class JsoupService {
    companion object {

        /**
         * 非同期RxでJsoup実行
         * @param url
         * @param onSuccess : Document
         * @param onError
         *
         */
        fun getJsoupDocument(url: String, onSuccess : (Document) -> Unit, onError: (Throwable) -> Unit) {

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
        fun _getTitle(aDocument: Document): String? { // OGPタイトル取得
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
        fun _getDescription(aDocument: Document): String? { // OGP description取得
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
        fun _getImage(
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
}