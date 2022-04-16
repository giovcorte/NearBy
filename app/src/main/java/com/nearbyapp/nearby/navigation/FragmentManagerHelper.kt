package com.nearbyapp.nearby.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.nearbyapp.nearby.fragment.FragmentFactory

class FragmentManagerHelper(private var host: Int, private var fragmentManager: FragmentManager):
    IFragmentManager {

    private val ARRAY_STATE_KEY = "array-state-key"
    private val FIRST_FRAGMENT_TAG = "first-fragment-tag"
    private val OVER_FRAGMENT_TAG = "single-fragment-tag"

    companion object {
        private var fragmentTagStack: ArrayList<String> = ArrayList()
    }

    private var fragmentListeners: ArrayList<IFragmentManager.IFragmentStateListener> = ArrayList()

    override fun pushFirstFragment(name: String) {
        if (fragmentTagStack.isEmpty()) {
            val fragment = FragmentFactory.createFragment(name)
            fragmentTagStack.add(FIRST_FRAGMENT_TAG)
            fragmentManager.beginTransaction()
                .replace(host, fragment, FIRST_FRAGMENT_TAG)
                .addToBackStack(FIRST_FRAGMENT_TAG)
                .commit()
        }
    }

    override fun pushFragment(name: String) {
        if (!isCurrentFragmentOver()) {
            val fragment: Fragment = FragmentFactory.createFragment(name)
            val tag = getTagForFragment(name)
            fragmentTagStack.add(tag)
            fragmentManager.beginTransaction()
                .replace(host, fragment, tag)
                .addToBackStack(tag)
                .commit()
        }
    }

    override fun pushOverFragment(name: String) {
        if (!isCurrentFragmentOver()) {
            val fragment: Fragment = FragmentFactory.createFragment(name)
            fragmentTagStack.add(OVER_FRAGMENT_TAG)
            fragmentManager.beginTransaction()
                .add(host, fragment, OVER_FRAGMENT_TAG)
                .addToBackStack(OVER_FRAGMENT_TAG)
                .commit()
        }
    }

    override fun popOverFragment() {
        if (isCurrentFragmentOver()) {
            fragmentTagStack.removeAt(fragmentTagStack.size - 1)
            fragmentManager.popBackStack()
        }
    }

    override fun popLastFragment() {
        if (canPopBackStack()) {
            if (isCurrentFragmentOver()) {
                fragmentTagStack.removeAt(fragmentTagStack.size - 1)

                val fragmentTag = fragmentTagStack[fragmentTagStack.size - 1]
                fragmentTagStack.removeAt(fragmentTagStack.size - 1)

                fragmentManager.popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } else {
                fragmentTagStack.removeAt(fragmentTagStack.size - 1)
                fragmentManager.popBackStack()
            }
        }
    }

    override fun popToName(name: String) {
        if (canPopBackStack()) {
            var fragmentTag: String? = null
            var fragmentEntryIndex = fragmentManager.backStackEntryCount - 1
            var fragmentAreaTagsIndex = fragmentTagStack.size - 1
            for (i in fragmentTagStack.indices.reversed()) {
                val currentFragmentName = fragmentTagStack[i].split(":").toTypedArray()[0]
                if (currentFragmentName != name) {
                    fragmentEntryIndex--
                    fragmentAreaTagsIndex--
                } else {
                    fragmentEntryIndex++
                    if (fragmentEntryIndex < fragmentTagStack.size) {
                        fragmentTag = fragmentTagStack[fragmentEntryIndex]
                    }
                    break
                }
            }
            if (fragmentTag != null) {
                if (fragmentTagStack.size > fragmentAreaTagsIndex + 1) {
                    fragmentTagStack.subList(fragmentAreaTagsIndex + 1, fragmentTagStack.size)
                        .clear()
                }
                fragmentManager.popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
    }

    override fun popToTag(tag: String) {
        if (canPopBackStack()) {
            var fragmentTag: String? = null
            var fragmentEntryIndex = fragmentManager.backStackEntryCount - 1
            var fragmentAreaTagsIndex = fragmentTagStack.size - 1
            for (i in fragmentTagStack.indices.reversed()) {
                if (tag != fragmentTagStack[i]) {
                    fragmentEntryIndex--
                    fragmentAreaTagsIndex--
                } else {
                    fragmentEntryIndex++
                    if (fragmentEntryIndex < fragmentTagStack.size) {
                        fragmentTag = fragmentTagStack[fragmentEntryIndex]
                    }
                    break
                }
            }
            if (fragmentTag != null) {
                if (fragmentTagStack.size > fragmentAreaTagsIndex + 1) {
                    fragmentTagStack.subList(fragmentAreaTagsIndex + 1, fragmentTagStack.size)
                        .clear()
                }
                fragmentManager.popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        }
    }

    override fun popToFirstFragment() {
        if (canPopBackStack()) {
            popToTag(FIRST_FRAGMENT_TAG)
        }
    }

    override fun isCurrentFragmentFirst(): Boolean {
        return fragmentTagStack[fragmentTagStack.size - 1] == FIRST_FRAGMENT_TAG
    }

    override fun getCurrentFragmentName(): String {
        return getFragmentName(getLastTag(false))
    }

    private fun isCurrentFragmentOver(): Boolean {
        return fragmentTagStack.contains(OVER_FRAGMENT_TAG)
    }

    private fun canPopBackStack(): Boolean {
        return fragmentTagStack.size > 1
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putStringArrayList(ARRAY_STATE_KEY, fragmentTagStack)
    }

    override fun onRestoreInstanceState(inState: Bundle?) {
        if (inState != null && inState.containsKey(ARRAY_STATE_KEY)) {
            fragmentTagStack = inState.getStringArrayList(ARRAY_STATE_KEY)!!
        }
    }

    override fun subscribeFragmentStateListener(listener: IFragmentManager.IFragmentStateListener) {
        fragmentListeners.add(listener)
    }

    private fun getTagForFragment(fragment: String): String {
        return fragment + ":" + System.currentTimeMillis()
    }

    private fun getFragmentName(tag: String): String {
        return if (tag.contains(":")) {
            tag.split(":").toTypedArray()[0]
        } else tag
    }

    private fun getLastTag(isOverFragment: Boolean): String {
        return if (!isOverFragment) {
            if (fragmentTagStack.size > 1) fragmentTagStack[fragmentTagStack.size - 1] else FIRST_FRAGMENT_TAG
        } else {
            if (fragmentTagStack.size > 2) fragmentTagStack[fragmentTagStack.size - 2] else FIRST_FRAGMENT_TAG
        }
    }


}