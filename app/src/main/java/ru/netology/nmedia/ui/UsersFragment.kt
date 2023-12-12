package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UsersAdapter
import ru.netology.nmedia.databinding.FragmentUsersBinding
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.model.UserItemModel
import ru.netology.nmedia.model.UsersSelectModel
import ru.netology.nmedia.repository.PostsSource
import ru.netology.nmedia.repository.SourceType
import ru.netology.nmedia.viewmodel.ProfileViewModel
import ru.netology.nmedia.viewmodel.UserViewModel
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

        binding.list.adapter = adapter

        viewModel.forSelection.observe(viewLifecycleOwner) {
            //requireActivity().title = it.title
            enableSelection = it.choice

            if (enableSelection) {
                setAppTopBar(binding, it)

                if (it.type === "Users") {
                    binding.topAppBar.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.save -> {
                                viewModel.setCheckList(viewModel.data.value!!.users.filter {
                                    user -> user.checked
                                })
                                findNavController().navigateUp()
                                true
                            }

                            else -> false
                        }
                    }
                }
            } else {
                if (it.type === "Users") {
                    requireActivity().title = it.title
                    binding.topAppBar.visibility = View.GONE
                    val params: CoordinatorLayout.LayoutParams = CoordinatorLayout.LayoutParams(
                        CoordinatorLayout.LayoutParams.MATCH_PARENT,
                        CoordinatorLayout.LayoutParams.MATCH_PARENT
                    )
                    params.setMargins(0, 0, 0, 0)
                    binding.listContainer.layoutParams = params
                }
            }

            when (it.type) {
                "Mentions" -> setObserverForDataType(binding, adapter, viewModel.mentionsData, it)
                "LikeOwners" -> setObserverForDataType(binding, adapter, viewModel.likeOwnersData, it)
                "EventLikeOwners" -> setObserverForDataType(binding, adapter, viewModel.eventLikeOwnersData, it)
                "Participants" -> setObserverForDataType(binding, adapter, viewModel.participantsData, it)
                "Speakers" -> setObserverForDataType(binding, adapter, viewModel.speakersData, it)
                else -> {
                    viewModel.data.observe(viewLifecycleOwner) { userModel ->
                        adapter.submitList(filterList(userModel.users, filterQuery ?: ""))
                    }
                }
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterQuery = newText
                adapter.submitList(filterList(viewModel.data.value?.users, filterQuery ?: ""))
                return true
            }
        })

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

    private fun setAppTopBar(binding: FragmentUsersBinding, usersSelectModel: UsersSelectModel, hideSearchField: Boolean = false) {
        binding.topAppBar.visibility = View.VISIBLE
        binding.topAppBar.title = usersSelectModel.title
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.hide()

        if (hideSearchField) {
            binding.searchMaterialCardView.visibility = View.GONE
            binding.topAppBar.menu.getItem(0).isVisible = false
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setObserverForDataType(binding: FragmentUsersBinding,
                                       adapter: UsersAdapter,
                                       viewModelData: LiveData<UserItemModel>,
                                       usersSelectModel: UsersSelectModel) {
        setAppTopBar(binding, usersSelectModel, true)
        viewModelData.observe(viewLifecycleOwner) { userItemModel ->
            val usersList = userItemModel.users.map { userItem ->
                User(userItem.id, "", userItem.name, userItem.avatar)
            }
            adapter.submitList(filterList(usersList, ""))
        }
    }

    private fun filterList(userList: List<User>?, query: String?): List<User> {
        if (query != null && userList != null) {
            val filteredList = ArrayList<User>()

            for (i in userList) {
                if (i.name.lowercase(Locale.ROOT).contains(query)) {
                    filteredList.add(i)
                }
            }
            if (filteredList.isNotEmpty()) {
                return filteredList.toList()
            }
            return emptyList()
        }
        return userList ?: emptyList()
    }

    override fun onStop() {
        super.onStop()
        viewModel.clearAllChecks()
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.show()
    }
}