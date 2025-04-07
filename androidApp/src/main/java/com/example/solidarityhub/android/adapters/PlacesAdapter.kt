package com.example.solidarityhub.android.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.solidarityhub.android.data.model.PlaceSuggestion
import com.example.solidarityhub.android.databinding.ItemPlaceBinding

class PlacesAdapter(
    private val onItemClick: (PlaceSuggestion) -> Unit
) : ListAdapter<PlaceSuggestion, PlacesAdapter.PlaceViewHolder>(DiffCallback()) {

    inner class PlaceViewHolder(private val binding: ItemPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(place: PlaceSuggestion) {
            binding.rvSuggestion.text = place.name
            binding.root.setOnClickListener { onItemClick(place) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PlaceSuggestion>() {
        override fun areItemsTheSame(oldItem: PlaceSuggestion, newItem: PlaceSuggestion) =
            oldItem.placeId == newItem.placeId

        override fun areContentsTheSame(oldItem: PlaceSuggestion, newItem: PlaceSuggestion) =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
