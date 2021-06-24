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
import kkj.settingseditor.data.MyPreferences
import kkj.settingseditor.data.MySettings
import kkj.settingseditor.ui.SectionsPagerAdapter

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG: String = "SettingsEditor.MainActivity"
        private const val PERMISSION_REQUEST_CODE = 1000
    }

    class TabSelectedListener(private val adapter: SectionsPagerAdapter)
            : TabLayout.OnTabSelectedListener {

        private var mSearchBar: MenuItem? = null

        fun setSearchBar(searchBar: MenuItem?) {
            mSearchBar = searchBar
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            val currentPos = tab?.position ?: adapter.count
            if (currentPos < adapter.count) {
                adapter.setCurrentPos(currentPos)
                adapter.refreshItems()
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            mSearchBar?.collapseActionView()
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }
    }

    class QueryTextListener(private val adapter: SectionsPagerAdapter)
            : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            if (newText.isNullOrEmpty()) {
                adapter.searchAllItems()
            } else {
                adapter.searchItems(newText)
            }
            return true
        }
    }

    private lateinit var mView: View
    private lateinit var mSearchBar: MenuItem
    private lateinit var mAdapter: SectionsPagerAdapter
    private lateinit var mTabListener: TabSelectedListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mView = findViewById(R.id.main_activity)
        mAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        mTabListener = TabSelectedListener(mAdapter)

        val tabs = findViewById<TabLayout>(R.id.tabs)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)

        viewPager.adapter = mAdapter
        tabs.setupWithViewPager(viewPager)
        tabs.addOnTabSelectedListener(mTabListener)

//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            MyUtils.showSnackbar(view, "Replace with your own action")
//        }

        setSupportActionBar(findViewById(R.id.toolbar))

        MyPreferences.makeInstance(this)
        MySettings.makeInstance(this)
        MyDatabase.makeInstance(this)
    }

    override fun onResume() {
        super.onResume()
        setWriteSettingsPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        mSearchBar = menu?.findItem(R.id.app_bar_search) ?: return false
        mTabListener.setSearchBar(mSearchBar)

        val searchView = mSearchBar.actionView as SearchView
        searchView.setOnQueryTextListener(QueryTextListener(mAdapter))
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