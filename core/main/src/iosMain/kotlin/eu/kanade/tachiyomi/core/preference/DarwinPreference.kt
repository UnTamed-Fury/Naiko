package eu.kanade.tachiyomi.core.preference

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDefaultsDidChangeNotification

sealed class DarwinPreference<T>(
    private val userDefaults: NSUserDefaults,
    private val key: String,
    private val defaultValue: T,
) : Preference<T> {

    private fun flowForKey(key: String) = callbackFlow {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = NSUserDefaultsDidChangeNotification,
            `object` = userDefaults,
            queue = null,
        ) { _: NSNotification? ->
            trySend(key)
        }
        awaitClose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
        }
    }

    abstract fun read(userDefaults: NSUserDefaults, key: String, defaultValue: T): T

    override fun key(): String {
        return key
    }

    override fun get(): T {
        return try {
            read(userDefaults, key, defaultValue)
        } catch (e: ClassCastException) {
            Logger.i { "Invalid value for $key; deleting" }
            delete()
            defaultValue
        }
    }

    override fun set(value: T) {
        userDefaults.setObject(value, key)
    }

    override fun isSet(): Boolean {
        return userDefaults.objectForKey(key) != null
    }

    override fun delete() {
        userDefaults.removeObjectForKey(key)
    }

    override fun defaultValue(): T {
        return defaultValue
    }

    override fun changes(): Flow<T> {
        return flowForKey(key)
            .onStart { emit("ignition") }
            .map { get() }
            .conflate()
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return changes().stateIn(scope, SharingStarted.Eagerly, get())
    }

    class StringPrimitive(
        userDefaults: NSUserDefaults,
        key: String,
        defaultValue: String,
    ) : DarwinPreference<String>(userDefaults, key, defaultValue) {
        override fun read(
            userDefaults: NSUserDefaults,
            key: String,
            defaultValue: String,
        ): String {
            return userDefaults.stringForKey(key) ?: defaultValue
        }
    }

    class LongPrimitive(
        userDefaults: NSUserDefaults,
        key: String,
        defaultValue: Long,
    ) : DarwinPreference<Long>(userDefaults, key, defaultValue) {
        override fun read(userDefaults: NSUserDefaults, key: String, defaultValue: Long): Long {
            return userDefaults.objectForKey(key) as? Long ?: defaultValue
        }
    }

    class IntPrimitive(
        userDefaults: NSUserDefaults,
        key: String,
        defaultValue: Int,
    ) : DarwinPreference<Int>(userDefaults, key, defaultValue) {
        override fun read(userDefaults: NSUserDefaults, key: String, defaultValue: Int): Int {
            return userDefaults.objectForKey(key) as? Int ?: defaultValue
        }
    }

    class FloatPrimitive(
        userDefaults: NSUserDefaults,
        key: String,
        defaultValue: Float,
    ) : DarwinPreference<Float>(userDefaults, key, defaultValue) {
        override fun read(userDefaults: NSUserDefaults, key: String, defaultValue: Float): Float {
            return userDefaults.objectForKey(key) as? Float ?: defaultValue
        }
    }

    class BooleanPrimitive(
        userDefaults: NSUserDefaults,
        key: String,
        defaultValue: Boolean,
    ) : DarwinPreference<Boolean>(userDefaults, key, defaultValue) {
        override fun read(
            userDefaults: NSUserDefaults,
            key: String,
            defaultValue: Boolean,
        ): Boolean {
            return userDefaults.objectForKey(key) as? Boolean ?: defaultValue
        }
    }

    class StringSetPrimitive(
        userDefaults: NSUserDefaults,
        key: String,
        defaultValue: Set<String>,
    ) : DarwinPreference<Set<String>>(userDefaults, key, defaultValue) {
        override fun read(
            userDefaults: NSUserDefaults,
            key: String,
            defaultValue: Set<String>,
        ): Set<String> {
            return userDefaults.stringArrayForKey(key) as? Set<String>? ?: defaultValue
        }
    }

    class Object<T>(
        userDefaults: NSUserDefaults,
        key: String,
        defaultValue: T,
        val serializer: (T) -> String,
        val deserializer: (String) -> T,
    ) : DarwinPreference<T>(userDefaults, key, defaultValue) {
        override fun read(userDefaults: NSUserDefaults, key: String, defaultValue: T): T {
            return try {
                userDefaults.stringForKey(key)?.let(deserializer) ?: defaultValue
            } catch (e: Exception) {
                defaultValue
            }
        }
    }
}
