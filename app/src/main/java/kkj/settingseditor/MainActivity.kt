package kkj.settingseditor

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.viewpager.widget.ViewPager
//import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import kkj.settingseditor.data.MyDatabase
import kkj.settingseditor.data.MyMemoryDatabase
import kkj.settingseditor.data.MyPreferences
import kkj.settingseditor.data.MySettings
import kkj.settingseditor.ui.SectionsPagerAdapter

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG: String = "SettingsEditor.MainActivity"
        private const val PERMISSION_REQUEST_CODE = 1000
    }

//    class SearchBarExpandListener : MenuItem.OnActionExpandListener {
//        private var mIsExpand = false
//
//        override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
//            Log.d(TAG, "onMenuItemActionExpand")
//            return true
//        }
//
//        override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
//            Log.d(TAG, "onMenuItemActionCollapse")
//            return true
//        }
//    }

    class SearchBarQueryTextListener : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            Log.d(TAG, "onQueryTextSubmit: $query")
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            Log.d(TAG, "onQueryTextChange: $newText")
            return true
        }
    }

    private lateinit var mView: View
    private lateinit var mViewPager: ViewPager
    private lateinit var mSectionsPagerAdapter: SectionsPagerAdapter
    private lateinit var mSearchBar: MenuItem

//    private val mSearchBarExpandListener = SearchBarExpandListener()
    private val mSearchBarQueryTextListener = SearchBarQueryTextListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mView = findViewById(R.id.main_activity)
        mViewPager = findViewById(R.id.view_pager)
        mSectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        mViewPager.adapter = mSectionsPagerAdapter

        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(mViewPager)
        tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val num = tab?.position ?: -1
                Log.d(TAG, "onTabSelected, position:$num")
                mSectionsPagerAdapter.loadData(num)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val num = tab?.position ?: -1
                Log.d(TAG, "onTabUnselected, position:$num")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                val num = tab?.position ?: -1
                Log.d(TAG, "onTabReselected, position:$num")
            }
        })

//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            MyUtils.showSnackbar(view, "Replace with your own action")
//        }

        setSupportActionBar(findViewById(R.id.toolbar))

        MyPreferences.makeInstance(this)
        MySettings.makeInstance(this)
        MyDatabase.makeInstance(this)
        MyMemoryDatabase.makeInstance(this)
    }

    override fun onResume() {
        super.onResume()
        setWriteSettingsPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        mSearchBar = menu?.findItem(R.id.app_bar_search) ?: return false
//        mSearchBar.setOnActionExpandListener(mSearchBarExpandListener)

        val searchView = mSearchBar.actionView as SearchView
        searchView.setOnQueryTextListener(mSearchBarQueryTextListener)
        searchView.maxWidth = Int.MAX_VALUE
        searchView.queryHint = "Find settings name"
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.getItem(1)?.isChecked = MyPreferences.getInstance()?.serviceAutoRun ?: false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.auto_run -> MyPreferences.getInstance()?.serviceAutoRun = !item.isChecked
            R.id.run -> startForegroundService(Intent(this, SettingsMonitoringService::class.java))
            R.id.stop -> stopService(Intent(this, SettingsMonitoringService::class.java))
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (idx in permissions.indices) {
                if (grantResults[idx] == PackageManager.PERMISSION_GRANTED) {
                    MyUtils.showSnackbarWithAutoDismiss(mView, "Permission was granted. ${permissions[idx]}")
                } else {
                    MyUtils.showSnackbarWithAutoDismiss(mView, "Permission was denied. ${permissions[idx]}")
                }
            }
        } else {
            MyUtils.showSnackbarWithAutoDismiss(mView, "Unknown permission request code.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == PERMISSION_REQUEST_CODE) {
            if (Settings.System.canWrite(this)) {
                MyUtils.showSnackbarWithAutoDismiss(mView, "Permission was granted. WRITE_SETTINGS")
            } else {
                MyUtils.showSnackbarWithAutoDismiss(mView, "Permission was denied. WRITE_SETTINGS")
            }
        }
    }

    override fun onBackPressed() {
        if (mSearchBar.isActionViewExpanded) {
            mSearchBar.collapseActionView()
        } else {
            super.onBackPressed()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
    }

    private fun setPermission(permission: String) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (shouldShowRequestPermissionRationale(permission)) {
            MyUtils.showSnackbarWithAction(
                mView,
                "Need to permission($permission)",
                "OK"
            ) {
                requestPermissions(arrayOf(permission), PERMISSION_REQUEST_CODE)
            }
        } else {
            requestPermissions(arrayOf(permission), PERMISSION_REQUEST_CODE)
        }
    }

    private fun setWriteSettingsPermission() {
        if (Settings.System.canWrite(this)) {
            return
        }

        MyUtils.showSnackbarWithAction(mView, "Need to grated WRITE_SETTINGS permission.", "OK") {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:${packageName}")
            startActivityForResult(intent, PERMISSION_REQUEST_CODE)
        }
    }
}