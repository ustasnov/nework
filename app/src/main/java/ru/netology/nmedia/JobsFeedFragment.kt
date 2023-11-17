package ru.netology.nmedia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.ProfileFragment.Companion.idArg
import ru.netology.nmedia.ProfileFragment.Companion.type
import ru.netology.nmedia.adapter.JobsAdapter
import ru.netology.nmedia.adapter.OnJobsInteractionListener
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UsersAdapter
import ru.netology.nmedia.databinding.FragmentJobsFeedBinding
import ru.netology.nmedia.databinding.FragmentUsersBinding
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.JobViewModel
import ru.netology.nmedia.viewmodel.UserViewModel

@AndroidEntryPoint
class JobsFeedFragment : Fragment() {
    val viewModel: JobViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentJobsFeedBinding.inflate(inflater, container, false)

        val adapter = JobsAdapter(object : OnJobsInteractionListener {
            override fun onViewJob(job: Job) {
                //viewModel.viewById(user.id)
                /*
                findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    Bundle().apply {
                        idArg = post.id
                    }
                )
                 */
            }
            //}, observer)
        })

        binding.list.adapter = adapter

        val ownerId = requireArguments().idArg
        val type = requireArguments().type

        /*
        if (requireArguments().type === "MY") {
            viewModel.loadMyJobs()
        } else {
            viewModel.loadUserJobs(requireArguments().idArg!!)
        }
        */

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.jobs)
        }


        viewModel.clearJobs()
        if (type === "MYWALL") {
            viewModel.loadMyJobs()
        } else {
            viewModel.loadUserJobs(ownerId!!)
        }

        //loadJobs(viewModel)

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.swiperefresh.isRefreshing = state.refreshing
            when (state.error) {
                ErrorType.LOADING ->
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModel.loadUserJobs(requireArguments().idArg!!) }
                        .show()
                else -> Unit
            }
        }

        val swipeRefresh = binding.swiperefresh
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            if (type === "MYWALL") {
                viewModel.refreshMyJobs()
            } else {
                viewModel.refreshUserJobs(ownerId!!)
            }
            swipeRefresh.isRefreshing = false
        }

        return binding.root
    }

    /*
    fun loadJobs(viewModel: JobViewModel) {
        if (requireArguments().type === "MY") {
            viewModel.loadMyJobs()
        } else {
            println("From JobsFeedFragment.loadJobs(): ${requireArguments().idArg}")
            viewModel.loadUserJobs(requireArguments().idArg!!)
        }
    }

     */

    companion object {
        var Bundle.idArg: Long? by LongArg
        var Bundle.type: String? by StringArg

        @JvmStatic
        fun newInstance() = JobsFeedFragment()
    }
}