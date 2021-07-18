package com.example.incidentsapp.utils

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.incidentsapp.FirebaseCallback
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.models.Incident
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*


data class FeatureCollection(
    val features: List<Feature>,
    val type: String = "FeatureCollection"
)

data class Feature(
    val geometry: Geometry,
    val properties: Map<String, String>,
    val type: String = "Feature"

)

data class Geometry (
    val coordinates: List<Double>,
    val type: String= "Point"
    )

class GeoJSON(context: Context) {

    var features = mutableListOf<Feature>()
    var context = context

    //Convierte a las incidencias en clase feature
    fun convertIncidentsToFeatures(incidents: MutableList<Incident>, callback: FirebaseCallback){
        for (incident in incidents){
                var feature = Feature(
                    geometry = Geometry(coordinates = listOf<Double>(incident.longitud!!, incident.latitud!!)),
                    properties = mapOf(
                        "id" to incident.id.toString(),
                        "description" to incident.description.toString(),
                        "status" to incident.status.toString()
                    )
                )
                features.add(feature)
            }

        saveJSON(GsonBuilder().setPrettyPrinting().create().toJson(FeatureCollection(features = features)))
        callback.onCallback(true)
    }

    // Guardar archivo geojson
    fun saveJSON(json: String){
        val output: Writer
        val file = createFileJSON()
        output = BufferedWriter(FileWriter(file))
        output.write(json)
        output.close()

        Log.d("Export", "Se ha exportado con éxito")
    }

    // Crear el archivo en el directorio seleccionado
    fun createFileJSON(): File {
        val fileName = "myJSON"

        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (!storageDir!!.exists()){
            storageDir.mkdir()
        }

        return File.createTempFile(fileName, ".geojson", storageDir
        )
    }
}