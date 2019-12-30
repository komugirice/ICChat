package com.komugirice.icchat

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.ViewPager
import com.komugirice.icchat.enum.ActivityEnum
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

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
     * 各Activityから戻った時にRoomが更新されていないバグ対応
     * かといってタブ切り替えでは更新したくない
     *
     */
    override fun onRestart() {
        super.onRestart()
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
        settingImageView.setOnClickListener {
            showSettingMenu(it)
        }

        addFriendsImageView.setOnClickListener {
            showAddFriendsMenu(it)
        }
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

    /**
     * 設定アイコンのオプションメニュー
     * @param v: View
     * @return Boolean
     *
     */
    fun showSettingMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.main_setting)
        popup.setOnMenuItemClickListener ( object: PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.profile_setting -> {
                        ProfileSettingActivity.start(this@MainActivity)
                        return true
                    }
                    R.id.logout_setting -> {
                        AlertDialog.Builder(this@MainActivity)
                            .setMessage(getString(R.string.confirm_logout))
                            .setPositiveButton("OK", object: DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    LoginActivity.signOut(this@MainActivity)
                                }
                            })
                            .setNegativeButton("キャンセル", null)
                            .show()
                        return true
                    }
                    else -> return false

                }
            }
        })
        popup.show()
    }

    /**
     * 友だち追加アイコンのオプションメニュー
     * @param v: View
     * @return Boolean
     *
     */
    fun showAddFriendsMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.add_friends)
        popup.setOnMenuItemClickListener ( object: PopupMenu.OnMenuItemClickListener {

            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.addFriends -> {
                        AddFriendActivity.start(this@MainActivity)
                        return true
                    }
                    R.id.groupSetting -> {
                        GroupSettingActivity.start(this@MainActivity)
                        return true
                    }
                    else -> return false

                }
            }
        })
        popup.show()
    }


    companion object {
        fun start(activity: Activity) = activity.apply {
            startActivity(Intent(activity, MainActivity::class.java))
        }
    }
}
