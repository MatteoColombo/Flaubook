package it.speedcubing.flaubook.interfaces

import java.util.*

interface FragmentClick {

    fun showChapters(id: String, position: Int)

    fun bookSelected(id: UUID)
}