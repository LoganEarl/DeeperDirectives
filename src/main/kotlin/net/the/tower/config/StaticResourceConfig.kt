package net.the.tower.config

import io.smallrye.config.ConfigMapping

@ConfigMapping(prefix = "deeper.static")
interface StaticResourceConfig {
    fun path(): String
}