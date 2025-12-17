package com.example.emergencyresponder.modules.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.emergencyresponder.R
import com.example.emergencyresponder.modules.dashboard.data.EmergencyContacts

class EmergencyContactsAdapter(
    private val items: List<EmergencyContacts>
) : RecyclerView.Adapter<EmergencyContactsAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgIcon: ImageView = itemView.findViewById(R.id.person_img)
        val txtName: TextView = itemView.findViewById(R.id.personName)
        val txtPhone: TextView = itemView.findViewById(R.id.personPhone)
        val btnSos: Button = itemView.findViewById(R.id.sosButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.custom_contact_cards, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val item = items[position]

        holder.imgIcon.setImageResource(item.iconRes)
        holder.txtName.text = item.name
        holder.txtPhone.text = item.phone

        holder.btnSos.setOnClickListener {
            item.sosAction()
        }
    }

    override fun getItemCount(): Int = items.size
}
