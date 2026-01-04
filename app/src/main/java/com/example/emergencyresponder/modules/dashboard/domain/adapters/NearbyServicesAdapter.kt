package com.example.emergencyresponder.modules.dashboard.domain.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button

import com.example.emergencyresponder.R

import androidx.recyclerview.widget.RecyclerView
import com.example.emergencyresponder.modules.dashboard.data.model.NearbyService

class NearbyServicesAdapter(
    private val items: List<NearbyService>
) : RecyclerView.Adapter<NearbyServicesAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgIcon = itemView.findViewById<ImageView>(R.id.person_img)
        val txtTitle = itemView.findViewById<TextView>(R.id.txtTitle)
        val txtLocation = itemView.findViewById<TextView>(R.id.txtLocation)
        val btnSos = itemView.findViewById<Button>(R.id.buttonSos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nearby_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = items[position]

        holder.imgIcon.setImageResource(item.iconRes)
        holder.txtTitle.text = item.title
        holder.txtLocation.text = "${item.location} • ${item.distance}"

        holder.btnSos.setOnClickListener {
            item.sosAction()
        }
    }

    override fun getItemCount(): Int = items.size
}