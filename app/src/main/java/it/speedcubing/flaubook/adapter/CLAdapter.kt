package it.speedcubing.flaubook.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.database.Chapter
import it.speedcubing.flaubook.tools.timeToStringShort

class CLAdapter(
    private val chapters: List<Chapter>, private val selected: Int, private val click: (Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CLViewHolder(inflater.inflate(R.layout.cl_item_layout, parent, false))
    }

    override fun getItemCount(): Int = chapters.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as CLViewHolder).bind(chapters[position], position == selected, click, position)
    }

    class CLViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val title = itemView.findViewById<TextView>(R.id.cl_title)
        private val time = itemView.findViewById<TextView>(R.id.cl_time)
        private val imageView = itemView.findViewById<ImageView>(R.id.cl_current_dot)


        fun bind(chapter: Chapter, selected: Boolean, click: (Int) -> Unit, position: Int) {
            title.text = chapter.title
            time.text = timeToStringShort(chapter.len)
            when (selected) {
                true -> imageView.visibility = View.VISIBLE
                false -> imageView.visibility = View.GONE
            }
            itemView.setOnClickListener {
                click(position)
            }
        }

    }

}