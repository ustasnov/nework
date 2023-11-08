package ru.netology.nmedia.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.netology.nmedia.FeedFragment
import ru.netology.nmedia.JobsFeedFragment

class WallAdapter(fa: FragmentActivity, private val list: List<Fragment>): FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}