package com.aatif.happyplacesapp.viewholder

import android.graphics.Bitmap
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.aatif.happyplacesapp.R
import com.aatif.happyplacesapp.models.HappyPlaceModel
import de.hdodenhof.circleimageview.CircleImageView

class HappyPlaceViewHolder(private val view: View): ViewHolder(view)  {
    private val image: CircleImageView by lazy{view.findViewById(R.id.iv)}
    private val title: TextView by lazy { view.findViewById(R.id.title) }
    private val description: TextView by lazy{view.findViewById(R.id.description)}
    private val location: TextView by lazy { view.findViewById(R.id.location) }
    private val date: TextView by lazy{view.findViewById(R.id.date)}
    fun bind(model: HappyPlaceModel, bitmap: Bitmap?){
        image.setImageBitmap(bitmap)
        title.text =model.title
        description.text = model.description
        date.text = model.date
        location.text = model.location.name
    }

    fun setOnClickListener(listener: ()->Unit){
        view.setOnClickListener{
            printLog("View is clicked and listener is being invoked")
            listener.invoke()
        }
    }

    private fun printLog(s: String) {
        android.util.Log.d(this.javaClass.simpleName,s)
    }
}