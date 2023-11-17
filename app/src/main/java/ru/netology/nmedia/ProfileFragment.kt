package ru.netology.nmedia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UserViewHolder
import ru.netology.nmedia.adapter.WallAdapter
import ru.netology.nmedia.databinding.FragmentProfileBinding
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.WallItem
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.JobViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import ru.netology.nmedia.viewmodel.WallViewModel

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    val userViewModel: UserViewModel by activityViewModels()
    val jobViewModel: JobViewModel by activityViewModels()
    val postViewModel: WallViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    private val fragTitles = listOf(
        "Места работы",
        "Сообщения",
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val userId = requireArguments().idArg
        val type = requireArguments().type

        val binding = FragmentProfileBinding.inflate(inflater, container, false)
        //val title = type == "MYWALL" ? getString(R.string.my_profile) : getString(R.string.user_profile)
        requireActivity().setTitle(if (type == "MYWALL") {
            getString(R.string.my_profile)
        } else {
            getString(R.string.user_profile)
        })

        val viewHolder = UserViewHolder(binding.userFr, object : OnUsersInteractionListener {
            override fun onViewUser(user: User) {

            }
        })

        userViewModel.currentUser.observe(viewLifecycleOwner) {
            viewHolder.bind(it)
        }

        postViewModel.setWallItem(userId!!, type!!)

        //println("From profile fragment: ${userViewModel.currentUserId.value}, ${userViewModel.currentUser.value}")

        val jobsFeedFragment: JobsFeedFragment = JobsFeedFragment.newInstance()
        jobsFeedFragment.arguments = Bundle().apply {
            /*
            arguments = bundleOf(
                "idArg" to userId,
                "type" to "WALL"
            )
            */
            putLong("idArg", userId)
            putString("type", "WALL")
        }

        val wallFragment: WallFragment = WallFragment.newInstance()
        /*
        wallFragment.arguments = Bundle().apply {
            /*
            arguments = bundleOf(
                "idArg" to userId,
                "type" to "WALL"
            )
            */
            putLong("idArg", userId!!)
            putString("type", "WALL")
        }
         */

        postViewModel.setData(PostsSource(userId, SourceType.WALL))

        val fragList = listOf(
            jobsFeedFragment,
            wallFragment
        )

        jobViewModel.refreshUserJobs(userId)

        postViewModel.setWallItem(userId, "WALL")
        //postViewModel.loadPosts()
        //println("From ProfileFragment: userId = ${userId}")
        //jobViewModel.refreshUserJobs(userId)

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

    override fun onStop() {
        super.onStop()
        //println("From ProfileFragment.onStop.clearJobs()")
        jobViewModel.clearJobs()
        postViewModel.clearPosts()
    }

    companion object {
        var Bundle.idArg: Long? by LongArg
        var Bundle.type: String? by StringArg
    }
}