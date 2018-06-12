package com.example.dahaka.mynotes.activity

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import com.example.dahaka.mynotes.Constants.DATABASE
import com.example.dahaka.mynotes.Constants.EDIT_NOTE
import com.example.dahaka.mynotes.Constants.NOTE
import com.example.dahaka.mynotes.Constants.NOTE_CODE
import com.example.dahaka.mynotes.Constants.SKIP
import com.example.dahaka.mynotes.R
import com.example.dahaka.mynotes.R.string.note_deleted
import com.example.dahaka.mynotes.adapters.NoteAdapter
import com.example.dahaka.mynotes.model.Note
import com.example.dahaka.mynotes.model.NoteToFB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.util.*




class MainActivity : AppCompatActivity(), NoteAdapter.Listener, SearchView.OnQueryTextListener {
    private var noteList: ArrayList<Note> = ArrayList()
    private var realm = Realm.getDefaultInstance()
    private lateinit var adapter: NoteAdapter
    private val noteBaseReference = FirebaseDatabase.getInstance().reference.child(DATABASE).child(NOTE)
    private lateinit var scrollListener: OnScrollListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = NoteAdapter()
        initRV()
        hideFabOnScroll()
        val dataBaseReference = FirebaseDatabase.getInstance().getReference(DATABASE)
        if (FirebaseAuth.getInstance().currentUser != null)
            dataBaseReference.keepSynced(true)
        fab.setOnClickListener { startActivityForResult<AddNoteActivity>(NOTE_CODE) }
    }

    override fun onResume() {
        super.onResume()
        if (getSharedPreferences(EDIT_NOTE, Context.MODE_PRIVATE).getString(EDIT_NOTE, null) != null)
            refreshItemsFromBD()
        notes_recycler.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        getSharedPreferences(EDIT_NOTE, Context.MODE_PRIVATE).edit().putString(EDIT_NOTE, null).apply()
        notes_recycler.removeOnScrollListener(scrollListener)
    }

    private val notesFromDB: ArrayList<Note>
        get() {
            val notes: ArrayList<Note> = ArrayList()
            realm.beginTransaction()
            val results = realm.where(Note::class.java).sort("timestamp", Sort.DESCENDING).findAll()
            if (!results.isEmpty()) notes.addAll(results)
            realm.commitTransaction()
            return notes
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NOTE_CODE) {
            if (resultCode == RESULT_OK) notifyAdapter()
        }
    }

    private fun initRV() {
        notes_recycler.layoutManager = LinearLayoutManager(this)
        notes_recycler.setHasFixedSize(true)
        notes_recycler.adapter = adapter
        notifyAdapter()
    }

    private fun notifyAdapter() {
        noteList = notesFromDB
        adapter.setNoteList(this, noteList)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onItemLongClick(timeStamp: Long) {
        alert(R.string.delete_title) {
            yesButton {
                removeNote(timeStamp)
                toast(note_deleted)
            }
            noButton { }
        }.show()
    }

    private fun hideFabOnScroll() {
        scrollListener = object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> fab.show()
                    else -> fab.hide()
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        val syncUp = menu.findItem(R.id.sync)
        if (intent.getStringExtra(SKIP) != null) {
            syncUp.isVisible = false
        }
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
        val searchAutoComplete = megaSearch(searchView)
        searchAutoComplete?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (!searchView.isIconified) searchView.isIconified = true else super.onBackPressed()
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sync) {
            loadToFB()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun megaSearch(viewGroup: ViewGroup): SearchView.SearchAutoComplete? {
        for (index in 0 until viewGroup.childCount) {
            val nextChild = viewGroup.getChildAt(index)
            if (nextChild is SearchView.SearchAutoComplete) return nextChild
            if (nextChild is ViewGroup) return megaSearch(nextChild)
        }
        return null
    }

    private fun removeNote(timeStamp: Long) {
        realm.beginTransaction()
        val note = realm.where(Note::class.java).equalTo("timestamp", timeStamp).findFirst()
        note?.deleteFromRealm()
        realm.commitTransaction()
        notifyAdapter()
    }

    private fun refreshItemsFromBD() {
        notifyAdapter()
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        val searchList = noteList.filter { note ->
            note.theme
                    .toLowerCase().trim { it <= ' ' }
                    .contains(newText.toLowerCase().trim { it <= ' ' })
        }
        adapter.setNoteList(this, searchList)
        return true
    }

    //Synchronization with Firebase now works only in one direction. There was not enough time for full synchronization.
    private fun loadToFB() {
        val notes = ArrayList<Note>()
        notes.addAll(notesFromDB)
        notes
                .map { NoteToFB(it.text, it.timestamp, it.theme) }
                .forEach { noteBaseReference.child(it.date.toString()).setValue(it) }
    }
}
