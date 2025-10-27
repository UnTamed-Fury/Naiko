package eu.kanade.tachiyomi.core.preference

import eu.kanade.tachiyomi.core.preference.DarwinPreference.*
import platform.Foundation.NSUserDefaults

class DarwinPreferenceStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults(),
) : PreferenceStore {

    override fun getString(key: String, defaultValue: String): Preference<String> {
        return StringPrimitive(userDefaults, key, defaultValue)
    }

    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return LongPrimitive(userDefaults, key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return IntPrimitive(userDefaults, key, defaultValue)
    }

    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return FloatPrimitive(userDefaults, key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return BooleanPrimitive(userDefaults, key, defaultValue)
    }

    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return StringSetPrimitive(userDefaults, key, defaultValue)
    }

    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T,
    ): Preference<T> {
        return Object(
            userDefaults = userDefaults,
            key = key,
            defaultValue = defaultValue,
            serializer = serializer,
            deserializer = deserializer,
        )
    }

    override fun getAll(): Map<String, *> {
        return userDefaults.dictionaryRepresentation() as? Map<String, *> ?: emptyMap<String, Any>()
    }
}
