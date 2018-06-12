package com.example.dahaka.mynotes.model

import io.realm.RealmObject

/**
 * Created by dahaka on 1/26/2018.
 */

open class Note : RealmObject() {
    lateinit var text: String
    lateinit var theme: String
    var timestamp: Long = 0
}