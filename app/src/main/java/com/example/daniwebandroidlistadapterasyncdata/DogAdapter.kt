package com.example.daniwebandroidlistadapterasyncdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "DOG_ADAPTER"

class DogAdapter(private val imageUrlLoader: (String)->Unit)
    : ListAdapter<DogUiState, DogAdapter.DogViewHolder>(DIFF_UTIL_CALLBACK) {

    inner class DogViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val breed: TextView = view.findViewById(R.id.textView_dogBreed)
        val image: ImageView = view.findViewById(R.id.imageView_breedImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_view, parent, false)

        return DogViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val currentData = currentList[position]

        holder.breed.text = currentData.breed

        //If there is no image data, requests ViewModel
        //to start loading the images
        if (currentData.image != null){
            holder.image.setImageDrawable(currentData.image)
        } else {
            imageUrlLoader(currentData.breed)
        }
    }

    override fun onViewRecycled(holder: DogViewHolder) {
        //If Drawables are not released, ViewHolders will display wrong image
        //when you are scrolling too fast
        holder.image.setImageDrawable(null)
        super.onViewRecycled(holder)
    }

    companion object {
        val DIFF_UTIL_CALLBACK = object : DiffUtil.ItemCallback<DogUiState>() {
            override fun areItemsTheSame(oldItem: DogUiState, newItem: DogUiState): Boolean {
                //This is called first
                return oldItem.breed == newItem.breed
            }

            override fun areContentsTheSame(oldItem: DogUiState, newItem: DogUiState): Boolean {
                //This is called after
                return oldItem == newItem
            }
        }
    }
}