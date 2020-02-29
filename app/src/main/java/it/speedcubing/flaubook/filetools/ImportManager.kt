package it.speedcubing.flaubook.filetools

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.speedcubing.flaubook.R

private const val TAG = "FLAUBOOK"
private const val PERMISSION_REQUEST_CODE: Int = 3828
private const val PICKER_REQUEST_CODE: Int = 3828

class ImportManager(val context: Context) {

    fun start() {
        if (checkPermissions()) {
            openFileExp()
        } else {
            requestPermissions()
        }
    }

    fun gotPermission() {
        openFileExp()
    }

    private fun checkPermissions(): Boolean =
        context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() =
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )

    private fun openFileExp() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        (context as Activity).startActivityForResult(intent, PICKER_REQUEST_CODE)
    }

    fun handleFile(data: Intent) {
        (context as Activity).layoutInflater
        val dialog = Dialog(context).apply {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            setContentView(R.layout.import_dialog)
        }
        dialog.show()
        data.data?.also { uri ->
            Thread(Runnable {
                var fullPath =
                    FileUtils.getPath(uri, context) ?: FileUtils.getUriFromRemote(context, uri)

                if (fullPath != null) {
                    var success = false
                    try {
                        success = importZip(fullPath, context)
                    } catch (e: Exception) {
                        e.message?.run {
                            Log.e(TAG, this)
                        }
                    } finally {
                        dialog.dismiss()
                    }
                    if (!success) {

                        context.getString(R.string.error)
                        showErrorDialog(
                            context.getString(R.string.zip_load_error),
                            context.getString(R.string.error)
                        )
                    }
                } else {
                    dialog.dismiss()
                    showErrorDialog(
                        context.getString(R.string.zip_load_error),
                        context.getString(R.string.error)
                    )
                }
            }).start()
        }
    }

    private fun showErrorDialog(message: String, title: String) {
        (context as Activity).runOnUiThread {
            val dialog = MaterialAlertDialogBuilder(context).apply {
                setMessage(message)
                setTitle(title)
                setPositiveButton(context.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            }.create()
            dialog.show()
        }
    }

}