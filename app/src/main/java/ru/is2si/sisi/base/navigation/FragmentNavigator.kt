package ru.is2si.sisi.base.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentNavigator(
        private val fragmentManager: FragmentManager,
        @IdRes private val containerId: Int
) : Navigator {

    override fun fragmentReplace(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .commit()
    }

    override fun fragmentAdd(fragment: Fragment) {
        val currentFragment = fragmentManager.fragments
                .first { it.isVisible }
        fragmentManager.beginTransaction()
                .hide(currentFragment)
                .add(containerId, fragment)
                .addToBackStack(null)
                .commit()
    }

}