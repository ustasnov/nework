package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UserViewHolder
import ru.netology.nmedia.adapter.WallAdapter
import ru.netology.nmedia.databinding.FragmentProfileBinding
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.JobViewModel
import ru.netology.nmedia.viewmodel.ProfileViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import ru.netology.nmedia.viewmodel.WallViewModel

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    val userViewModel: UserViewModel by activityViewModels()
    val jobViewModel: JobViewModel by activityViewModels()
    val postViewModel: WallViewModel by activityViewModels()
    val profileViewModel: ProfileViewModel  by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        val fragTitles = listOf(
            getString(R.string.jobs),
            getString(R.string.postsTitle)
        )

        val viewHolder = UserViewHolder(binding.userFr, object : OnUsersInteractionListener{})

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            if (it != null && it.id != 0L) {
                val type = profileViewModel.sourceType.value
                viewHolder.bind(it)
                requireActivity().setTitle(
                    if (type == SourceType.MYWALL) {
                        getString(R.string.my_profile)
                    } else {
                        getString(R.string.user_profile)
                    }
                )
                postViewModel.setPostSource(it.id, type!!)
                jobViewModel.setPostSource(it.id, type)
            }
        }

        profileViewModel.postSource.observe(viewLifecycleOwner) {
            if (it.authorId != null && it.authorId != 0L) {
                profileViewModel.setSourceType(it.sourceType!!)
                userViewModel.getUserById(it.authorId)
            }
        }

        val jobsFeedFragment = JobsFeedFragment.newInstance()
        val wallFragment: WallFragment = WallFragment.newInstance()
        val fragList = listOf(jobsFeedFragment, wallFragment)

        val adapter = WallAdapter(requireActivity(), fragList)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = fragTitles[pos]
        }.attach()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )

        return binding.root
    }
}