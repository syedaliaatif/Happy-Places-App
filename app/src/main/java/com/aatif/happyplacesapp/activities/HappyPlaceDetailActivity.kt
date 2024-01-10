package com.aatif.happyplacesapp.activities

import android.Manifest
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.widget.doOnTextChanged
import com.aatif.happyplacesapp.database.DatabaseHandler
import com.aatif.happyplacesapp.databinding.ActivityHappyPlaceDetailBinding
import com.aatif.happyplacesapp.models.HappyPlaceModel
import com.aatif.happyplacesapp.utility.ImageLibrary
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.text.SimpleDateFormat
import java.util.Calendar

class HappyPlaceDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHappyPlaceDetailBinding
    private lateinit var happyPlace: HappyPlaceModel
    private lateinit var originalHappyPlace: HappyPlaceModel
    private lateinit var imageLibrary : ImageLibrary
    private val calendar = Calendar.getInstance()
    private val imageUploadLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
        if(it==null)return@registerForActivityResult
        val bitmap = imageLibrary.getBitmapFromTheContentUri(it)
        binding.ivCurrentSelectedImage.setImageBitmap(bitmap)
        happyPlace = happyPlace.copy(imageUrl = imageLibrary.saveBitmapToLocalStorage(bitmap))
        enableSaveButton()
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
        if(it == null)return@registerForActivityResult
        binding.ivCurrentSelectedImage.setImageBitmap(it)
        happyPlace = happyPlace.copy(imageUrl = imageLibrary.saveBitmapToLocalStorage(bitmap = it))
        enableSaveButton()
    }
    private lateinit var databaseHandler: DatabaseHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("AATIF_DBG", "on create")
        imageLibrary = ImageLibrary(applicationContext)
        databaseHandler = DatabaseHandler(applicationContext)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        happyPlace = intent.getHappyPlace() ?: run{
            finish()
            return
        }
        originalHappyPlace = happyPlace
        setCalendar()
        setupView()
    }

    private fun setCalendar() {
        val sdf = SimpleDateFormat(SIMPLE_DATE_FORMAT_PATTERN)
        calendar.time = sdf.parse(happyPlace.date)
    }

    private fun Intent.getHappyPlace():HappyPlaceModel?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) this.getParcelableExtra(HAPPY_PLACE_KEY, HappyPlaceModel::class.java)
        else this.getParcelableExtra<HappyPlaceModel>(HAPPY_PLACE_KEY)
    }

    private fun setupView() {
        binding.editTextTitle.setText(happyPlace.title)
        binding.editTextDescription.setText(happyPlace.description)
        binding.editTextDate.setText(happyPlace.date)
        binding.editTextLocation.setText(happyPlace.location.name)
        binding.ivCurrentSelectedImage.setImageBitmap(imageLibrary.getBitmapFromTheAppUri(happyPlace.imageUrl?.toUri()))
        binding.updateImageButton.setOnClickListener {
            showDialogForTakePictureOrUpload()
        }
        binding.editTextDate.setOnClickListener{
            showDatePicker()
        }
        setupSaveButton()
    }

    private fun showDatePicker() {
       DatePickerDialog(this,
           { _, year, month, dayOfMonth ->
               calendar.set(Calendar.YEAR, year)
               calendar.set(Calendar.MONTH, month)
               calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
               binding.editTextDate.setText(calendar.toStrf())
           },
           calendar.get(Calendar.YEAR),
           calendar.get(Calendar.MONTH),
           calendar.get(Calendar.DAY_OF_MONTH)
           ).show()
    }


    private fun setupSaveButton(){
        binding.editTextDate.doOnTextChanged { text, start, before, count ->
            printLog("date changed.")
            if(text != originalHappyPlace.date){
                happyPlace = happyPlace.copy(date = text.toString())
                enableSaveButton()
            }
        }
        binding.editTextLocation.doOnTextChanged { text, start, before, count ->
            printLog("location changed.")
            if(text != originalHappyPlace.location.name){
                happyPlace = happyPlace.copy(location = happyPlace.location.copy(name = text.toString()))
                enableSaveButton()
            }
        }
        binding.editTextTitle.doOnTextChanged { text, start, before, count ->
            printLog("title changed.")
            if(text!=originalHappyPlace.title){
                happyPlace = happyPlace.copy(title = text.toString())
                enableSaveButton()
            }
        }
        binding.editTextDescription.doOnTextChanged { text, start, before, count ->
            printLog("description changed.")
            if(text != originalHappyPlace.description){
                happyPlace = happyPlace.copy(description = text.toString())
                enableSaveButton()
            }
        }
        binding.saveButton.setOnClickListener {
            printLog("save button clicked.")
            storeHappyPlaceToDatabase()
        }
    }

    private fun storeHappyPlaceToDatabase() {
        databaseHandler.updateHappyPlace(happyPlaceModel = happyPlace)
        originalHappyPlace = happyPlace
        disableSaveButton()
        showToast("Updated details of the happy place.")
    }

    private fun showToast(toastMessage: String) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    private fun enableSaveButton() {
        binding.saveButton.isEnabled = true
    }

    private fun disableSaveButton(){
        binding.saveButton.isEnabled = false
    }

    private fun showDialogForTakePictureOrUpload() {
        val dialog = AlertDialog.Builder(this)
        dialog.setItems(
            arrayOf("Take A Picture", "Upload From Device")
        ){ _: DialogInterface, index: Int ->
            when(index){
                0 -> takeAPictureFromCamera()
                1 -> uploadPictureFromDevice()
                else -> Unit
            }
        }.setTitle("Update Picture")
            .show()
    }

    private fun uploadPictureFromDevice() {
        askForPermissionIfRequired {
            imageUploadLauncher.launch(PickVisualMediaRequest(
                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
            ))
        }
    }

    private fun takeAPictureFromCamera() {
        askForPermissionIfRequired{
            cameraLauncher.launch(null)
        }
    }

    private fun askForPermissionIfRequired(
        doOnPermissionGiven : ()->Unit
    ) {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA
            )
            .withListener(
                object : MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if(report?.areAllPermissionsGranted() == true){
                            doOnPermissionGiven.invoke()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        request: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }

                }
            )
            .onSameThread()
            .check()
    }

    private fun Calendar.toStrf(): String {
        val sdf = SimpleDateFormat(SIMPLE_DATE_FORMAT_PATTERN)
        return sdf.format(this.time)
    }

    companion object{
        const val HAPPY_PLACE_KEY: String = "_happy_place"
        private val TAG : String = Companion::class.java.simpleName
        private fun printLog(msg: String){
            android.util.Log.d(TAG, msg)
        }
        private const val SIMPLE_DATE_FORMAT_PATTERN = "dd/MM/yyyy"
    }
}
