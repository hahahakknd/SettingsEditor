package kkj.settingseditor.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kkj.settingseditor.R

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {
    companion object {
        private const val TAG = "SettingsEditor.PlaceholderFragment"

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            Log.d(TAG, "newInstance, sectionNumber:$sectionNumber")
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    private lateinit var mPageViewModel: PageViewModel
    private lateinit var mRecyclerView: RecyclerView
    private var mPageNumber  = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPageNumber = arguments?.getInt(ARG_SECTION_NUMBER) ?: 0
        mPageViewModel = ViewModelProvider(this).get(PageViewModel::class.java)
        loadData()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main,container,false)

        mRecyclerView = root.findViewById(R.id.recycler_view) as RecyclerView
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(root.context)

        val adapter = CardViewAdapter()
        adapter.setPageNumber(mPageNumber)
        mRecyclerView.adapter = adapter

        mPageViewModel.text.observe(this, {
            adapter.addItems(it)
        })
        return root
    }

    fun loadData() {
        mPageViewModel.setIndex(mPageNumber)
    }
}