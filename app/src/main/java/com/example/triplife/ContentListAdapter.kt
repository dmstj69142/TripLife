package com.example.triplife

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.triplife.model.ContentDTO

class ContentListAdapter(
    private val onCommentClick: (String) -> Unit,
    private val onDeleteContent: (ContentDTO) -> Unit
) :
    ListAdapter<ContentDTO, ContentListViewHolder>(object : DiffUtil.ItemCallback<ContentDTO>() {
        override fun areItemsTheSame(oldItem: ContentDTO, newItem: ContentDTO): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: ContentDTO, newItem: ContentDTO): Boolean {
            return oldItem == newItem
        }
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentListViewHolder {
        return ContentListViewHolder(parent, onCommentClick, onDeleteContent)
    }

    override fun onBindViewHolder(holder: ContentListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}