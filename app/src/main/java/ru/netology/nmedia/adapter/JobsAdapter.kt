package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.CardJobBinding
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.utils.AndroidUtils.formatDate

interface OnJobsInteractionListener {
    fun onViewJob(job: Job) {}
}

class JobsAdapter(
    private val onJobsInteractionListener: OnJobsInteractionListener
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onJobsInteractionListener)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }

}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onJobsInteractionListener: OnJobsInteractionListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(job: Job) {
        binding.apply {
            company.text = job.name
            position.text = job.position
            startDate.text = formatDate(job.start, "dd MMM yyyy")
            if (job.finish.isNullOrBlank()) {
                endDateLabel.visibility = View.GONE
                endDate.visibility = View.GONE
            } else {
                endDate.text = formatDate(job.finish, "dd MMM yyyy")
                endDateLabel.visibility = View.VISIBLE
                endDate.visibility = View.VISIBLE
            }
            if (job.link.isNullOrBlank()) {
                linkLabel.visibility = View.GONE
                link.visibility = View.GONE
            } else {
                link.text = job.link
            }

            root.setOnClickListener {
                onJobsInteractionListener.onViewJob(job)
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

