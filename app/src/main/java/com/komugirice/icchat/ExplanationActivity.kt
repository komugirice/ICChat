package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import androidx.core.view.updatePaddingRelative
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.komugirice.icchat.databinding.ActivityExplanationBinding
import com.komugirice.icchat.databinding.IndicatorLayoutBinding
import com.komugirice.icchat.view.ExplanationAdapter
import kotlinx.android.synthetic.main.activity_header.view.*

class ExplanationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExplanationBinding

    private val pagerAdapter by lazy { ExplanationAdapter() }

    private val indicatorLayouts = mutableListOf<IndicatorLayoutBinding>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initBinding
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_explanation
        )

        binding.lifecycleOwner = this

        initLayout()
        initViewPager2()
        initClick()
        initPagerButton()
    }

    private fun initLayout() {
        binding.header.titleTextView.text = getString(R.string.explanation_activity_title)
    }

    private fun initViewPager2() {
        binding.viewpager2.apply {
            adapter = pagerAdapter
            registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    changePagerButtonColor(position)
                }
            })
        }
    }

    private fun initClick() {
        binding.header.backImageView.setOnClickListener {
            finish()
        }
    }

    private fun initPagerButton() {
        binding.pagerButton.removeAllViews()
        for (i in 0 until pagerAdapter.itemCount) {
            binding.pagerButton.addView(ImageView(this).apply {
                setImageResource(R.drawable.ic_fiber_manual_record_gray_24dp)
                setOnClickListener {
                    changePage(i)
                }
            }, LinearLayout.LayoutParams(resources.getDimensionPixelSize(R.dimen.pager_button_image_length), resources.getDimensionPixelSize(R.dimen.pager_button_image_length)).apply {

                gravity = Gravity.CENTER
                setMargins(15, 0, 15, 0)

            })
        }
//        for (i in 0 until pagerAdapter.itemCount) {
//            val indicatorLayoutBinding = IndicatorLayoutBinding.inflate(LayoutInflater.from(this), null, false)
//            binding.pagerButton.addView(indicatorLayoutBinding.root)
//            indicatorLayouts.add(indicatorLayoutBinding)
//            indicatorLayoutBinding.root.setOnClickListener {
//                // ViewPagerのcurrentItem変更処理
//                changePage(i)
//            }
//        }

    }

    private fun changePage(index: Int) {
        binding.viewpager2.setCurrentItem(index, true)
    }

    private fun changePagerButtonColor(index: Int) {
        for (i in 0 until binding.pagerButton.childCount) {
            (binding.pagerButton.getChildAt(i) as? ImageView)?.also {
                it.setImageResource(if (i == index) R.drawable.ic_fiber_manual_record_red_24dp else R.drawable.ic_fiber_manual_record_gray_24dp)
            }
        }
    }


    companion object {

        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, ExplanationActivity::class.java)
            )
    }
}
