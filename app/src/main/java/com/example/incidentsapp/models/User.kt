package com.example.incidentsapp.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class User(var id: String? = null,
           var role: String? = null,
           var px: String? = null,
           var level: String? = null,
           var email: String? = null,
           var deviceId: String? = null,
           var incidentsN: String? = null,
           var badgesN: String? = null,
           var badges: MutableList<Badge>? = mutableListOf<Badge>()
) {
}