package it.speedcubing.flaubook.adapter


import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import es.claucookie.miniequalizerlibrary.EqualizerView
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.tools.timeToString
import java.util.*

class BLAdapter(
    private val books: List<Book>,
    private val playing: UUID? = null,
    private val clickListener: ((Book, Boolean) -> Unit)? = null
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        if (books.isEmpty()) return -1
        if (books[position].id == playing) return 2
        return when (books[position].listened) {
            0 -> 0
            else -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> BLNewBook(inflater.inflate(R.layout.bl_item_no_progress, parent, false))
            1 -> BLProgress(inflater.inflate(R.layout.bl_item_layout, parent, false))
            2 -> BLPlaying(inflater.inflate(R.layout.bl_item_playing, parent, false))
            else -> BLEmpty(inflater.inflate(R.layout.bl_empty, parent, false))
        }
    }


    override fun getItemCount(): Int = if (books.isEmpty()) 1 else books.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (books.isNotEmpty()) {
            (holder as BLViewHolder).bind(books[position], clickListener!!)
        }
    }

    open class BLViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {
        private val image: ImageView = itemView.findViewById(R.id.bl_item_miniature)
        private val author: TextView = itemView.findViewById(R.id.bl_item_author)
        private val title: TextView = itemView.findViewById(R.id.bl_item_title)
        private val readBy: TextView = itemView.findViewById(R.id.bl_item_read_by)
        protected val remaining: TextView = itemView.findViewById(R.id.bl_item_remaining)


        open fun bind(book: Book, clicklistener: (Book, Boolean) -> Unit) {
            image.setImageBitmap(BitmapFactory.decodeFile(book.picture))
            title.text = book.title
            author.text = itemView.context.getString(R.string.by, book.author)
            readBy.text = itemView.context.getString(R.string.read_by, book.readBy)
            itemView.setOnClickListener { clicklistener(book, false) }
            itemView.setOnLongClickListener {
                clicklistener(book, true)
                true
            }
            remaining.text = book.strLen
        }

    }


    class BLNewBook(view: View) : BLViewHolder(view)

    class BLProgress(view: View) : BLViewHolder(view) {

        private val progress: ProgressBar = itemView.findViewById(R.id.bl_item_progress)

        override fun bind(book: Book, clicklistener: (Book, Boolean) -> Unit) {
            super.bind(book, clicklistener)
            progress.max = book.len
            progress.progress = book.listened
            remaining.text = when (book.listened) {
                book.len -> itemView.context.getString(R.string.finished)
                else -> itemView.context.getString(
                    R.string.remaining_time,
                    timeToString(book.len - book.listened)
                )
            }
        }
    }

    class BLPlaying(view: View) : BLViewHolder(view) {

        private val equalizerView: EqualizerView = itemView.findViewById(R.id.bl_item_equalizer)

        override fun bind(book: Book, clicklistener: (Book, Boolean) -> Unit) {
            super.bind(book, clicklistener)
            equalizerView.animateBars()
        }
    }

    class BLEmpty(view: View) :
        RecyclerView.ViewHolder(view)


}