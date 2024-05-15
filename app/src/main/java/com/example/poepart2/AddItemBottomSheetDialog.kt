package com.example.poepart2
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddItemBottomSheetDialog(userId1: String, collectionId1: String, categoryId1: String) : BottomSheetDialogFragment() {
    private var collectionId: String = collectionId1
    private var categoryId: String = categoryId1
    private var userId: String = userId1
    private lateinit var firebaseDataManager: FirebaseDataManager
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null
    private lateinit var itemAdapter: ItemAdapter
    private val REQUEST_STORAGE_PERMISSION_CODE = 1001
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageUri = uri
                imageView.setImageURI(uri)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        return inflater.inflate(R.layout.item_popup_layout, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("collecrion id issso: $collectionId")
        imageView = view.findViewById<ImageView>(R.id.item_image)
        imageView.setImageResource(R.drawable.image_placeholder)

        val selectImageButton = view.findViewById<Button>(R.id.select_image_button)

        selectImageButton.setOnClickListener {
            requestStoragePermission()

        }
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView(cal)
        }

        view.findViewById<Button>(R.id.item_date_button).setOnClickListener {
            DatePickerDialog(
                requireContext(), dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        firebaseDataManager = FirebaseDataManager()
itemAdapter = ItemAdapter(mutableListOf())
        view.findViewById<Button>(R.id.confirm_button3).setOnClickListener {
            val itemName = view.findViewById<EditText>(R.id.item_name_edittext).text.toString()
            val itemDescription = view.findViewById<EditText>(R.id.item_description_edittext).text.toString()
            val itemDate = view.findViewById<EditText>(R.id.item_date_edittext).text.toString()

            try {
                if (itemName.isNotEmpty() && itemDescription.isNotEmpty() && itemDate.isNotEmpty() && imageUri != null) {
                    // Get the file path from the URI
                    val filePath = context?.let { it1 -> getPathFromUri(it1, imageUri!!) }
                    if (filePath != null) {
                        val file = File(filePath)
                        if (file.exists()) {
                            // Point the uploaded file to one path before uploading
                            val newItem = Item(itemName, itemDescription, filePath, itemDate)
                            firebaseDataManager.addItem(userId, categoryId, collectionId, newItem, imageUri!!, itemAdapter)
                            dismiss()
                        } else {
                            // File does not exist
                            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // File path is null
                        Toast.makeText(context, "File path is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Show error message or handle invalid input
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                println("There is an error $e")
            }



        }

        view.findViewById<Button>(R.id.cancel_button1).setOnClickListener {
            dismiss()
        }
    }
    private fun updateDateInView(cal: Calendar) {
        val myFormat = "dd-MM-yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        view?.findViewById<EditText>(R.id.item_date_edittext)?.setText(sdf.format(cal.time))
    }
    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION_CODE
            )
        } else {
            // Permission already granted
            openImageChooser()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                openImageChooser()
            } else {

            }
        }
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        var realPath: String? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            realPath = it.getString(columnIndex)
        }
        return realPath
    }


    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    companion object {
        private const val RESULT_OK = -1
    }
}
