package com.aatif.happyplacesapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getStringOrNull
import com.aatif.happyplacesapp.models.HappyPlaceModel
import com.aatif.happyplacesapp.models.Location
import java.sql.SQLException
class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION ){

     companion object{
        const val DATABASE_NAME : String = "HappyPlacesDatabase"
        const val DATABASE_VERSION : Int = 1
        const val TABLE_HAPPY_PLACE_NAME = "HappyPlacesTable"
        const val KEY_ID ="id"
        const val KEY_TITLE ="title"
        const val KEY_DESCRIPTION="description"
        const val KEY_DATE="date"
        const val KEY_LOCATION_NAME ="location_name"
        const val KEY_LOCATION_LONGITUDE="longitude"
        const val KEY_LOCATION_LATITUDE="latitude"
         const val KEY_IMAGE_URL="image_url"
    }

    override fun onCreate(database: SQLiteDatabase?) {
       val sql = "CREATE TABLE ${TABLE_HAPPY_PLACE_NAME} (" +
               "$KEY_ID INTEGER," +
               "$KEY_TITLE TEXT ," +
               "$KEY_DESCRIPTION TEXT," +
               "$KEY_DATE TEXT," +
               "$KEY_LOCATION_NAME TEXT," +
               "$KEY_LOCATION_LATITUDE TEXT," +
               "$KEY_LOCATION_LONGITUDE TEXT," +
               "$KEY_IMAGE_URL TEXT," +
               "PRIMARY KEY ($KEY_ID))"
        database?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val sql = "DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE_NAME"
        db?.execSQL(sql)
        onCreate(db)
    }

    fun addHappyPlace(happyPlaceModel: HappyPlaceModel):Long{
        val db = this.writableDatabase
        val contentValues = getContentValue(happyPlaceModel)
        val result = db.insert(TABLE_HAPPY_PLACE_NAME,null, contentValues)
        db.close()
        return result
    }

    fun updateHappyPlace(happyPlaceModel: HappyPlaceModel){
        val db = this.writableDatabase
        db.update(TABLE_HAPPY_PLACE_NAME, getContentValue(happyPlaceModel), "$KEY_ID=?", arrayOf(happyPlaceModel.id.toString()) )
    }

    fun getHappyPlaces(): ArrayList<HappyPlaceModel>{
        val db = this.readableDatabase
        val sql = "SELECT * FROM $TABLE_HAPPY_PLACE_NAME"
        val cursor = db.rawQuery(sql, null)
        val result = arrayListOf<HappyPlaceModel>()
        try {
            if (cursor.moveToFirst()) {
                do {
                    result.add(
                        HappyPlaceModel(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                            title = cursor.getStringOrNull(cursor.getColumnIndex(KEY_TITLE)).orEmpty(),
                            description = cursor.getStringOrNull(cursor.getColumnIndex(
                                KEY_DESCRIPTION)).orEmpty(),
                            date = cursor.getStringOrNull(cursor.getColumnIndex(KEY_DATE)).orEmpty(),
                            location = Location(
                                name = cursor.getStringOrNull(cursor.getColumnIndex(
                                    KEY_LOCATION_NAME)).orEmpty(),
                                latitude = cursor.getDoubleOrNull(cursor.getColumnIndex(
                                    KEY_LOCATION_LATITUDE)),
                                longitude = cursor.getDoubleOrNull(cursor.getColumnIndex(
                                    KEY_LOCATION_LONGITUDE
                                ))
                            ),
                            imageUrl = cursor.getStringOrNull(cursor.getColumnIndex(
                                KEY_IMAGE_URL))
                        )
                    )


                } while (cursor.moveToNext())

            }
        }catch (exception: SQLException){
            exception.printStackTrace()
            db.close()
            return arrayListOf()
        }
        db.close()
        cursor.close()

        return result

    }

    private fun getContentValue(happyPlaceModel: HappyPlaceModel): ContentValues{
        val contentValues = ContentValues()
         with(happyPlaceModel){
             return@getContentValue contentValues.apply {
                 put(KEY_ID, id)
                 put(KEY_TITLE, title)
                 put(KEY_DESCRIPTION, description)
                 put(KEY_DATE, date)
                 put(KEY_LOCATION_NAME, location.name)
                 put(KEY_LOCATION_LATITUDE, location.latitude)
                 put(KEY_LOCATION_LONGITUDE, location.longitude)
                 put(KEY_IMAGE_URL, imageUrl)
             }
        }
    }

    fun removeHappyPlace(model: HappyPlaceModel):Int {
        val db = this.writableDatabase
        return db.delete(TABLE_HAPPY_PLACE_NAME, "${KEY_ID} = ?", arrayOf(model.id.toString()))
    }
}