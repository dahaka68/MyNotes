package com.example.dahaka.mynotes

import android.app.Activity
import android.content.Context
import android.content.Intent

inline fun <reified T : Activity> Context?.startActivity() = this?.startActivity(Intent(this, T::class.java))