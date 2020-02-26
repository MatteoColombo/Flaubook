package it.speedcubing.flaubook.adapter


import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.tools.timeToString

class BLAdapter(private val books: List<Book>, private val clickListener: (Book, Boolean) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (books.isEmpty()) {
            val view = inflater.inflate(R.layout.bl_empty, parent, false)
            return BLEmptyViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.bl_item_layout, parent, false)
            return BLViewHolder(view)
        }
    }

    override fun getItemCount(): Int = if (books.isEmpty()) 1 else books.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (books.isNotEmpty()) {
            (holder as BLViewHolder).bind(books[position], clickListener)
        }
    }

    class BLViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        private val image: ImageView = itemView.findViewById(R.id.bl_item_miniature)
        private val author: TextView = itemView.findViewById(R.id.bl_item_author)
        private val title: TextView = itemView.findViewById(R.id.bl_item_title)
        private val readBy: TextView = itemView.findViewById(R.id.bl_item_read_by)
        private val remaining: TextView = itemView.findViewById(R.id.bl_item_remaining)
        private val progress: ProgressBar = itemView.findViewById(R.id.bl_item_progress)

        fun bind(book: Book, clicklistener: (Book, Boolean) -> Unit) {
            image.setImageBitmap(BitmapFactory.decodeFile(book.picture))
            title.text = book.title
            author.text = itemView.context.getString(R.string.by, book.author)
            readBy.text = itemView.context.getString(R.string.read_by, book.readBy)

            val progressPerc = book.listened.toDouble() / book.len * 100
            when {
                book.listened == 0 -> {
                    remaining.text = timeToString(book.len - book.listened)
                    progress.visibility = View.GONE
                }
                book.listened == book.len -> {
                    progress.progress = 100
                    remaining.text = itemView.context.getString(R.string.finished)
                }
                else -> {
                    remaining.text = itemView.context.getString(
                        R.string.remaining_time,
                        timeToString(book.len - book.listened)
                    )
                    progress.progress = progressPerc.toInt()
                }
            }

            itemView.setOnClickListener { clicklistener(book, false) }
            itemView.setOnLongClickListener {
                clicklistener(book, true)
                true
            }
        }

    }

    class BLEmptyViewHolder(view: View) :
        RecyclerView.ViewHolder(view)


}