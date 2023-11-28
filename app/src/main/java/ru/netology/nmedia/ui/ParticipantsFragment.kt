package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnUserItemInteractionListener
import ru.netology.nmedia.adapter.UserItemAdapter
import ru.netology.nmedia.databinding.FragmentUsersItemsBinding
import ru.netology.nmedia.dto.UserItem
import ru.netology.nmedia.utils.LongArg
import ru.netology.nmedia.viewmodel.ParticipantsViewModel

@AndroidEntryPoint
class ParticipantsFragment : Fragment() {
    val viewModel: ParticipantsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUsersItemsBinding.inflate(inflater, container, false)

        binding.topAppBar.title = getString(R.string.participants)

        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.hide()

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = UserItemAdapter(object : OnUserItemInteractionListener {
            override fun onViewUser(user: UserItem) {
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
        activity.supportActionBar?.show()
    }

    companion object {
        var Bundle.idArg: Long? by LongArg
    }
}
