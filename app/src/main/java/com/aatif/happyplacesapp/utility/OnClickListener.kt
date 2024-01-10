package com.aatif.happyplacesapp.utility

import com.aatif.happyplacesapp.models.HappyPlaceModel
import com.aatif.happyplacesapp.viewholder.HappyPlaceViewHolder

interface OnClickListener {
    fun onClick( itemViewHolder: HappyPlaceViewHolder,  position:Int, model: HappyPlaceModel)
}