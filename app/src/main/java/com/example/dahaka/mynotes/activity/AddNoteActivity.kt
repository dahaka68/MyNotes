package com.example.dahaka.mynotes.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.dahaka.mynotes.Constants.NEW_NOTE
import com.example.dahaka.mynotes.R
import com.example.dahaka.mynotes.model.Note
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_note_details.*

class AddNoteActivity : AppCompatActivity() {
    private val realm: Realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_details)
        supportActionBar?.apply { setDisplayHomeAsUpEnabled(true) }
        save_button.setOnClickListener {
            addNewNote()
        }
    }

    private fun addNewNote() {
        realm.beginTransaction()
        val note = Note()
        note.theme = et_theme.text.toString()
        note.text = node_text.text.toString()
        note.timestamp = System.currentTimeMillis()
        realm.insert(note)
        realm.commitTransaction()
        setResult(Activity.RESULT_OK, Intent().putExtra(NEW_NOTE, "new"))
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED, Intent().putExtra(NEW_NOTE, ""))
        onBackPressed()
        return true
    }

    public override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }
}
