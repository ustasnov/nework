package ru.netology.nmedia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.JobsFeedFragment.Companion.idArg
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UserViewHolder
import ru.netology.nmedia.adapter.WallAdapter
import ru.netology.nmedia.databinding.FragmentProfileBinding
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.JobViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.UserViewModel

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    val userViewModel: UserViewModel by activityViewModels()
    val jobViewModel: JobViewModel by activityViewModels()
    val postViewModel: PostViewModel by activityViewModels()
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
        /*
        val fragList = listOf(
            JobsFeedFragment.newInstance().apply {
                arguments = bundleOf(
                    "idArg" to userId,
                    "type" to  "WALL")
                //loadJobs()
            },
            FeedFragment.newInstance().apply {
                arguments = bundleOf(
                    "idArg" to userId,
                    "type" to  "WALL")
            }
        )
        */
        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        requireActivity().setTitle(getString(R.string.user_profile))

        val viewHolder = UserViewHolder(binding.userFr, object : OnUsersInteractionListener {
            override fun onViewUser(user: User) {

            }
        })

        viewHolder.bind(userViewModel.currentUser.value!!)

        val jobsFeedFragment: JobsFeedFragment = JobsFeedFragment.newInstance()
        jobsFeedFragment.arguments = Bundle().apply {
            /*
            arguments = bundleOf(
                "idArg" to userId,
                "type" to "WALL"
            )
             */
            putLong("idArg", userId!!)
            putString("type", "WALL")
        }

        val feedFragment: FeedFragment = FeedFragment.newInstance()
        feedFragment.arguments = Bundle().apply {
            /*
            arguments = bundleOf(
                "idArg" to userId,
                "type" to "WALL"
            )
            */
            putLong("idArg", userId!!)
            putString("type", "WALL")
        }

        //postViewModel.setData(PostsSource(userId!!, SourceType.WALL))

        val fragList = listOf(
            jobsFeedFragment,
            feedFragment
        )

        //jobViewModel.refreshUserJobs(userId)

        postViewModel.loadPosts()
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
        println("From ProfileFragment.onStop.clearJobs()")
        jobViewModel.clearJobs()
        postViewModel.clearPosts()
    }

    companion object {
        var Bundle.idArg: Long? by LongArg
        var Bundle.type: String? by StringArg
    }
}