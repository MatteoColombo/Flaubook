package it.speedcubing.flaubook.filetools

import java.io.File

fun deleteFile(file: File? = null, path: String? = null) {
    file?.deleteRecursively()
    path?.apply { File(path).deleteRecursively() }
}