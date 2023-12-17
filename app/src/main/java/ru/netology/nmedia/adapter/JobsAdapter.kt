package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardJobBinding
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.utils.AndroidUtils.formatDate

interface OnJobsInteractionListener {
    fun onEdit(job: Job) {}
    fun onRemove(job: Job) {}
}

class JobsAdapter(
    private val onJobsInteractionListener: OnJobsInteractionListener,
    private val editEnabled: Boolean
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onJobsInteractionListener, editEnabled)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onJobsInteractionListener: OnJobsInteractionListener,
    private val editEnabled: Boolean
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        binding.apply {
            company.text = job.name
            position.text = job.position
            startDate.text = formatDate(job.start, "dd MMM yyyy")
            if (job.finish.isNullOrBlank()) {
                finishTitle.visibility = View.GONE
                finishDate.visibility = View.GONE
            } else {
                finishDate.text = formatDate(job.finish, "dd MMM yyyy")
                finishTitle.visibility = View.VISIBLE
                finishDate.visibility = View.VISIBLE
            }
            if (job.link.isNullOrBlank()) {
                linkTitle.visibility = View.GONE
                link.visibility = View.GONE
            } else {
                link.text = job.link
            }

            jobMenu.visibility = if (editEnabled) View.VISIBLE else View.GONE
            jobMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onJobsInteractionListener.onRemove(job)
                                true
                            }

                            R.id.edit -> {
                                onJobsInteractionListener.onEdit(job)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}

