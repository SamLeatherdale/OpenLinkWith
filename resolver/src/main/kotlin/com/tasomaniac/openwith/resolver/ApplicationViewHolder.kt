package com.tasomaniac.openwith.resolver

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tasomaniac.openwith.extensions.inflater
import com.tasomaniac.openwith.resolver.databinding.ResolveListItemBinding
import javax.inject.Inject

class ApplicationViewHolder private constructor(
    private val binding: ResolveListItemBinding,
    private val displaySubtext: Boolean
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        info: DisplayActivityInfo,
        itemClickListener: ItemClickListener? = null,
        itemLongClickListener: ItemLongClickListener? = null
    ) {
        binding.text1.text = info.displayLabel
        binding.text2.isVisible = displaySubtext
        binding.text2.text = info.extendedInfo
        binding.icon.setImageDrawable(info.displayIcon)

        itemView.setOnClickListener {
            itemClickListener?.onItemClick(info)
        }
        itemView.setOnLongClickListener {
            itemLongClickListener?.onItemLongClick(info) ?: false
        }
    }

    class Factory @Inject constructor() {

        fun createWith(parent: ViewGroup, displaySubtext: Boolean) =
            ApplicationViewHolder(ResolveListItemBinding.inflate(parent.inflater(), parent, false), displaySubtext)
    }
}
