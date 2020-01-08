package com.komugirice.icchat

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.Encoder
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import com.komugirice.icchat.databinding.QrCodeDialogBinding
import com.komugirice.icchat.firebase.firebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.store.UserStore
import kotlinx.android.synthetic.main.activity_add_friend.*
import kotlinx.android.synthetic.main.activity_chat.backImageView
import timber.log.Timber


class AddFriendActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
        initialize()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)?.also {
            if (resultCode == Activity.RESULT_OK) {
                val targetUserId = it.contents
                // 必須チェック
                if(targetUserId.isEmpty()) {
                    AlertDialog.Builder(this@AddFriendActivity)
                        .setMessage("QRコードの読み取りに失敗しました")
                        .setPositiveButton("OK", null)
                        .show()
                    return
                }
                Timber.d("onReceiveByQr:${targetUserId}")
                    UserStore.getTargetUser(targetUserId) {

                        AlertDialog.Builder(this)
                            .setTitle("${it.name}")
                            .setMessage("ユーザを友だち登録しますか？")
                            .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {

                                    val onFailed = {
                                        Toast.makeText(
                                            this@AddFriendActivity,
                                            "既に登録済みです。",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    // Users更新
                                    firebaseFacade.addFriend(targetUserId, onFailed) {
                                        AlertDialog.Builder(this@AddFriendActivity)
                                            .setMessage("友だち登録が完了しました")
                                            .setPositiveButton("OK", null)
                                            .show()
                                    }
                                }
                            })
                            .setNegativeButton("キャンセル", null)
                            .show()
                    }
            }
        }
    }

    private fun initialize() {
        initClick()
    }

    private fun initClick() {
        // <ボタン
        backImageView.setOnClickListener {
            this.onBackPressed()
        }

        readQRCodeButton.setOnClickListener{
            IntentIntegrator(this)
                .setBeepEnabled(false)
                .apply {
                setPrompt("Scan a QR code")
                captureActivity = QrCodeCaptureActivity::class.java
            }.initiateScan()
        }

        displayQRCodeButton.setOnClickListener {
            showQrDialog(makeQrBitmap(UserManager.myUserId))
        }

        userSearchButton.setOnClickListener {
            SearchUserActivity.start(this)
        }

    }

    private fun showQrDialog(bitmap: Bitmap) {
        MaterialDialog(this).apply {
            cancelable(false)
            val binding = QrCodeDialogBinding.inflate(LayoutInflater.from(this@AddFriendActivity), null, false)
            binding.apply {
                okButton.setOnClickListener {
                    bitmap.recycle()
                    dismiss()
                }
                imageView.setImageBitmap(bitmap)
            }
            setContentView(binding.root)
        }.show()
    }

    private fun makeQrBitmap(target: String): Bitmap {
        // QRコードのビットマトリクスを作成
        val qrCode = Encoder.encode(target, ErrorCorrectionLevel.H)
        val byteMatrix = qrCode.matrix

        // QRコードのサイズのBitmapを作成
        var bitmap = Bitmap.createBitmap(
            byteMatrix.width,
            byteMatrix.height,
            Bitmap.Config.ARGB_8888
        )

        // 各ピクセルを黒か白で埋める
        for (y in 0 until byteMatrix.height) {
            for (x in 0 until byteMatrix.width) {
                val pos = byteMatrix.get(x, y)
                bitmap.setPixel(x, y, if (pos.toInt() == 1) Color.BLACK else Color.WHITE)
            }
        }
        // 必要な大きさに拡大する
        bitmap = Bitmap.createScaledBitmap(bitmap, QR_CODE_SIZE, QR_CODE_SIZE, false)
        return bitmap
    }

    companion object {
        private val QR_CODE_SIZE = applicationContext.resources.getDimensionPixelSize(R.dimen.qr_code_size)
        fun start(context: Context?) =
            context?.startActivity(Intent(context, AddFriendActivity::class.java))
    }
}
