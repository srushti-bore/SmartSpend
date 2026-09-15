package com.smartspend.app.core.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File

object BackupStorageHelper {

    private const val CHANNEL_ID = "smartspend_backup_downloads"
    private const val CHANNEL_NAME = "Backup & Downloads"
    private const val NOTIFICATION_ID = 4001

    /**
     * Writes sourceFile directly to a user-selected SAF Document Uri (via ActivityResultContracts.CreateDocument).
     */
    fun saveToUri(context: Context, sourceFile: File, destinationUri: Uri): Result<Unit> {
        return runCatching {
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("Failed to open destination stream for backup")

            notifyDownloadComplete(context, destinationUri, sourceFile.name)
        }
    }

    /**
     * Copies sourceFile into the public Downloads directory so the user can easily find it in their Files/Downloads app.
     * Returns the user-friendly path of where it was saved.
     */
    fun saveToDownloads(context: Context, sourceFile: File, displayName: String): Result<String> {
        return runCatching {
            var savedUri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: resolver.insert(MediaStore.Files.getContentUri("external"), values)
                    ?: throw IllegalStateException("Failed to create entry in Downloads MediaStore")

                savedUri = uri

                resolver.openOutputStream(uri)?.use { out ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                } ?: throw IllegalStateException("Failed to write backup stream to Downloads")

                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                notifyDownloadComplete(context, uri, displayName)
                "Downloads/$displayName"
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, displayName)
                sourceFile.copyTo(targetFile, overwrite = true)

                val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
                notifyDownloadComplete(context, fileUri, displayName)
                "Downloads/$displayName"
            }
        }
    }

    /**
     * Opens Android Share Sheet for the backup file using FileProvider.
     */
    fun shareFile(context: Context, file: File, chooserTitle: String = "Share SmartSpend Backup"): Result<Unit> {
        return runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    private fun notifyDownloadComplete(context: Context, uri: Uri, fileName: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for downloaded backup files"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                viewIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("SmartSpend Backup Downloaded")
                .setContentText("$fileName is ready in your Downloads")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Timber.w(e, "Failed to post backup download notification")
        }
    }
}
