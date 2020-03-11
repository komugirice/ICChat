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
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.store.InterestStore
import com.komugirice.icchat.services.JsoupService
import com.komugirice.icchat.util.FireStorageUtil
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

    private fun initBinding() {
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_input_interest
        )
        binding.lifecycleOwner = this
        binding.isSelectedImage = false
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this).get(InputInterestViewModel::class.java).apply {
            // 更新モードの場合は、intentからデータが取得される。
            intent.getSerializableExtra(KEY_INTEREST).also {
                if (it is Interest && it.documentId.isNotEmpty()) {
                    interestData = it
                    binding.interest = it

                    // 更新モードON
                    isUpdateMode = true

                    interestData.apply{

                        if (this.isOgp) {
                            binding.urlRadioButton.isChecked = true
                            binding.isCheckedUrl = true
                            // ogp設定
                            ogpData = OgpData(this)
                        } else {
                            //画像設定
                            this.image?.apply{
                                // 画像有りの場合（コメントだけのデータはURLにチェックしたいので）
                                binding.imageRadioButton.isChecked = true
                                binding.isSelectedImage = true

                                FireStorageUtil.getInterestImage(UserManager.myUserId, this){
                                    Picasso.get().load(it).into(binding.interestImageView) // UIスレッド
                                    binding.addImageButton.toggle(false)
                                    viewModel.imageUri = it // これを設定しないと画像が消えるので注意
                                }
                            }
                        }


                    }

                }
            } ?: run {
                // 新規モードの場合、登録日時に現在日時を設定
                interestData.createdAt = Date()

            }
        }
    }


    private fun initLayout() {
        // タイトル
        binding.header.titleTextView.text = getString(R.string.input_interest_activity_title)

        // 登録日時
        binding.createdAt.text =
            "${DateFormat.format(getString(R.string.input_interest_activity_date), viewModel.interestData.createdAt)}"
    }

    private fun initClick() {
        // 戻る
        binding.header.backImageView.setOnClickListener {
            finish()
        }

        // 画像
        binding.interestImageView.setOnClickListener {
            if (binding.interestImageView.drawable == null)
                selectImage()
            else
                showImageChangeDialog()
        }

        // 画像長押し
        binding.interestImageView.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                if (binding.interestImageView.drawable != null)
                    showImageChangeDialog()

                return true
            }
        })

        // チェック
        binding.checkButton.setOnClickListener {
            searchUrl()
        }

        // 登録日時
        binding.createdAt.setOnClickListener {
            showCreatedAtDialog()
        }

        // 登録ボタン
        binding.saveButton.setOnClickListener {
            if(validateInputData())
                registInterest()
        }

        binding.container.setOnClickListener {
            hideKeybord(it)
        }
        // containerでは効かないので追加
        binding.contents.setOnClickListener {
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
            .setType("image/*")
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
        when (requestCode) {
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

                    // 各種設定
                    viewModel.imageUri = it
                    viewModel.isImageUpdate = true
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
                    withAspectRatio(
                        binding.interestImageView.width.toFloat()
                        , binding.interestImageView.height.toFloat()
                    )
                })
                .start(this@InputInterestActivity)
        }
    }

    /**
     * URL検索
     */
    private fun searchUrl() {
        val url = binding.url.text.toString()

        if (url.isEmpty()) {
            Toast.makeText(this, R.string.url_empty, Toast.LENGTH_SHORT).show()
            viewModel.ogpData = null
            binding.isCheckedUrl = false
            return
        }

        JsoupService.getJsoupDocument(url, {
            val ogpData = OgpData().apply {
                this.ogpUrl = url
                this.ogpTitle = JsoupService._getTitle(it)
                this.ogpImageUrl = JsoupService._getImage(it, url)
                this.ogpDescription = JsoupService._getDescription(it)
            }
            viewModel.ogpData = ogpData
            binding.isCheckedUrl = true

            // プレビューダイアログ表示
            MaterialDialog(this).apply {
                cancelable(true)
                val dialogBinding = UrlPreviewDialogBinding.inflate(
                    LayoutInflater.from(this@InputInterestActivity),
                    null,
                    false
                )
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
            viewModel.ogpData = null
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
                            viewModel.imageUri = null
                            viewModel.isImageUpdate = true
                        }

                        else -> return@listItems
                    }
                })
        }.show()
    }

    private fun showCreatedAtDialog() {
        MaterialDialog(this).apply {
            val dateTimePickerDialogBinding = DateTimePickerDialogBinding.inflate(
                LayoutInflater.from(this@InputInterestActivity),
                null,
                false
            )
            // 初期値設定
            val settingDate = Calendar.getInstance()
            settingDate.time = viewModel.interestData.createdAt
            dateTimePickerDialogBinding.datePicker.apply {
                maxDate = Date().time
                updateDate(settingDate.get(Calendar.YEAR), settingDate.get(Calendar.MONTH), settingDate.get(Calendar.DAY_OF_MONTH))

            }
            dateTimePickerDialogBinding.timePicker.apply {
                currentHour = settingDate.get(Calendar.HOUR_OF_DAY)
                currentMinute = settingDate.get(Calendar.MINUTE)
            }

            dateTimePickerDialogBinding.okButton.setOnClickListener {
                val date = Calendar.getInstance().apply {
                    set(Calendar.YEAR, dateTimePickerDialogBinding.datePicker.year)
                    set(Calendar.MONTH, dateTimePickerDialogBinding.datePicker.month)
                    set(Calendar.DAY_OF_MONTH, dateTimePickerDialogBinding.datePicker.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, dateTimePickerDialogBinding.timePicker.currentHour)
                    set(Calendar.MINUTE, dateTimePickerDialogBinding.timePicker.currentMinute)
                }.time
                //Toast.makeText(this@InputInterestActivity, "${DateFormat.format("yyyy年MM月dd日 HH時mm分", date)}", Toast.LENGTH_SHORT).show()
                binding.createdAt.text =
                    "${DateFormat.format(getString(R.string.input_interest_activity_date), date)}"
                viewModel.interestData.createdAt = date // 直に設定する
                dismiss()
            }
            dateTimePickerDialogBinding.cancelButton.setOnClickListener {
                dismiss()
            }
            setContentView(dateTimePickerDialogBinding.root)
        }.show()
    }

    /**
     * 登録ボタン押下でエラーチェック
     */
    private fun validateInputData(): Boolean {
        val comment = binding.comment.text.toString()

        // URLチェック時、URLorコメントの必須チェック
        if (binding.urlRadioButton.isChecked) {

            if((binding.isCheckedUrl == null || binding.isCheckedUrl == false)
                && comment.isEmpty()) {
                Toast.makeText(this, R.string.regist_validate_error, Toast.LENGTH_SHORT).show()
                return false
            }
        }

        // 画像チェック時、画像orコメントの必須チェック
        if (binding.imageRadioButton.isChecked) {
            if(binding.interestImageView.drawable == null && comment.isEmpty()) {
                Toast.makeText(this, R.string.regist_validate_error, Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    /**
     * 登録ボタン押下でプレビュー表示
     */
    private fun previewRegistData() {

    }

    /**
     * 入力された興味データを登録する。
     */
    private fun registInterest() {
        val data = viewModel.interestData

        showProgressDialog(this)

        // documentId
        if (data.documentId.isEmpty()) data.documentId = UUID.randomUUID().toString()

        // コメント
        val comment = binding.comment.text.toString()
        if(comment.isEmpty())
            data.comment = null
        else
            data.comment = comment

        // URL
        viewModel.ogpData?.apply {
            data.isOgp = true
            data.setOgpData(this)
        } ?: run {
            data.isOgp = false
            data.setOgpData(OgpData())
        }

        // 画像
        val imageFileName = "${System.currentTimeMillis()}.jpg"
        // 新規モード：画像データ有り、更新モード：画像データ更新、の場合は設定
        if (viewModel.isImageUpdate && viewModel.imageUri != null) {
            data.image = imageFileName
        } else if(viewModel.imageUri == null){
            data.image = null
        }

        // 登録日時は設定済

        // FireStoreに登録
        InterestStore.registerInterest(data) {

            // 画像をFireStorageに登録
            if (viewModel.isImageUpdate) {
                // 更新モード(data.imageに値有り)
                if (viewModel.isUpdateMode) {
                    // 更新モードの場合、変更・削除、いづれも元画像削除
                    FireStorageUtil.deleteInterestImage(data.image) {
                        // 更新モード：変更
                        viewModel.imageUri?.apply {
                            // 変更画像登録
                            FireStorageUtil.registInterestImage(imageFileName, this) {
                            }
                        }
                    }
                } else {
                    // 新規モード
                    viewModel.imageUri?.apply {
                        // 画像登録
                        FireStorageUtil.registInterestImage(imageFileName, this) {
                        }
                    }
                }
            }
            Toast.makeText(this, R.string.regist_complete, Toast.LENGTH_SHORT).show()
            dismissProgressDialog()
            finish()


        }

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
