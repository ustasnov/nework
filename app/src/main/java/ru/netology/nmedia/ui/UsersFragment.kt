package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UsersAdapter
import ru.netology.nmedia.databinding.FragmentUsersBinding
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.ProfileViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
import java.util.ArrayList
import java.util.Locale

@AndroidEntryPoint
class UsersFragment : Fragment() {
    val viewModel: UserViewModel by activityViewModels()
    val profileViewModel: ProfileViewModel by activityViewModels()
    var enableSelection = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var filterQuery: String? = null

        val binding = FragmentUsersBinding.inflate(inflater, container, false)

        viewModel.forSelection.observe(viewLifecycleOwner) {
            requireActivity().title = it.title
            enableSelection = it.choice

            if (enableSelection) {
                binding.topAppBar.title = it.title
                val activity = requireActivity() as AppCompatActivity
                activity.supportActionBar?.hide()

                binding.topAppBar.setNavigationOnClickListener {
                    findNavController().navigateUp()
                }

                binding.topAppBar.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.save -> {
                            viewModel.setCheckList(viewModel.data.value!!.users.filter {
                                it.checked
                            })
                            findNavController().navigateUp()
                            true
                        }
                        else -> false
                    }
                }
            } else {
                binding.topAppBar.visibility = View.GONE
                val params: CoordinatorLayout.LayoutParams = CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT,
                    CoordinatorLayout.LayoutParams.MATCH_PARENT)
                params.setMargins(0, 0,0,0)
                binding.listContainer.layoutParams = params
            }
        }

        val adapter = UsersAdapter(object : OnUsersInteractionListener {
            override fun onViewUser(user: User) {
                if (enableSelection) {
                    viewModel.setChecked(user.id, !user.checked)
                } else {
                    profileViewModel.setPostSource(PostsSource(user.id, SourceType.WALL))
                    findNavController().navigate(
                        R.id.action_usersFragment_to_profileFragment
                    )
                }
            }
        })

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterQuery = newText
                adapter.submitList(filterList(viewModel.data.value!!.users, filterQuery))
                return true
            }
        })

        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(filterList(state.users, filterQuery))
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

        viewModel.loadUsers()

        return binding.root
    }

    private fun filterList(userList: List<User>, query: String?): List<User> {
        if (query != null) {
            val filteredList = ArrayList<User>()

            for (i in userList) {
                if (i.name.lowercase(Locale.ROOT).contains(query)) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isNotEmpty())  {
                return filteredList.toList()
            }
            return emptyList<User>()
        }
        return userList
    }

    override fun onStop() {
        super.onStop()
        viewModel.clearAllChecks()
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.show()
    }
}