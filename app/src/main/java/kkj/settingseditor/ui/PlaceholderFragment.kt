package kkj.settingseditor.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kkj.settingseditor.R
import kkj.settingseditor.data.MyDataCenter

class PlaceholderFragment(private val adapter: CardViewAdapter) : Fragment() {
    companion object {
        private const val TAG = "SettingsEditor.PlaceholderFragment"
    }

    class DataObserver(private val adapter: CardViewAdapter) : MyDataCenter.DataObserver(adapter.dataType) {
        override fun onRefresh() {
            Log.d(TAG, "DataObserver.onRefresh: ${adapter.dataType}")
            MyDataCenter.getInstance()?.searchAllSettings(adapter.dataType)
        }

        override fun onChanged(items: ArrayList<MyDataCenter.Item>) {
            Log.d(TAG, "DataObserver.onChanged: ${adapter.dataType}")
            adapter.updateItems(items)
        }

        override fun onUpdated(item: MyDataCenter.Item) {
            Log.d(TAG, "DataObserver.onUpdated: ${adapter.dataType}")
            MyDataCenter.getInstance()?.searchAllSettings(adapter.dataType)
        }

        override fun onAddFavorite(item: MyDataCenter.Item) {
            Log.d(TAG, "DataObserver.onAddFavorite: ${adapter.dataType}")
            MyDataCenter.getInstance()?.searchAllSettings(adapter.dataType)
        }

        override fun onDeleteFavorite(item: MyDataCenter.Item) {
            Log.d(TAG, "DataObserver.onDeleteFavorite: ${adapter.dataType}")
            MyDataCenter.getInstance()?.searchAllSettings(adapter.dataType)
        }
    }

    class UIObserver(private val adapter: CardViewAdapter, private val swipeRefreshLayout: SwipeRefreshLayout) : MyDataCenter.UIObserver(adapter.dataType) {
        override fun onRefresh() {
            Log.d(TAG, "UIObserver.onRefresh: ${adapter.dataType}")
            swipeRefreshLayout.isRefreshing = false
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

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) : View? {
        val view = inflater.inflate(R.layout.fragment_main,container,false)
        val recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(view.context)
        recyclerView.adapter = adapter

        val swipeRefreshLayout = view.findViewById(R.id.swipe_layout) as SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            MyDataCenter.getInstance()?.refresh()
        }

        MyDataCenter.getInstance()?.registerObserver(DataObserver(adapter))
        MyDataCenter.getInstance()?.registerObserver(UIObserver(adapter, swipeRefreshLayout))
        return view
    }
}