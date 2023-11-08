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
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UserViewHolder
import ru.netology.nmedia.adapter.WallAdapter
import ru.netology.nmedia.databinding.FragmentProfileBinding
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.UserViewModel

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    val userViewModel: UserViewModel by activityViewModels()
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

        val jobsFeedFragment: JobsFeedFragment = JobsFeedFragment.newInstance().apply {
            arguments = bundleOf(
                "idArg" to userId,
                "type" to "WALL"
            )
        }
        val feedFragment: FeedFragment = FeedFragment.newInstance().apply {
            arguments = bundleOf(
                "idArg" to userId,
                "type" to "WALL"
            )
        }

        //jobsFeedFragment.loadJobs(jobsFeedFragment.viewModel)

        val fragList = listOf(
            jobsFeedFragment,
            feedFragment
        )

        val binding = FragmentProfileBinding.inflate(inflater, container, false)

        val viewHolder = UserViewHolder(binding.userFr, object : OnUsersInteractionListener {
            override fun onViewUser(user: User) {

            }
        })

        //val postId = requireArguments().idArg
        viewHolder.bind(userViewModel.currentUser.value!!)


        val adapter = WallAdapter(requireActivity(), fragList)
        //val adapter = WallAdapter(requireActivity())
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) {
            tab, pos -> tab.text = fragTitles[pos]
        }.attach()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }

        //(fragList[0] as JobsFeedFragment).loadJobs()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            callback
        )

        return binding.root
    }

    companion object {
        var Bundle.idArg: Long? by LongArg
        var Bundle.type: String? by StringArg
    }
}