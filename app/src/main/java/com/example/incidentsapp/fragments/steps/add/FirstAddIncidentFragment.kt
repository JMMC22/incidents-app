package com.example.incidentsapp.fragments.steps.add

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.incidentsapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_first_add_incident.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class FirstAddIncidentFragment : Fragment() {

    private lateinit var uploadButton : Button

    private lateinit var currentPhotoPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first_add_incident, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uploadButton = view.findViewById(R.id.uploadButton)

        uploadButton.setOnClickListener(View.OnClickListener { v: View? ->
            dispatchTakePictureIntent()
        })
    }


    //Intent para realizar foto con la camara
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                val photoFile : File? = try {
                    createImageFile()
                }catch (ex:IOException){
                    null
                }
                photoFile?.also {
                    val photoURI : Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    //Si se ha conseguido, se pasa el path de la imagen como argumento.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imagenUpload.setImageURI(Uri.fromFile(File(currentPhotoPath)))
            arguments = Bundle().apply {
                putString("image", currentPhotoPath)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp : String = SimpleDateFormat("yyyMMdd_HHmmss").format(Date())
        val storageDir : File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }


    companion object {
        val REQUEST_IMAGE_CAPTURE = 1

    }
}