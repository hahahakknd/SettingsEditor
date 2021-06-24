package kkj.settingseditor.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kkj.settingseditor.R

class PlaceholderFragment(private val cardViewAdapter: CardViewAdapter) : Fragment() {
    companion object {
        private const val TAG = "SettingsEditor.PlaceholderFragment"
    }

    private lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cardViewAdapter.refreshItems()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) : View? {
        val root = inflater.inflate(R.layout.fragment_main,container,false)
        mRecyclerView = root.findViewById(R.id.recycler_view) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(root.context)
        mRecyclerView.adapter = cardViewAdapter
        return root
    }
}