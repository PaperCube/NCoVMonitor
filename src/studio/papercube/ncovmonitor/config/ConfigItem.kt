package studio.papercube.ncovmonitor.config

import java.io.File

open class ConfigItem(val name: String) {
    companion object {
        const val parentDir: String = "./.ncovmon"
    }

    private val file = File(parentDir, name)

    fun get(): String {
        return file.readText().trim()
    }

    fun getOrNull(): String? {
        try {
            if (!file.exists() || !file.canRead()) return null
            return get()
        } catch (e: Exception) {
            return null
        }
    }
}