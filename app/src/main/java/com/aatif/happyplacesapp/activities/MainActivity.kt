package com.aatif.happyplacesapp.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aatif.happyplacesapp.R
import com.aatif.happyplacesapp.adapters.MainPageRecyclerAdapter
import com.aatif.happyplacesapp.database.DatabaseHandler
import com.aatif.happyplacesapp.databinding.ActivityMainBinding
import com.aatif.happyplacesapp.models.HappyPlaceModel
import com.aatif.happyplacesapp.utility.ImageLibrary
import com.aatif.happyplacesapp.utility.MainPageItemTouchHelperCallback
import com.aatif.happyplacesapp.utility.OnClickListener
import com.aatif.happyplacesapp.viewholder.HappyPlaceViewHolder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val databaseHandler = DatabaseHandler(this)
    private lateinit var imageLibrary: ImageLibrary
    private val itemTouchHelperCallback = MainPageItemTouchHelperCallback(this::removeItem)
    private val activityWithResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        refreshData()
    }
    private lateinit var adapter: MainPageRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLibrary = ImageLibrary(applicationContext)
        adapter = MainPageRecyclerAdapter(imageLibrary)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvHappyPlaces)
        binding.homePageFabButton.setOnClickListener{
            val intent = Intent(this, AddHappyPlacesActivity::class.java)
            activityWithResultLauncher.launch(intent)
        }
        adapter.setOnClickListener(listener = object : OnClickListener{
            override fun onClick(itemViewHolder: HappyPlaceViewHolder, position:Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(HappyPlaceDetailActivity.HAPPY_PLACE_KEY, model)
                activityWithResultLauncher.launch(intent)
            }
        })
        binding.rvHappyPlaces.adapter = this.adapter
        binding.rvHappyPlaces.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        refreshData()
        binding.rvHappyPlaces.visibility = View.VISIBLE

    }

    private fun refreshData(){
        val content = databaseHandler.getHappyPlaces()
        adapter.updateContent(content.toList())
    }

    private fun removeItem(position: Int) {
        val item = adapter.getItemAt(position)
        item?.let {
            adapter.removeItemAt(position)
            val rowsDeleted = databaseHandler.removeHappyPlace(it)
            printLog("Number of rows deleted are ${rowsDeleted}")
        }
    }

    private fun printLog(message: String){
        android.util.Log.d(TAG, message)
    }

    companion object {
        const val TAG: String = "Aatif_DBG"
    }
}