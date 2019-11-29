package com.komugirice.icchat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.ViewPager
import com.komugirice.icchat.fragment.DebugFragment
import com.komugirice.icchat.fragment.FriendFragment
import com.komugirice.icchat.fragment.InterestFragment
import com.komugirice.icchat.fragment.RoomFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // TabLayoutで使用
    private val customAdapter by lazy { CustomAdapter(this, supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) }

    /**
     * onCreateメソッド
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize()
    }

    /**
     * initializeメソッド
     *
     */
    private fun initialize() {
        initLayout()
    }

    /**
     * initLayoutメソッド
     *
     */
    private fun initLayout() {
        initClick()
        initViewPager()
        initTabLayout()
    }

    /**
     * initClickメソッド
     *
     */
    private fun initClick() {

    }

    /**
     * initViewPagerメソッド
     *
     */
    private fun initViewPager() {
        viewPager.apply {
            adapter = customAdapter
            offscreenPageLimit = customAdapter.count
            addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    headerTextView.text = customAdapter.getPageTitle(position)
                }
            })
        }
    }

    /**
     * initTabLayoutメソッド
     *
     */
    private fun initTabLayout() {
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)?.setCustomView(R.layout.design_fragment_icon_person)
        tabLayout.getTabAt(0)?.setText("")
        tabLayout.getTabAt(1)?.setCustomView(R.layout.design_fragment_icon_chat)
        tabLayout.getTabAt(1)?.setText("")
        tabLayout.getTabAt(2)?.setCustomView(R.layout.design_fragment_icon_interest)
        tabLayout.getTabAt(2)?.setText("")
        tabLayout.getTabAt(3)?.setText(R.string.tab_debug)
    }

    /**
     * CustomAdapterクラス
     * TabLayout用
     *
     */
    class CustomAdapter(private val context: Context, fragmentManager: FragmentManager, behavor: Int) :
        FragmentPagerAdapter(fragmentManager, behavor) {

        inner class Item(val fragment: Fragment, val title:Int)

        val fragments = listOf(Item(FriendFragment(), R.string.tab_frient)
            , Item(RoomFragment(), R.string.tab_room)
            , Item(InterestFragment(), R.string.tab_interest)
            , Item(DebugFragment(), R.string.tab_debug))

        override fun getCount(): Int = fragments.size

        override fun getItem(position: Int) = fragments[position].fragment

        // アイコンにするのでコメントアウト
        override fun getPageTitle(position: Int) = context.getString( fragments[position].title )

    }


    companion object {
        fun start(activity: Activity) = activity.startActivity(Intent(activity, MainActivity::class.java))
    }
}
