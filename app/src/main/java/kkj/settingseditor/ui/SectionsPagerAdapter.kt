package kkj.settingseditor.ui

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kkj.settingseditor.R

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {
    companion object {
        private const val TAG = "SettingsEditor.SectionsPagerAdapter"

        private const val FAVORITE_PAGE = 0
        private const val GLOBAL_PAGE = 1
        private const val SYSTEM_PAGE = 2
        private const val SECURE_PAGE = 3
    }

    class FragmentData(val name: Int, val adapter: CardViewAdapter, val fragment: PlaceholderFragment)

    private var mFragmentList: Array<FragmentData>
    private var mCurrentPos = 0

    init {
        val favoriteAdapter = CardViewAdapter(context, FAVORITE_PAGE, fm)
        val globalAdapter = CardViewAdapter(context, GLOBAL_PAGE, fm)
        val systemAdapter = CardViewAdapter(context, SYSTEM_PAGE, fm)
        val secureAdapter = CardViewAdapter(context, SECURE_PAGE, fm)

        mFragmentList = arrayOf(
            FragmentData(R.string.tab_favorite, favoriteAdapter, PlaceholderFragment(favoriteAdapter)),
            FragmentData(R.string.tab_global, globalAdapter, PlaceholderFragment(globalAdapter)),
            FragmentData(R.string.tab_system, systemAdapter, PlaceholderFragment(systemAdapter)),
            FragmentData(R.string.tab_secure, secureAdapter, PlaceholderFragment(secureAdapter))
        )
    }

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position].fragment
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(mFragmentList[position].name)
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun setCurrentPos(currentPos: Int) {
        mCurrentPos = currentPos
    }

    fun refreshItems() {
        mFragmentList[mCurrentPos].adapter.refreshItems()
    }

    fun searchAllItems() {
        mFragmentList[mCurrentPos].adapter.searchAllItems()
    }

    fun searchItems(pattern: String) {
        mFragmentList[mCurrentPos].adapter.searchItems(pattern)
    }
}