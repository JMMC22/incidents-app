package com.example.incidentsapp.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.MapsActivity
import com.example.incidentsapp.R
import com.example.incidentsapp.adapters.ViewPagerAdapter
import com.example.incidentsapp.models.Incident
import com.example.incidentsapp.models.Status
import com.example.incidentsapp.utils.LoadingDialog
import com.example.incidentsapp.utils.UserProfile
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.shuhart.stepview.StepView
import kotlinx.android.synthetic.main.fragment_add_incident.*
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.toolbar_status
import java.io.File
import java.util.*

class AddIncidentFragment : DialogFragment() {


    private lateinit var database: DatabaseReference
    private lateinit var storage : StorageReference
    private var user = Firebase.auth.currentUser

    private var latitud : Double = 0.0
    private var longitud : Double = 0.0

    private lateinit var stepView : StepView
    private lateinit var viewPager : ViewPager2

    private lateinit var filePath: Uri

    private var imagePath : String = ""

    private lateinit var loading: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = Firebase.database.reference
        storage= Firebase.storage.reference
        loading = LoadingDialog(requireActivity())

        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_IncidentsApp_FullScreenDialog)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        latitud = requireArguments().getDouble("latitude")
        longitud = requireArguments().getDouble("longitude")

        Log.d(latitud.toString(),"LATITUD")
        Log.d(longitud.toString(),"LONGITUD")

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_incident, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar_status?.setNavigationOnClickListener { v: View? -> dismiss() }
        toolbar_status?.setTitle("Añadir nueva incidencia")
        toolbar_status?.inflateMenu(R.menu.menu_dialog_incident)

        toolbar_status?.setOnMenuItemClickListener { item: MenuItem? ->
            when(item?.itemId){
                R.id.action_save ->{

                    val fragmentOne = requireActivity().supportFragmentManager.findFragmentByTag("f0")
                    val imagePath  = fragmentOne!!.arguments?.getString("image")

                    val fragmentTwo = requireActivity().supportFragmentManager.findFragmentByTag("f1")
                    val description  = fragmentTwo!!.arguments?.getString("description")

                    val fragmentThird = requireActivity().supportFragmentManager.findFragmentByTag("f2")
                    val tag  = fragmentThird!!.arguments?.getString("tag")

                    if(checkFields(imagePath,description,tag)){
                        filePath = Uri.fromFile(File(imagePath))
                        loading.startLoadingDialog()
                        uploadImageAndCreateIncident(description,tag)
                    }else{
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("¡Faltan datos!")
                            .setMessage("Rellene todos los campos.")
                            .show()
                    }

                    true
                }
            }
            false

        }


        stepView = view?.findViewById(R.id.stepView)
        viewPager = view?.findViewById(R.id.viewPager)

        setupStepView()
        setupViewPager()
        settingsButtons()


    }


    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT

            dialog?.window?.setLayout(width, height)
            dialog?.window?.setWindowAnimations(R.style.Theme_IncidentsApp_Slide)
        }
    }

    //Definicion stepper
    private fun setupStepView(){
        stepView.state
            .steps(listOf("Foto","Descripción","Categoría"))
            .stepsNumber(3)
            .animationDuration(resources.getInteger(android.R.integer.config_shortAnimTime))
            .commit()
    }

    //Adociar viewPager con adapter y configuración botones callback
    private fun setupViewPager(){
        viewPager.adapter = ViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle)
        
        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    stepView.go(position,true)
                    UIButtons(position)
                }
            }
        )
    }

    //Configuración botones next y previous
    private fun settingsButtons(){

        previous_button.setOnClickListener(View.OnClickListener {
            viewPager.setCurrentItem(viewPager.currentItem - 1,false)
        })

        next_button.setOnClickListener(View.OnClickListener {
            viewPager.setCurrentItem(viewPager.currentItem + 1,false)
        })
    }

    //función para el diseño de los botones
    private fun UIButtons(position:Int){
        when(position){
            0 -> {
                previous_button.visibility = View.INVISIBLE
                next_button.visibility = View.VISIBLE
                toolbar_status?.menu?.findItem(R.id.action_save)?.setVisible(false)


            }
            1 ->{
                previous_button.visibility = View.VISIBLE
                next_button.visibility = View.VISIBLE
                toolbar_status?.menu?.findItem(R.id.action_save)?.setVisible(false)

            }
            2 ->{
                previous_button.visibility = View.VISIBLE
                next_button.visibility = View.INVISIBLE
                toolbar_status?.menu?.findItem(R.id.action_save)?.setVisible(true)

            }

        }
    }

    //Se crea imagen en firebase y se crea incidents con los datos
    private fun uploadImageAndCreateIncident(description: String?,tag: String?){
        if(filePath !=null){
            val sref: StorageReference = storage.child("images/"+UUID.randomUUID().toString())
            sref.putFile(filePath)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->

                    taskSnapshot?.metadata?.reference?.downloadUrl?.addOnSuccessListener {
                        val incident = Incident(UUID.randomUUID().toString(),description,tag,it.toString(),latitud,longitud, Status.PENDING.name, user?.uid)

                        database
                            .child("incidents")
                            .child(incident.id!!)
                            .setValue(incident)
                    }
                    loading.dismissLoadingDialog()

                    FirebaseData().addOneIncidentToCount((activity as MapsActivity).userData)

                    dismiss()
                    Snackbar.make(requireActivity().findViewById(R.id.mapFragment), "Subida correctamente.", Snackbar.LENGTH_SHORT).show()

                }
                .addOnFailureListener(OnFailureListener {
                    loading.dismissLoadingDialog()

                    dismiss()
                    Snackbar.make(requireActivity().findViewById(R.id.mapFragment), "Parece que hemos tenido un error.", Snackbar.LENGTH_SHORT).show()
                })

        }

    }

    //Función para checkear que los campos no están vacíos o nulos
    private fun checkFields(imagePath: String?, description:String?, tag:String?): Boolean{
        if(imagePath == null || description == null || tag == null || description.isEmpty() || tag.isEmpty()){

            return false
        }
        return true
    }

}