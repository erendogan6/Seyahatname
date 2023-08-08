package com.erendogan.konumgunluk.adaptor

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.erendogan.konumgunluk.databinding.RowBinding
import com.erendogan.konumgunluk.model.Konum
import com.erendogan.konumgunluk.view.MapsActivity

class RecyclerAdaptor (private val list: List<Konum>) : RecyclerView.Adapter<RecyclerAdaptor.ViewHolder>() {

    class ViewHolder(val binding:RowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val recyclerRowBinding = RowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.textKonum.text = list[position].isim
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context,MapsActivity::class.java)
            intent.putExtra("Konum", list[position])
            holder.itemView.context.startActivity(intent)
        }
    }


}