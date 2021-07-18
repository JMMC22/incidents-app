package com.example.incidentsapp.models

import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
data class Badge(var id: String? = null,
                 var title: String? = null,
                 var description: String? = null,
                 var minCount: String? = null,
                 var maxCount: String? = null,
                 var actualCount: String? = null,
                 var type: String? = null,
                 var done: Boolean? = false) {

    fun generateBadges(): MutableList<Badge> {
        var badges: MutableList<Badge> = mutableListOf(
            Badge(UUID.randomUUID().toString(),
                "Primera incidencia",
                "Reporta tu primera incidencia",
                "0",
                "1",
                "0",
                BadgeType.INCIDENT.toString(),
                false),
            Badge(UUID.randomUUID().toString(),
                "Llega al nivel 1",
                "Suma los puntos de experiencia necesarios para subir al nivel 1.",
                "0",
                "1",
                "0",
                BadgeType.LEVEL.toString(),
                false),
            Badge(UUID.randomUUID().toString(),
                "Reporta 5 incidencias",
                "Reporta 5 incidencias a la plataforma.",
                "0",
                "5",
                "0",
                BadgeType.INCIDENT.toString(),
                false),
            Badge(UUID.randomUUID().toString(),
                "Llega al nivel 5",
                "Suma los puntos de experiencia necesarios para subir al nivel 5.",
                "0",
                "5",
                "0",
                BadgeType.LEVEL.toString(),
                false),
            Badge(UUID.randomUUID().toString(),
                "Llega al nivel 10",
                "Suma los puntos de experiencia necesarios para subir al nivel 10.",
                "0",
                "10",
                "0",
                BadgeType.LEVEL.toString(),
                false),
            Badge(UUID.randomUUID().toString(),
                "Reporta 10 incidencias",
                "Reporta 10 incidencias a la plataforma.",
                "0",
                "10",
                "0",
                BadgeType.INCIDENT.toString(),
                false)
        )

        return badges
    }
}

enum class BadgeType {
    INCIDENT, LEVEL, VALIDATION
}