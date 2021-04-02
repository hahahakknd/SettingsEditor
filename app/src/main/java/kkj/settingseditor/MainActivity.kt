package kkj.settingseditor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kkj.settingseditor.ui.main.SectionsPagerAdapter

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG: String = "SettingsEditor.MainActivity"
    }

    private lateinit var mMySettingsData: MySettingsData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        mMySettingsData = MySettingsData(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.getItem(0)?.isChecked = mMySettingsData.serviceAutoRun
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.auto_run -> mMySettingsData.serviceAutoRun = !item.isChecked
            R.id.run -> startForegroundService(Intent(this, SettingsMonitoringService::class.java))
            R.id.stop -> stopService(Intent(this, SettingsMonitoringService::class.java))
        }
        return true
    }
}