package com.example.incidentsapp.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.example.incidentsapp.FirebaseCallback
import com.example.incidentsapp.FirebaseData
import com.example.incidentsapp.R
import com.example.incidentsapp.models.Status
import com.example.incidentsapp.notifications.NotificationData
import com.example.incidentsapp.notifications.PushNotification
import com.example.incidentsapp.utils.LoadingDialog
import com.example.incidentsapp.utils.MyFirebaseMessagingService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_filter.*
import kotlinx.android.synthetic.main.fragment_filter.toolbar_status
import kotlinx.android.synthetic.main.fragment_status.*
import java.io.File


class StatusFragment : DialogFragment() {

    private lateinit var idIncident: String
    private lateinit var status: String
    private lateinit var userId: String

    private lateinit var statusSaved: Status
    lateinit var loading: LoadingDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_IncidentsApp_FullScreenDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        idIncident = requireArguments().getString("id").toString()
        status = requireArguments().getString("status").toString()
        userId = requireArguments().getString("userId").toString()

        loading = LoadingDialog(requireActivity())

        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar_status?.setNavigationOnClickListener { v: View? -> dismiss() }
        toolbar_status?.setTitle("Cambiar estado")
        toolbar_status?.inflateMenu(R.menu.menu_dialog_incident)

        toolbar_status?.setOnMenuItemClickListener { item: MenuItem? ->
            when(item?.itemId){
                R.id.action_save -> { changeStatus() }
            }
            false

        }

        sliderStatus?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when(progress) {
                    0 -> statusSaved = Status.PENDING
                    1 -> statusSaved = Status.REJECTED
                    2 -> statusSaved = Status.INPROCESS
                    3 -> statusSaved = Status.FIXED
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                Log.i("SeekBar","OnStart")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.i("SeekBar","OnStop")
            }

        })

        when(status) {
            Status.PENDING.name -> sliderStatus.progress = 0
            Status.REJECTED.name -> sliderStatus.progress = 1
            Status.INPROCESS.name -> sliderStatus.progress = 2
            Status.FIXED.name -> sliderStatus.progress = 3
        }
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

    fun changeStatus() {
        loading.startLoadingDialog()
        FirebaseData().setStatus(idIncident, statusSaved.name, object :FirebaseCallback  {
            override fun onCallback(data: Any) {
                if(data == true){
                    sendNotificationToUser()
                    setPxToUser(statusSaved.name)
                    loading.dismissLoadingDialog()
                } else {
                    loading.dismissLoadingDialog()
                }
            }

        })
    }

    fun sendNotificationToUser() {
        FirebaseData().getDeviceIdFromUser(userId, object :FirebaseCallback  {
            override fun onCallback(data: Any) {
                var notificationData = NotificationData("¡Novedades!", "Una de sus incidencias ha cambiado de estado")
                PushNotification(notificationData, data as String, notificationData).sendNotification()
                dismiss()
                loading.dismissLoadingDialog()
            }

        })
    }

    fun setPxToUser(status: String) {
        when(status) {
            Status.PENDING.name -> FirebaseData().setPxToUser(userId, 0)
            Status.REJECTED.name -> FirebaseData().setPxToUser(userId, 0)
            Status.INPROCESS.name -> FirebaseData().setPxToUser(userId, 50)
            Status.FIXED.name -> FirebaseData().setPxToUser(userId, 200)
        }
    }

}