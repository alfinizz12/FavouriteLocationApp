package com.example.favouritelocationnotesapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.favouritelocationnotesapp.Model.LocationNote
import com.example.favouritelocationnotesapp.R

class LocationNotesAdapter(
    private val locationNoteList: List<LocationNote>,
    private val onItemClick: (LocationNote) -> Unit,
    private val onDeleteClick: (LocationNote) -> Unit,
    private val onItemLongClick: (LocationNote) -> Unit
) : RecyclerView.Adapter<LocationNotesAdapter.LocationViewHolder>() {

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)
        val description: TextView = itemView.findViewById(R.id.tvDescription)
        val date : TextView = itemView.findViewById(R.id.Date)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locationNoteList[position]

        holder.title.text = location.title
        holder.description.text = location.description
        holder.date.text = location.createdAt.toString()

        // klik untuk buka maps
        holder.itemView.setOnClickListener {
            onItemClick(location)
        }

        // klik untuk hapus
        holder.btnDelete.setOnClickListener {
            onDeleteClick(location)
        }

        // fungsi klik long update
        holder.itemView.setOnLongClickListener {
            onItemLongClick(location)
            true // 'true' menandakan event ini sudah ditangani
        }
    }

    override fun getItemCount(): Int = locationNoteList.size
}