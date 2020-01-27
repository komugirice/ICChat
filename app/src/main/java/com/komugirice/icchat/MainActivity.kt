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
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.komugirice.icchat.databinding.ActivityMainBinding
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.interfaces.Update
import com.komugirice.icchat.view.OtherUserView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_interest.*
import kotlinx.android.synthetic.main.item_drawer.*
import kotlinx.android.synthetic.main.item_drawer.swipeRefreshLayout

class MainActivity : BaseActivity() {

    // TabLayoutで使用
    private val customAdapter by lazy { CustomAdapter(this, supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) }

    private lateinit var binding: ActivityMainBinding
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

    /**
     * Drawerが表示の時、バックボタンでDrawerを閉じる。画面は閉じない。
     */
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
        initBinding()
        initLayout()
    }

    /**
     * MVVMのBinding
     *
     */
    private fun initBinding() {
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        binding.lifecycleOwner = this
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
        initDrawerSwipeLayout()
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
                    binding.fragmentIndex = position
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
        tabLayout.getTabAt(0)?.text = ""
        tabLayout.getTabAt(1)?.setCustomView(R.layout.design_fragment_icon_chat)
        tabLayout.getTabAt(1)?.text = ""
        tabLayout.getTabAt(2)?.setCustomView(R.layout.design_fragment_icon_interest)
        tabLayout.getTabAt(2)?.text = ""
        tabLayout.getTabAt(3)?.setText(R.string.tab_debug)
    }

    private fun initDrawerLayout() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        // ユーザ一覧更新
        otherUsersView.customAdapter.refresh(UserManager.myFriends)
    }

    private fun initDrawerSwipeLayout(){
        val swipeRefreshLayout = drawerMenuView.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            // ユーザ一覧更新
            otherUsersView.customAdapter.refresh(UserManager.myFriends)
        }
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
