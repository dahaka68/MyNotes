package com.example.dahaka.mynotes.adapters

import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dahaka.mynotes.Constants.EDIT_NOTE
import com.example.dahaka.mynotes.R
import com.example.dahaka.mynotes.activity.NoteDetailActivity
import com.example.dahaka.mynotes.model.Note
import kotlinx.android.synthetic.main.note_item.view.*
import org.jetbrains.anko.startActivity
import java.util.*

/**
 * Created by dahaka on 1/26/2018.
 */

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.ItemViewHolder>() {
    private var noteList: List<Note> = ArrayList()
    private lateinit var listener: Listener

    fun setNoteList(listener: Listener, noteList: List<Note>) {
        this.listener = listener
        this.noteList = noteList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        val viewHolder = ItemViewHolder(view)
        view.setOnClickListener {
            val note = noteList[viewHolder.adapterPosition]
            view.context.startActivity<NoteDetailActivity>(EDIT_NOTE to note.timestamp)
        }
        view.setOnLongClickListener {
            listener.onItemLongClick(noteList[viewHolder.adapterPosition].timestamp)
            false
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindItems(noteList[position])
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(note: Note) {
            itemView.note_title.text = note.theme
            itemView.note_date.text = DateUtils.getRelativeTimeSpanString(note.timestamp)
        }
    }

    interface Listener {
        fun onItemLongClick(timeStamp: Long)
    }
}
