package world.icebear03.starlight

import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.configuration.Configuration
import world.icebear03.starlight.utils.YamlUpdater

object Config {
    val config:Configuration = YamlUpdater.loadAndUpdate("config.yml")
}