package org.obywatelgcc.timelogger.core.cache


import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

interface Cache<K, V> {
    fun get(key: K): V?
    fun contains(key: K): Boolean

    fun put(key: K, value: V)
    fun computeIfAbsent(key: K, mappingFunction: (K) -> V): V
}

class TimedCache<K, V>() : Cache<K, V> {
    private var cacheTimeValidityInMillis: Long = TimeUnit.DAYS.toMillis(1L)
    private val hashMap = ConcurrentHashMap<K, TimedEntry<V>>()

    companion object {
        fun <K, V> expiringEvery(duration: Long, timeUnit: TimeUnit) =
            TimedCache<K, V>().apply {
                cacheTimeValidityInMillis = timeUnit.toMillis(duration)
            }

        fun <K, V> expiringEveryMinutes(duration: Long): TimedCache<K, V> = expiringEvery(duration, TimeUnit.MINUTES)
    }

    override fun get(key: K): V? {
        return hashMap[key]?.let { timedEntry ->
            when {
                timedEntry.isExpired() -> {
                    hashMap.remove(key)
                    null
                }

                else -> timedEntry.value
            }
        }
    }

    override fun contains(key: K): Boolean {
        return hashMap.containsKey(key)
    }

    override fun put(key: K, value: V) {
        hashMap[key] = TimedEntry(value, cacheTimeValidityInMillis)
    }

    override fun computeIfAbsent(key: K, mappingFunction: (K) -> V): V {
        return get(key) ?: mappingFunction(key).also { put(key, it) }
    }

    data class TimedEntry<V>(val value: V, val maxDurationInMillis: Long) {
        private val expirationTime: Long = now() + maxDurationInMillis

        fun isExpired() = expirationTime < now()

        private fun now() = System.currentTimeMillis()
    }
}
