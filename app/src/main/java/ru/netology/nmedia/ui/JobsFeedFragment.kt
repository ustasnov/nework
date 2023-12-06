package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.JobsAdapter
import ru.netology.nmedia.adapter.OnJobsInteractionListener
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentJobsFeedBinding
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.JobViewModel
import ru.netology.nmedia.viewmodel.WallViewModel
import ru.netology.nmedia.viewmodel.empty
import ru.netology.nmedia.viewmodel.emptyJob
import javax.inject.Inject

@AndroidEntryPoint
class JobsFeedFragment : Fragment() {
    val viewModel: JobViewModel by activityViewModels()
    val postViewModel: WallViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        var editEnabled: Boolean

        val binding = FragmentJobsFeedBinding.inflate(inflater, container, false)
        val swipeRefresh = binding.swiperefresh

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            swipeRefresh.isRefreshing = state.refreshing
            when (state.error) {
                ErrorType.LOADING ->
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModel.loadUserJobs(postViewModel.postSource.value!!.authorId!!) }
                        .show()
                else -> Unit
            }
        }

        editEnabled = viewModel.postSource.value!!.sourceType == SourceType.MYWALL
        //val ownerId = requireArguments().idArg
        //val type = requireArguments().type
        viewModel.postSource.observe(viewLifecycleOwner) {
            if (it.authorId != null && it.authorId != 0L) {
                //viewModel.clearJobs()
                if (it.sourceType === SourceType.MYWALL) {
                    viewModel.loadMyJobs()
                    binding.add.visibility = View.VISIBLE
                    editEnabled = true
                } else {
                    viewModel.loadUserJobs(it.authorId)
                    binding.add.visibility = View.GONE
                    editEnabled = false
                }
                val adapter = JobsAdapter(object : OnJobsInteractionListener {
                    override fun onRemove(job: Job) {
                        viewModel.removeMyJob(job)
                    }

                    override fun onEdit(job: Job) {
                        viewModel.edit(job)
                        findNavController().navigate(R.id.action_profileFragment_to_jobFragment)
                    }
                }, editEnabled)

                binding.list.adapter = adapter

                viewModel.data.observe(viewLifecycleOwner) { state ->
                    adapter.submitList(state.jobs)
                }
            }
        }

        /*
        val adapter = JobsAdapter(object : OnJobsInteractionListener {
            override fun onRemove(job: Job) {
                viewModel.removeMyJob(job)
            }

            override fun onEdit(job: Job) {
                viewModel.edit(job)
                findNavController().navigate(R.id.action_profileFragment_to_jobFragment)
            }
        }, editEnabled)

         */

        //binding.list.adapter = adapter

        //viewModel.data.observe(viewLifecycleOwner) { state ->
        //    adapter.submitList(state.jobs)
        //}

        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            if (postViewModel.postSource.value!!.sourceType == SourceType.MYWALL) {
                viewModel.refreshMyJobs()
            } else {
                viewModel.refreshUserJobs(postViewModel.postSource.value!!.authorId!!)
            }
            swipeRefresh.isRefreshing = false
        }

        binding.add.setOnClickListener {
            if (authViewModel.isAuthorized) {
                viewModel.edit(emptyJob)
                findNavController().navigate(R.id.action_profileFragment_to_jobFragment)
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.authorization_required),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.login) { findNavController().navigate(R.id.authFragment) }
                    .show()
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = JobsFeedFragment()
    }
}