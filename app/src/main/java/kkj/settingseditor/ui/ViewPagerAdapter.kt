package kkj.settingseditor.ui

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kkj.settingseditor.R
import kkj.settingseditor.data.MyDataCenter

class ViewPagerAdapter(private val context: Context, fm: FragmentManager)
        : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        private const val TAG = "SettingsEditor.ViewPagerAdapter"
    }

    class FragmentData(val name: Int, val fragment: PlaceholderFragment)

    private val mFragmentList = ArrayList<FragmentData>()

    init {
        val favoriteAdapter = CardViewAdapter(MyDataCenter.SETTINGS_FAVORITE, EditBoxDialogFragment(fm))
        mFragmentList.add(FragmentData(R.string.tab_favorite, PlaceholderFragment(favoriteAdapter)))

        val globalAdapter = CardViewAdapter(MyDataCenter.SETTINGS_GLOBAL, EditBoxDialogFragment(fm))
        mFragmentList.add(FragmentData(R.string.tab_global, PlaceholderFragment(globalAdapter)))

        val systemAdapter = CardViewAdapter(MyDataCenter.SETTINGS_SYSTEM, EditBoxDialogFragment(fm))
        mFragmentList.add(FragmentData(R.string.tab_system, PlaceholderFragment(systemAdapter)))

        val secureAdapter = CardViewAdapter(MyDataCenter.SETTINGS_SECURE, EditBoxDialogFragment(fm))
        mFragmentList.add(FragmentData(R.string.tab_secure, PlaceholderFragment(secureAdapter)))
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
}