package com.example.incidentsapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import com.example.incidentsapp.R

class LoadingDialog(var act: Activity) {

    lateinit var activity: Activity
    lateinit var dialog: AlertDialog

    init {
        this.activity = act
    }

    fun startLoadingDialog() {
        var builder: AlertDialog.Builder = AlertDialog.Builder(activity, R.style.Theme_IncidentsApp_FullScreenDialogLoading)
        var inflater:LayoutInflater = activity.layoutInflater

        builder.setView(inflater.inflate(R.layout.loading_dialog, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun dismissLoadingDialog() {
        dialog.dismiss()
    }
}