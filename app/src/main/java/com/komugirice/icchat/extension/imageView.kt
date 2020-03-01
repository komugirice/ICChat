package com.komugirice.icchat.extension

import android.graphics.*
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.komugirice.icchat.R
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.util.FireStorageUtil
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import timber.log.Timber


/**
 * xmlでImageViewに:imageUrlを設定すると画像が取得できる
 *
 * @param url
 *
 */
@BindingAdapter("imageUrl")
fun ImageView.loadImage(url: String?) {
    Picasso.get().load(url).into(this)
}

/**
 * ユーザアイコンを設定する
 *
 * @param userId
 *
 */
@BindingAdapter("userIconImageUrl")
fun ImageView.loadUserIconImage(userId: String?) {
    if(userId == null) return
    FireStorageUtil.getUserIconImage(userId) {
        this.decorateRoundedImageView(it)
    }
}

/**
 * RoomFragmentにアイコン画像を設定する
 *
 * @param url
 *
 */
@BindingAdapter("roomIconImageUrl")
fun ImageView.loadRoomIconImage(room: Room?) {
    if(room == null) return

    this.setImageDrawable(null)

    // シングルルームとグループルームで分岐
    if(room.isGroup == false) {
        // シングルルームの場合
        val friendId  = room.userIdList.filter{ !it.equals(UserManager.myUserId) }.first()
        FireStorageUtil.getUserIconImage(friendId) {
            this.decorateRoundedImageView(it)
        }
    } else {
        // グループルームの場合
        FireStorageUtil.getGroupIconImage(room.documentId) {
            this.decorateRoundedImageView(it)
        }
    }
}

/**
 * メッセージの画像を設定する
 *
 * @param url
 *
 */
@BindingAdapter("messageImageUrl")
fun ImageView.loadMessageImage(message: Message) {
    // 画像タイプ判定
    if(!MessageType.getValue(message.type).isImage) return

    FireStorageUtil.getRoomMessageImage(message){
        Picasso.get().load(it).into(this)

    }
}

/**
 * 興味データの画像を設定する
 *
 * @param url
 *
 */
@BindingAdapter("userIdForInterestImage", "interestImageFileName")
fun ImageView.loadInterestImage(userId: String?, fileName: String?) {
    if(userId == null || fileName == null) return

    FireStorageUtil.getInterestImage(userId, fileName){
        Picasso.get().load(it).into(this)
    }
}

fun ImageView.decorateRoundedImageView(uri: Uri?) {
    if(uri != null) {
        this.setRoundedImageView(uri)
    } else {
        // nullの場合、背景画像
        this.setImageResource(R.drawable.background_icon_image)
    }
}

fun ImageView.setRoundedImageView(uri: Uri?) {
    if (uri == null) {
        // 画像を取り除く処理
        Picasso.get().load(R.drawable.dummy).into(this)
        return
    }

    Picasso.get().load(uri).transform(object: Transformation{
        override fun key(): String {
            return "rounded"
        }

        override fun transform(source: Bitmap?): Bitmap {
            source ?: return BitmapFactory.decodeResource(context.resources, R.drawable.dummy)
            // 最終出力サイズ取得
            var size = Math.min(source.width, source.height)
            
            var x = (source.width - size) / 2
            var y = (source.height - size) / 2
            
            var squareBitmap = Bitmap.createBitmap(source, x, y, size, size)
            
            if (squareBitmap != source)
                source.recycle()
            
            var bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            var canvas = Canvas(bitmap)
            var paint = Paint()
            var bitmapShader = BitmapShader(squareBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

            //paint.shader = bitmapShader
            paint.color = ContextCompat.getColor(context, android.R.color.white)
            paint.isAntiAlias = true
            
            var radius = size.toFloat() / 2f
            canvas.drawCircle(radius, radius, radius, paint)
            var rect = Rect(0, 0, size, size)
            canvas.drawBitmap(squareBitmap, rect, rect, Paint(paint).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            })

            squareBitmap.recycle()
            return bitmap
            
        }

    }).into(this)

}

@BindingAdapter("imageUrlNoImage")
fun ImageView.loadImageUrl(imageUrl: String?) {
    Timber.d("loadImageUrl imageUrl:$imageUrl")

    if (imageUrl == null || imageUrl.isEmpty() || !imageUrl.hasImageExtension())
        setImageResource(R.drawable.no_image)
    else
        Picasso.get().load(imageUrl).into(this)
}