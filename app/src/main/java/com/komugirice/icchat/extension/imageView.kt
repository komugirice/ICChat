package com.komugirice.icchat.extension

import android.graphics.*
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.komugirice.icchat.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import timber.log.Timber

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