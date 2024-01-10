package com.aatif.happyplacesapp.utility

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageLibrary(private val context: Context) {

    fun getBitmapFromTheAppUri(uri:Uri?): Bitmap?{
        return uri?.let {
            BitmapFactory.decodeFile(it.toString())
        }
    }

    fun getBitmapFromTheContentUri(uri:Uri?): Bitmap?{
        return uri?.let{
            MediaStore.Images.Media.getBitmap(context.contentResolver, it)
        }
    }

    fun saveBitmapToLocalStorage(bitmap: Bitmap?):String?{
        if(bitmap == null)return null
        val wrapper = ContextWrapper(context)
        val directory = wrapper.getDir(MediaStore.Images.Media.DATA, MODE_PRIVATE)
        val file = File(directory, "${UUID.randomUUID()}.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        stream.flush()
        stream.close()
        return file.absolutePath
    }

}