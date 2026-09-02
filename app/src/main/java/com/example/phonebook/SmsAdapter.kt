package com.example.phonebook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class SmsAdapter(
    private var messages: List<SmsMessage>,
    private val onDelete: (SmsMessage) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<SmsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvBody: TextView = view.findViewById(R.id.tvBody)
        val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
        val btnDelete: MaterialButton = view.findViewById(R.id.btnDeleteSms)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sms, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.tvAddress.text = message.address
        holder.tvDate.text = message.date
        holder.tvBody.text = message.body

        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked = message.isSelected
        holder.cbSelect.setOnCheckedChangeListener { _, isChecked ->
            message.isSelected = isChecked
            onSelectionChanged()
        }

        holder.btnDelete.setOnClickListener { onDelete(message) }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<SmsMessage>) {
        this.messages = newMessages
        notifyDataSetChanged()
    }

    fun getSelectedMessages(): List<SmsMessage> {
        return messages.filter { it.isSelected }
    }

    fun selectAll(select: Boolean) {
        messages.forEach { it.isSelected = select }
        notifyDataSetChanged()
        onSelectionChanged()
    }
}
