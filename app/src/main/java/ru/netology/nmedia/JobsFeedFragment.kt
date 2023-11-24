package ru.netology.nmedia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.adapter.JobsAdapter
import ru.netology.nmedia.adapter.OnJobsInteractionListener
import ru.netology.nmedia.databinding.FragmentJobsFeedBinding
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.viewmodel.JobViewModel
import ru.netology.nmedia.viewmodel.WallViewModel

@AndroidEntryPoint
class JobsFeedFragment : Fragment() {
    val viewModel: JobViewModel by activityViewModels()
    val postViewModel: WallViewModel by activityViewModels()

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

        //val ownerId = requireArguments().idArg
        //val type = requireArguments().type
        viewModel.postSource.observe(viewLifecycleOwner) {
            if (it.authorId != null && it.authorId != 0L) {
                //viewModel.clearJobs()
                if (it.sourceType === SourceType.MYWALL) {
                    viewModel.loadMyJobs()
                } else {
                    viewModel.loadUserJobs(it.authorId)
                }
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.jobs)
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.swiperefresh.isRefreshing = state.refreshing
            when (state.error) {
                ErrorType.LOADING ->
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModel.loadUserJobs(postViewModel.postSource.value!!.authorId!!) }
                        .show()
                else -> Unit
            }
        }

        val swipeRefresh = binding.swiperefresh
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            if (postViewModel.postSource.value!!.sourceType == SourceType.MYWALL) {
                viewModel.refreshMyJobs()
            } else {
                viewModel.refreshUserJobs(postViewModel.postSource.value!!.authorId!!)
            }
            swipeRefresh.isRefreshing = false
        }

        return binding.root
    }

    companion object {
        //var Bundle.idArg: Long? by LongArg
        //var Bundle.type: String? by StringArg

        @JvmStatic
        fun newInstance() = JobsFeedFragment()
    }
}