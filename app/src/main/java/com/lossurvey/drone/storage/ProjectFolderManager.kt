package com.lossurvey.drone.storage

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectFolderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val root: File
        get() = context.getExternalFilesDir(null) ?: context.filesDir

    fun createProjectFolder(missionName: String): String {
        val sanitized = sanitize(missionName)
        val folder = File(root, "Projects/$sanitized")
        folder.mkdirs()
        File(folder, "images").mkdirs()
        return folder.absolutePath
    }

    fun getImagesFolder(projectPath: String): File =
        File(projectPath, "images").also { it.mkdirs() }

    fun saveInputFile(uri: Uri, projectPath: String, suggestedName: String): String {
        val safeName = sanitize(suggestedName.ifBlank { "survey_input" })
        val ext = suggestedName.substringAfterLast('.', "csv").lowercase()
        val dest = File(projectPath, "$safeName.$ext")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { out -> input.copyTo(out) }
        }
        return dest.absolutePath
    }

    fun listProjectFolders(): List<File> =
        File(root, "Projects").listFiles()?.toList().orEmpty()

    private fun sanitize(name: String): String =
        name.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_").ifEmpty { "mission" }
}
