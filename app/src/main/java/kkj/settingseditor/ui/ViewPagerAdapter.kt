package kkj.settingseditor.ui

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kkj.settingseditor.R
import kkj.settingseditor.data.MyDataCenter
import kkj.settingseditor.data.MyDataCenter.Item

class ViewPagerAdapter(private val context: Context, fm: FragmentManager)
        : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    companion object {
        private const val TAG = "SettingsEditor.ViewPagerAdapter"
    }

    class FragmentData(val name: Int, val fragment: PlaceholderFragment)

    class DataObserver(private val adapter: CardViewAdapter) : MyDataCenter.DataObserver(adapter.dataType) {
        override fun onRefresh() {
            Log.d(TAG, "DataObserver.onRefresh: ${adapter.dataType}")
            MyDataCenter.getInstance()?.searchAllSettings(adapter.dataType)
        }

        override fun onChanged(items: ArrayList<Item>) {
            Log.d(TAG, "DataObserver.onChanged: ${adapter.dataType}")
            adapter.updateItems(items)
        }

        override fun onUpdated(item: Item) {
            Log.d(TAG, "DataObserver.onUpdated: ${adapter.dataType}")
            MyDataCenter.getInstance()?.searchAllSettings(adapter.dataType)
        }

        override fun onAddFavorite(item: Item) {
            Log.d(TAG, "DataObserver.onAddFavorite: ${adapter.dataType}")
            MyDataCenter.getInstance()?.searchAllSettings(adapter.dataType)
        }

        override fun onDeleteFavorite(item: Item) {
            Log.d(TAG, "DataObserver.onDeleteFavorite: ${adapter.dataType}")
            MyDataCenter.getInstance()?.searchAllSettings(adapter.dataType)
        }
    }

    class UIObserver(private val adapter: CardViewAdapter) : MyDataCenter.UIObserver(adapter.dataType) {
        override fun onRefresh() {
            Log.d(TAG, "UIObserver.onRefresh: ${adapter.dataType}")
        }

        override fun onChanged() {
            Log.d(TAG, "UIObserver.onChanged: ${adapter.dataType}")
            adapter.notifyDataSetChanged()
        }

        override fun onUpdated() {
            Log.d(TAG, "UIObserver.onUpdated: ${adapter.dataType}")
        }

        override fun onAddFavorite() {
            Log.d(TAG, "UIObserver.onAddFavorite: ${adapter.dataType}")
        }

        override fun onDeleteFavorite() {
            Log.d(TAG, "UIObserver.onDeleteFavorite: ${adapter.dataType}")
        }
    }

    private val mFragmentList = ArrayList<FragmentData>()

    init {
        val favoriteAdapter = CardViewAdapter(MyDataCenter.SETTINGS_FAVORITE, EditBoxDialogFragment(fm))
        MyDataCenter.getInstance()?.registerObserver(DataObserver(favoriteAdapter))
        MyDataCenter.getInstance()?.registerObserver(UIObserver(favoriteAdapter))
        mFragmentList.add(FragmentData(R.string.tab_favorite, PlaceholderFragment(favoriteAdapter)))

        val globalAdapter = CardViewAdapter(MyDataCenter.SETTINGS_GLOBAL, EditBoxDialogFragment(fm))
        MyDataCenter.getInstance()?.registerObserver(DataObserver(globalAdapter))
        MyDataCenter.getInstance()?.registerObserver(UIObserver(globalAdapter))
        mFragmentList.add(FragmentData(R.string.tab_favorite, PlaceholderFragment(globalAdapter)))

        val systemAdapter = CardViewAdapter(MyDataCenter.SETTINGS_SYSTEM, EditBoxDialogFragment(fm))
        MyDataCenter.getInstance()?.registerObserver(DataObserver(systemAdapter))
        MyDataCenter.getInstance()?.registerObserver(UIObserver(systemAdapter))
        mFragmentList.add(FragmentData(R.string.tab_favorite, PlaceholderFragment(systemAdapter)))

        val secureAdapter = CardViewAdapter(MyDataCenter.SETTINGS_SECURE, EditBoxDialogFragment(fm))
        MyDataCenter.getInstance()?.registerObserver(DataObserver(secureAdapter))
        MyDataCenter.getInstance()?.registerObserver(UIObserver(secureAdapter))
        mFragmentList.add(FragmentData(R.string.tab_favorite, PlaceholderFragment(secureAdapter)))
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