package io.github.keithlobo07.edinburghairport

expect fun getEdinburghCurrentTime(): String

expect fun getAeroApiTimeRange(): Pair<String, String>

expect suspend fun fetchHttpUrl(url: String): String?

expect object PlatformStorage {
    fun save(key: String, value: String)
    fun load(key: String): String?
}
