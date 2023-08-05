package world.icebear03.starlight.tag

import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import java.io.File

object TagLibrary {

    val tags = mutableMapOf<String, Tag>()

    fun initialize() {
        val directory = File(getDataFolder().absolutePath + "/tag/tags/")
        if (!directory.exists())
            directory.mkdirs()
        directory.listFiles()?.let {
            it.forEach { file ->
                val config = Configuration.loadFromFile(file)
                val name = config.getString("name")!!
                val tag = Tag(
                    name,
                    config.getString("color")!!,
                    config.getStringList("description"),
                    config.getString("skull")!!
                )
                tags[name] = tag
            }
        }
    }

    fun reloadAllTags() {
        tags.clear()
        initialize()
    }

    fun getTag(name: String): Tag? {
        return tags[name]
    }
}