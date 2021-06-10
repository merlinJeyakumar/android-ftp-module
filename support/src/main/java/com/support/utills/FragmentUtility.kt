package com.support.utills

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

object FragmentUtility {
    fun AppCompatActivity.replaceFragment(fragment: Fragment, containerId: Int) {
        val tag = fragment.javaClass.name
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(containerId, fragment, tag)
            .addToBackStack(tag).commit()
    }
}