package com.app.mangrove.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.mangrove.R
import com.app.mangrove.databinding.ItemVisitorBinding
import com.app.mangrove.model.ServiceEntry
import com.app.mangrove.model.Visitor
import kotlin.collections.ArrayList

class VisitorListAdapter(val visitors: ArrayList<Visitor>) :
    RecyclerView.Adapter<VisitorListAdapter.VisitorViewHolder>() {

    private var _binding: ItemVisitorBinding? = null
    private val binding get() = _binding!!
    private lateinit var mContext: Context

    fun setVisitorsList(mVisitors: ArrayList<Visitor>, context: Context) {

        mContext = context
        visitors.clear()
        visitors.addAll(mVisitors)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        _binding = ItemVisitorBinding.inflate(inflater,parent,false)
        return VisitorViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {
        holder.view.visitor = visitors.get(position)
    }

    override fun getItemCount() = visitors.size

    class VisitorViewHolder(val view: ItemVisitorBinding) : RecyclerView.ViewHolder(view.root)

}

