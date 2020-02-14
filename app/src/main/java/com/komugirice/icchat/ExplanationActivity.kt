package com.komugirice.icchat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.komugirice.icchat.databinding.ActivityExplanationBinding
import com.komugirice.icchat.databinding.FragmentFriendBinding
import com.komugirice.icchat.view.ExplanationAdapter
import kotlinx.android.synthetic.main.activity_change_password.view.*
import kotlinx.android.synthetic.main.activity_explanation.*
import kotlinx.android.synthetic.main.activity_header.view.*

class ExplanationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExplanationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initBinding
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_explanation
        )

        binding.lifecycleOwner = this

        initLayout()
        initAdapter()
        initClick()

    }

    private fun initLayout() {
        binding.header.titleTextView.text = getString(R.string.explanation_activity_title)
    }

    private fun initAdapter() {
        viewpager2.adapter = ExplanationAdapter()
        viewpager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
        })
    }

    private fun initClick() {
        binding.header.backButton.setOnClickListener {
            finish()
        }
    }


    companion object {

        fun start(activity: Activity?) =
            activity?.startActivity(
                Intent(activity, ExplanationActivity::class.java)
            )
    }
}
