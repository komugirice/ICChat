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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.ViewPager
import com.komugirice.icchat.extension.toggle
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.interfaces.Update
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_drawer.*

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
     * かといってタブ切り替えでは更新したくないのでonRestart
     *
     */
    override fun onRestart() {
        super.onRestart()
        customAdapter.fragments.forEach {
            if(it.fragment is Update)
                it.fragment.update()
        }

    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
            return
        }
        super.onBackPressed()
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
        initDrawerLayout()
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

        myTextView.setOnClickListener {
            Toast.makeText(this, "自分ボタンが押されたよ", Toast.LENGTH_SHORT).show()
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
                    headerTextView?.text = customAdapter.getPageTitle(position)
                    closeDrawer()
                    drawerLayout.setDrawerLockMode(if (position == VISIBLE_DRAWER_POSITION) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
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

    private fun initDrawerLayout() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    /**
     * 興味UserId変更処理
     * @param newUserId : 新しいUserId
     */
    private fun changeInterestUserId(newUserId: String) {
        customAdapter.fragments.map { it.fragment }.forEach {
            if (it is InterestFragment)
                it.updateUserId(newUserId)
        }
    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun openDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            return
        drawerLayout.openDrawer(GravityCompat.START)
    }

    /**
     * CustomAdapterクラス
     * TabLayout用
     *
     */
    class CustomAdapter(private val context: Context, fragmentManager: FragmentManager, behavor: Int) :
        FragmentPagerAdapter(fragmentManager, behavor){

        inner class Item(val fragment: Fragment, val title:Int)

        val fragments = listOf(Item(FriendFragment(), R.string.tab_frient)
            , Item(RoomFragment(), R.string.tab_room)
            , Item(InterestFragment().apply {
                arguments = Bundle().apply {
                    putString(InterestFragment.KEY_USER_ID, UserManager.myUserId)
                }
            }, R.string.tab_interest)
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
                            .setPositiveButton(R.string.ok, object: DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    LoginActivity.signOut(this@MainActivity)
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
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
        private const val VISIBLE_DRAWER_POSITION = 2
        fun start(activity: Activity) = activity.apply {
            startActivity(Intent(activity, MainActivity::class.java))
        }
    }
}
