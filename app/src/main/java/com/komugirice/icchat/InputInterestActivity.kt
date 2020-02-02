package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.komugirice.icchat.data.model.OgpData
import com.komugirice.icchat.databinding.ActivityInputInterestBinding
import com.komugirice.icchat.databinding.DateTimePickerDialogBinding
import com.komugirice.icchat.databinding.UrlPreviewDialogBinding
import com.komugirice.icchat.extension.toggle
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.services.JsoupService
import com.komugirice.icchat.viewModel.InputInterestViewModel
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_header.view.*
import timber.log.Timber
import java.io.File
import java.util.*

class InputInterestActivity : BaseActivity() {

    private lateinit var binding: ActivityInputInterestBinding
    private lateinit var viewModel: InputInterestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        initBinding()
        initViewModel()
        initLayout()
        initClick()
        initRadioGroup()
    }

    private fun initBinding(){
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_input_interest
        )
        binding.lifecycleOwner = this
        binding.isSelectedImage = true
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(InputInterestViewModel::class.java).apply {

        }
    }


    private fun initLayout(){
        // タイトル
        binding.header.titleTextView.text = getString(R.string.input_interest_activity_title)
        // 登録日時
        binding.createdAt.text = "${DateFormat.format("yyyy年MM月dd日 hh時mm分", Date())}"
    }

    private fun initClick(){
        // 戻る
        binding.header.backImageView.setOnClickListener {
            finish()
        }

        // 画像
        binding.interestImageView.setOnClickListener {
            if(binding.interestImageView.drawable == null)
                selectImage()
            else
                showImageChangeDialog()
        }

        // 画像長押し
        binding.interestImageView.setOnLongClickListener(object: View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                if (binding.interestImageView.drawable != null)
                    showImageChangeDialog()

                return true
            }
        })

        // チェック
        binding.checkButton.setOnClickListener{
            searchUrl()
        }

        // 登録日時
        binding.createdAt.setOnClickListener {
            showCreatedAtDialog()
        }

        binding.container.setOnClickListener {
            hideKeybord(it)
        }
    }

    private fun initRadioGroup() {
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            binding.isSelectedImage = binding.imageRadioButton.isChecked

        }
    }

    /**
     * プロフィール画像選択
     *
     */
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("image/jpeg")
        startActivityForResult(intent, RC_CHOOSE_IMAGE)
    }
    /**
     * ActivityResult
     *
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null)
            return
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        when(requestCode) {
            // 画像選択
            RC_CHOOSE_IMAGE -> {

                data.data?.also {
                    // uCrop実行
                    startUCrop(it)
                }

            }
            UCrop.REQUEST_CROP -> {
                val resultUri = UCrop.getOutput(data)

                resultUri?.also {
                    Timber.d("画像URL：$it")

                    Picasso.get().load(it).into(binding.interestImageView) // UIスレッド
                    binding.addImageButton.toggle(false)

                }
            }
            UCrop.RESULT_ERROR -> {
                Timber.d(UCrop.getError(data))
                Toast.makeText(this, R.string.failed_ucrop, Toast.LENGTH_SHORT).show()
            }

        }
    }

    /**
     * uCropActivity表示
     *
     */
    private fun startUCrop(srcUri: Uri?) {
        var file = File.createTempFile("${System.currentTimeMillis()}", ".temp", cacheDir)
        srcUri?.apply {
            UCrop.of(this, file.toUri())
                .withAspectRatio(1f, 1f)
                .withOptions(UCrop.Options().apply {
                    setHideBottomControls(true)
                    setCircleDimmedLayer(false)
                    setShowCropGrid(false)
                    setShowCropFrame(false)
                    withAspectRatio(binding.interestImageView.width.toFloat()
                        ,  binding.interestImageView.height.toFloat())
                })
                .start(this@InputInterestActivity)
        }
    }

    /**
     * URL検索
     */
    private fun searchUrl() {
        val url = binding.url.text.toString()

        if( url.isEmpty()) {
            Toast.makeText(this, R.string.url_empty, Toast.LENGTH_SHORT).show()
            binding.ogpData = null
            binding.isCheckedUrl = false
            return
        }

        JsoupService.getJsoupDocument(url, {
            val ogpData = OgpData().apply{
                this.url = url
                this.title = JsoupService._getTitle(it)
                this.imageUrl = JsoupService._getImage(it, url)
                this.description = JsoupService._getDescription(it)
            }
            binding.ogpData = ogpData
            binding.isCheckedUrl = true

            // プレビューダイアログ表示
            MaterialDialog(this).apply {
                cancelable(true)
                val dialogBinding = UrlPreviewDialogBinding.inflate(LayoutInflater.from(this@InputInterestActivity), null, false)
                dialogBinding.apply {
                    this.ogpData = ogpData
                    ogpImageView.setOnClickListener {
                        dismiss()
                    }
                }
                setContentView(dialogBinding.root)
            }.show()


        }, {
            Timber.e(it)
            Toast.makeText(this, R.string.url_error, Toast.LENGTH_SHORT).show()
            binding.isCheckedUrl = false
        })
    }

    private fun showImageChangeDialog() {
        val menuList = listOf(
            Pair(0, R.string.change),
            Pair(1, R.string.delete_message)
        )

        MaterialDialog(this).apply {
            listItems(items = listOf(
                context.getString(menuList.get(0).second),
                context.getString(menuList.get(1).second)
            ),
                selection = { dialog, index, text ->
                    when (index) {
                        menuList.get(0).first -> {
                            selectImage()
                        }
                        menuList.get(1).first -> {
                            binding.interestImageView.setImageDrawable(null)
                            binding.addImageButton.toggle(true)
                        }

                        else -> return@listItems
                    }
                })
        }.show()
    }

    private fun showCreatedAtDialog() {
        MaterialDialog(this).apply {
            val dateTimePickerDialogBinding = DateTimePickerDialogBinding.inflate(LayoutInflater.from(this@InputInterestActivity), null, false)
            dateTimePickerDialogBinding.okButton.setOnClickListener {
                val date = Calendar.getInstance().apply {
                    set(Calendar.YEAR, dateTimePickerDialogBinding.datePicker.year)
                    set(Calendar.MONTH, dateTimePickerDialogBinding.datePicker.month)
                    set(Calendar.DAY_OF_MONTH, dateTimePickerDialogBinding.datePicker.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, dateTimePickerDialogBinding.timePicker.currentHour)
                    set(Calendar.MINUTE, dateTimePickerDialogBinding.timePicker.currentMinute)
                }.time
                Toast.makeText(this@InputInterestActivity, "${DateFormat.format("yyyy年MM月dd日 hh時mm分", date)}", Toast.LENGTH_SHORT).show()
                binding.createdAt.text = "${DateFormat.format("yyyy年MM月dd日 hh時mm分", date)}"
                dismiss()
            }
            setContentView(dateTimePickerDialogBinding.root)
        }.show()
    }

    companion object {
        private const val KEY_INTEREST = "key_interest"
        private const val RC_CHOOSE_IMAGE = 1000

        fun start(context: Context?) {
            context?.startActivity(
                Intent(context, InputInterestActivity::class.java)
            )
        }

        fun update(context: Context?, interest: Interest?) =
            context?.startActivity(
                Intent(context, InputInterestActivity::class.java)
                    .putExtra(KEY_INTEREST, interest)
            )

    }
}
