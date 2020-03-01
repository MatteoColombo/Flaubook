package it.speedcubing.flaubook.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.speedcubing.flaubook.R

class BSFragment : BottomSheetDialogFragment(), CLFragment.ChapterSelected {

    private lateinit var pager: ViewPager2
    private lateinit var tab: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        CLFragment.initialize(this)
        pager = view.findViewById(R.id.bs_pager)
        pager.adapter = VPAdapter(this)
        tab = view.findViewById(R.id.bs_tab)
        TabLayoutMediator(tab, pager) { _, _ -> run {} }.attach()
        return view
    }

    private inner class VPAdapter(
        fa: BottomSheetDialogFragment
    ) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> BookFragment()
                else -> CLFragment()
            }
        }

    }

    override fun chapterSelected() {
        pager.currentItem = 0
    }
}