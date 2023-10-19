package ru.netology.nmedia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.toList
import ru.netology.nmedia.adapter.MentionsAdapter
import ru.netology.nmedia.adapter.OnMentionsInteractionListener
import ru.netology.nmedia.adapter.OnUsersInteractionListener
import ru.netology.nmedia.adapter.UsersAdapter
import ru.netology.nmedia.databinding.FragmentUsersBinding
import ru.netology.nmedia.dto.ErrorType
import ru.netology.nmedia.dto.User
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.MentionViewModel
import ru.netology.nmedia.viewmodel.UserViewModel

@AndroidEntryPoint
class MentionsFragment : Fragment() {
    val viewModel: MentionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUsersBinding.inflate(inflater, container, false)

        requireActivity().title = getString(R.string.mentors)

        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_24)
        //activity.supportActionBar?.hide()

        val adapter = MentionsAdapter(object : OnMentionsInteractionListener {
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


        binding.list.adapter = adapter

        viewModel.setData(requireArguments().idArg!!)

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.users)
        }

        val swipeRefresh = binding.swiperefresh
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            viewModel.setData(requireArguments().idArg!!)
            swipeRefresh.isRefreshing = false
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_24)
    }

    companion object {
        var Bundle.idArg: Long? by LongArg
    }
}