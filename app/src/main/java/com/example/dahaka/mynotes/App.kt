package com.example.dahaka.mynotes

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by dahaka on 1/27/2018.
 */

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder().build()
        Realm.setDefaultConfiguration(config)
    }
}