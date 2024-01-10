package com.aatif.happyplacesapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.aatif.happyplacesapp.R
import com.aatif.happyplacesapp.database.DatabaseHandler
import com.aatif.happyplacesapp.databinding.ActivityAddHappyPlacesBinding
import com.aatif.happyplacesapp.models.HappyPlaceModel
import com.aatif.happyplacesapp.models.Location
import com.aatif.happyplacesapp.utility.ImageLibrary
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID


class AddHappyPlacesActivity : AppCompatActivity(), OnClickListener{
    private lateinit var binding: ActivityAddHappyPlacesBinding
    private val calendar= Calendar.getInstance()
    private lateinit var imageLibrary: ImageLibrary
    private val pickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia() ){
        if(it==null)return@registerForActivityResult
        val bitmap = imageLibrary.getBitmapFromTheContentUri(it)
        binding.ivSelectedImage.setImageBitmap(bitmap)
        imageUrl = imageLibrary.saveBitmapToLocalStorage(bitmap)
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){ bitmap->
        if(bitmap == null)return@registerForActivityResult
        binding.ivSelectedImage.setImageBitmap(bitmap)
        imageUrl = imageLibrary.saveBitmapToLocalStorage(bitmap)
    }
    private val launchAutocomplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        printLog(Autocomplete.getStatusFromIntent(it.data).statusMessage.toString())
        if(it.resultCode == Activity.RESULT_OK){
            if(it.data == null){
                return@registerForActivityResult
            }
            val place = Autocomplete.getPlaceFromIntent(it.data)
            setPlace(place)
        }
    }
    private var imageUrl: String? = null
    private var dbHandler: DatabaseHandler? = null
    private var latitude: Double?=null
    private var longitude: Double?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLibrary = ImageLibrary(applicationContext)
        dbHandler = DatabaseHandler(this)
        binding = ActivityAddHappyPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.addPlacesToolbar)
        supportActionBar?.title = resources.getString(R.string.add_places_activity_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.addPlacesToolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        binding.addImageButton.setOnClickListener(this)
        binding.editTextDate.setOnClickListener(this)
        binding.editTextLocation.setOnClickListener(this)
        binding.saveButton.setOnClickListener(this)
    }

    private fun showDatePicker(){
        val datesetListener =
            DatePickerDialog.OnDateSetListener { picker, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                SimpleDateFormat(SIMPLE_DATE_FORMAT_PATTERN).format(calendar.time).also{
                    binding.editTextDate.setText(it)
                }
            }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, datesetListener, year, month, day ).show()
    }

    private fun doOnAddImageButton(){
       val dialog =  AlertDialog.Builder(this)
        dialog.setTitle("Add Image")
        dialog.setItems(arrayOf("Upload From Device", "Take A Picture")
        ) { dialog, which ->
            when (which) {
                0 -> uploadFromDevice(dialog)
                1 -> takePictureFromCamera(dialog)
                else -> Unit
            }
        }.show()
    }

    private fun requestPermission(
        doOnAllPermissionGranted: (()->Unit)?,
        doOnAllPermissionsNotGranted: (()->Unit)?,
        doOnPermissionRationaleShouldBeShown: (()->Unit)?
    ){
        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let{
                        if(it.areAllPermissionsGranted()){
                            doOnAllPermissionGranted?.invoke()
                        }else{
                            doOnAllPermissionsNotGranted?.invoke()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    requests: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    doOnPermissionRationaleShouldBeShown?.invoke()
                }

            }).onSameThread()
            .check()
    }

    private fun takePictureFromCamera(dialog: DialogInterface?) {
        requestPermission(
            this::openCameraAndTakePicture,
            {
                showToast("You have denied to give permission for this feature. Go to app settings to use this feature.")

            },
            this::showRequestPermissionRationale
            )
    }

    private fun openCameraAndTakePicture() {
        cameraLauncher.launch(null)
    }

    private fun uploadFromDevice(dialogInterface: DialogInterface) {
        requestPermission(
            this::selectMediaFromDevice,
            {
                showToast("You have denied to give permission for this feature. Go to app settings to use this feature.")

            },
            this::showRequestPermissionRationale
        )

    }

    private fun showRequestPermissionRationale() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
        builder.setMessage(
            "It looks like you have not " +
            "granted the permission required for this " +
            "feature. Permissions can be enabled from" +
            " the App Settings section.")
        builder.setPositiveButton("Give Permission"){ _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.setData(uri)
                    startActivity(intent)

        }
        builder.setNegativeButton("Go back"){
            dialog, _ -> dialog.dismiss()
        }
        builder.show()
    }

    private fun selectMediaFromDevice(){
        pickerLauncher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.add_image_button -> doOnAddImageButton()
            R.id.edit_text_date -> showDatePicker()
            R.id.save_button -> saveHappyPlaceInfoToDatabase()
            R.id.edit_text_location -> showAutoComplete()
            else -> Unit
        }
    }

    private fun showAutoComplete() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.places_api_key), Locale.US);
        }
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
            .build(this)
        launchAutocomplete.launch(intent)
    }

    private fun saveHappyPlaceInfoToDatabase() {
        when {
            binding.editTextTitle.text.isNullOrEmpty() -> {
                showToast("Title can not be empty.")
                return
            }
            binding.editTextDescription.text.isNullOrEmpty() -> {
                showToast("Description can not be empty.")
                return
            }
            binding.editTextDate.text.isNullOrEmpty() -> {
                showToast("Date can not be empty.")
                return
            }
            binding.editTextLocation.text.isNullOrEmpty() || longitude == null || latitude == null -> {
                showToast("Entered location can not be identified!")
                return
            }
            imageUrl.isNullOrEmpty() -> {
                showToast("You need to upload an image.")
                return
            }

        }
        val happyPlace = HappyPlaceModel(
            id = (Math.random() * 10000000.0).toInt(),
            title = binding.editTextTitle.text.toString(),
            description = binding.editTextDescription.text.toString(),
            date = binding.editTextDate.text.toString(),
            imageUrl = imageUrl,
            location = Location(
                name = binding.editTextLocation.text.toString(),
                longitude = longitude,
                latitude = latitude
            )
        )
        val result = dbHandler?.addHappyPlace(happyPlace)
        printLog("Result of the db operation is : $result")
        setResult(RESULT_OK)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private fun printLog(message:String){
            android.util.Log.d("AATIF_DBG", message)
        }
        const val SIMPLE_DATE_FORMAT_PATTERN = "dd/MM/yyyy"
    }

    private fun setPlace(place: Place) {
        val placeName = place.name
        val placeLatLng = place.latLng
        binding.editTextLocation.setText(placeName.orEmpty())
        placeLatLng?.let {
            longitude = it.longitude
            latitude = it.latitude
        }
    }

}