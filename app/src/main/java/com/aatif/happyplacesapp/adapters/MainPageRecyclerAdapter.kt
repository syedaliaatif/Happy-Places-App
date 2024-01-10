package com.aatif.happyplacesapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.aatif.happyplacesapp.R
import com.aatif.happyplacesapp.models.HappyPlaceModel
import com.aatif.happyplacesapp.utility.ImageLibrary
import com.aatif.happyplacesapp.utility.OnClickListener
import com.aatif.happyplacesapp.viewholder.HappyPlaceViewHolder


class MainPageRecyclerAdapter(private val imageLibrary: ImageLibrary): RecyclerView.Adapter<HappyPlaceViewHolder>() {
    private val data = mutableListOf<HappyPlaceModel>()
    private var listener: OnClickListener?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HappyPlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.happy_place_item_layout, parent, false)
        return HappyPlaceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: HappyPlaceViewHolder, position: Int) {
        holder.bind(data[position], imageLibrary.getBitmapFromTheAppUri(data[position].imageUrl?.toUri()))
        printLog("Listener is being set for position $position. Is Listener null? ${listener == null}!")
        holder.setOnClickListener{
            listener?.onClick(holder, position, data[position])
        }
    }

    fun updateContent(newData: List<HappyPlaceModel>){
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnClickListener){
        this.listener = listener
    }

    fun removeItemAt(position: Int){
        if(position >= 0 && position < data.size) {
            data.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItemAt(position: Int): HappyPlaceModel?{
        if(position >=0 && position < data.size){
            return data[position]
        }
        return null
    }


    private fun printLog(message:String){
        android.util.Log.d(this.javaClass.simpleName, message)
    }
}