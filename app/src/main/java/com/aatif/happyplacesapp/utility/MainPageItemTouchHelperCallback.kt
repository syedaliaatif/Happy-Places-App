package com.aatif.happyplacesapp.utility

import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.aatif.happyplacesapp.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class MainPageItemTouchHelperCallback(private val removeItemCallback:(Int)->Unit): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        when(direction){
            ItemTouchHelper.RIGHT -> {
                val position = viewHolder.adapterPosition
                removeItemCallback.invoke(position)
            }
            else -> Unit
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
    RecyclerViewSwipeDecorator.Builder(
        c,
        recyclerView,
        viewHolder,
        dX,
        dY,
        actionState,
        isCurrentlyActive)
        .addSwipeRightBackgroundColor(ContextCompat.getColor(recyclerView.context, R.color.red))
        .addSwipeRightActionIcon(R.drawable.delete)
        .create()
        .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}