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
                val id = config.getString("id")!!
                val tag = Tag(
                    id,
                    config.getString("display")!!,
                    config.getStringList("description"),
                    config.getString("skull")!!,
                    config.getString("owner"),
                    config.getString("activity")
                )
                tags[id] = tag
            }
        }

        SpecialTagGiver.initialize()
    }

    fun reloadAllTags() {
        tags.clear()
        initialize()
    }

    fun getTag(id: String): Tag? {
        return tags[id]
    }
}