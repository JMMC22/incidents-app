package com.example.incidentsapp.models

import android.util.Log
import com.example.incidentsapp.MapsActivity

class Levels() {

    val levels = mapOf<Int,List<Int>>(
        0 to listOf(0,100),
        1 to listOf(101,200),
        2 to listOf(201,350),
        3 to listOf(351,500),
        4 to listOf(501,600),
        5 to listOf(601,750),
        6 to listOf(751,900),
        7 to listOf(901,1000),
        8 to listOf(1001,1250),
        9 to listOf(1251,1300),
        10 to listOf(1301,1500)
    )

    fun getPercentOfLevel(px: Int): Int {
        var level = checkCurrentLevelOfUser(px)
        var max = levels[level]?.get(1)!!.minus(levels[level]?.get(0)!!)
        var newPx = px.minus(levels[level]?.get(0)!!)
        return newPx * 100 / max!!
    }

    fun checkCurrentLevelOfUser(px: Int): Int {
        var currentLevel = 0
        levels.forEach { level, rangePx ->
            if (rangePx[0] <= px && px <= rangePx[1]) {
                currentLevel = level
            }
        }
        return currentLevel
    }

    companion object {
        private val TAG = Levels::class.java.simpleName
    }
}