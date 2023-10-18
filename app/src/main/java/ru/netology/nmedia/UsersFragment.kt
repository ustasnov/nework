package ru.netology.nmedia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.PostFragment.Companion.idArg
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UsersAdapter
import ru.netology.nmedia.databinding.FragmentUsersBinding
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.entity.UserItem
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.UserViewModel

@AndroidEntryPoint
class UsersFragment : Fragment() {
    val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        requireActivity().title = getString(R.string.users)

        val adapter = UsersAdapter(object : OnUsersInteractionListener {
            override fun onViewUser(user: UserItem) {
                viewModel.viewById(user.id)
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

        val binding = FragmentUsersBinding.inflate(inflater, container, false)
        binding.list.adapter = adapter

        viewModel.setDataByType(requireArguments().listType, requireArguments().idArg)

        when (requireArguments().listType) {
            "all" -> { viewModel.loadUsers() }
            "mentions" -> { viewModel.data = viewModel.loadMentors(requireArguments().idArg) }
            else -> { viewModel.data = viewModel.loadLikeOwners(requireArguments().idArg) }
        }

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.users)
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.swiperefresh.isRefreshing = state.refreshing
            when (state.error) {
                ErrorType.LOADING ->
                    Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry_loading) { viewModel.loadUsers() }
                        .show()
                else -> Unit
            }
        }

        val swipeRefresh = binding.swiperefresh
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            viewModel.refresh()
            swipeRefresh.isRefreshing = false
        }

        return binding.root
    }

    companion object {
        var Bundle.idArg: Long? by LongArg
        var Bundle.listType: String? by StringArg
    }
}