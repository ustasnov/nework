package ru.netology.nmedia.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.nmedia.FeedFragment
import ru.netology.nmedia.JobsFeedFragment

class WallAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
    private val fragmentList = listOf<Fragment>(JobsFeedFragment(), FeedFragment())
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}