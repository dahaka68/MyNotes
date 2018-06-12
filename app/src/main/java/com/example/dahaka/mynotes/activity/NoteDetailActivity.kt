package com.example.dahaka.mynotes.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.dahaka.mynotes.Constants.EDIT_DONE
import com.example.dahaka.mynotes.Constants.EDIT_NOTE
import com.example.dahaka.mynotes.R
import com.example.dahaka.mynotes.model.Note
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_note_details.*

class NoteDetailActivity : AppCompatActivity() {
    private var realm = Realm.getDefaultInstance()
    private val noteTimestamp: Long
        get() = intent.getLongExtra(EDIT_NOTE, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true) }
        initNote()
        save_button.setOnClickListener {
            editNote()
        }
    }

    private fun editNote() {
        realm.executeTransaction {
            val note = it.where(Note::class.java).equalTo("timestamp", noteTimestamp).findFirst()
            note?.timestamp = System.currentTimeMillis()
            note?.text = node_text.text.toString()
            note?.theme = et_theme.text.toString()
            setResult(RESULT_OK, Intent().putExtra(EDIT_NOTE, ""))
            getSharedPreferences(EDIT_NOTE, Context.MODE_PRIVATE).edit().putString(EDIT_NOTE, EDIT_DONE).apply()
        }
        finish()
    }

    private fun initNote() {
        val note = realm.where(Note::class.java).equalTo("timestamp", noteTimestamp).findFirst()
        node_text.setText(note?.text)
        et_theme.setText(note?.theme)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    public override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
