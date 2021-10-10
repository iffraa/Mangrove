package com.app.mangrove.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.mangrove.databinding.PackageItemBinding
import com.app.mangrove.model.ServicePackage
import androidx.annotation.NonNull


class PackageListAdapter(val pckgList: ArrayList<ServicePackage>, val onItemCheckListener: OnItemCheckListener) :
    RecyclerView.Adapter<PackageListAdapter.PackageViewHolder>() {

    private var _binding: PackageItemBinding? = null
    private val binding get() = _binding!!
    private var package_id: String =""
    private var packagee: ServicePackage? =null
    private var selectedPosition = -1 // no selection by default

    interface OnItemCheckListener {
        fun onItemCheck(item: ServicePackage?)
        fun onItemUncheck(item: ServicePackage?)
    }

    @NonNull
    private var onItemClick: OnItemCheckListener? = null

    fun updatePckgList(newPckgList: ArrayList<ServicePackage>) {
        pckgList.clear()
        pckgList.addAll(newPckgList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = PackageItemBinding.inflate(inflater)
        return PackageViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        holder.view.servicePackage = pckgList.get(position)

        holder.view.ctvPckgs.setChecked(selectedPosition == position);

        onItemClick = onItemCheckListener
        holder.view.ctvPckgs.setOnClickListener(View.OnClickListener {
            holder.view.ctvPckgs.toggle()
            selectedPosition = holder.getAdapterPosition();

            if(selectedPosition == position){
                holder.view.ctvPckgs.setChecked(true);
            }
            else{
                holder.view.ctvPckgs.setChecked(false);
            }

            notifyDataSetChanged()

            if(holder.view.ctvPckgs.isChecked) {
                onItemClick!!.onItemCheck(holder.view.servicePackage);
            }
            else
            {
                onItemClick!!.onItemUncheck(holder.view.servicePackage);
            }

        })

    }

    override fun getItemCount() = pckgList.size

    class PackageViewHolder(val view: PackageItemBinding) : RecyclerView.ViewHolder(view.root)

}