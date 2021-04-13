package com.support.widgets

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class ViewPagerAdapter(fm: FragmentManager) :
    FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments: MutableList<Fragment> = mutableListOf()
    private val fragmentNames: MutableList<String> = mutableListOf()

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragmentNames[position]
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment: Fragment = super.instantiateItem(container, position) as Fragment
        fragments[position] = fragment
        return fragment
    }

    fun addFragment(fragment: Fragment, name: String){
        fragments.add(fragment)
        fragmentNames.add(name)
    }

    fun setFragments(mList: List<Fragment>) {
        fragments.clear()
        fragments.addAll(mList)
        notifyDataSetChanged()
    }
}