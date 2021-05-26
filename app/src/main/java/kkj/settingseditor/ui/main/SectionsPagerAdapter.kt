package kkj.settingseditor.ui.main

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kkj.settingseditor.R

private val TAB_TITLES = arrayOf(
        R.string.tab_favorite,
        R.string.tab_global,
        R.string.tab_system,
        R.string.tab_secure
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {
    companion object {
        private const val TAG = "SettingsEditor.SectionsPagerAdapter"
    }

    private val mFragmentList: ArrayList<PlaceholderFragment> = ArrayList()

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        mFragmentList.add(position,PlaceholderFragment.newInstance(position))
        return mFragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 4 total pages.
        return 4
    }

    fun loadData(position: Int) {
        mFragmentList[position].loadData()
    }
}