package yokai.core.migration

import uy.kohesive.injekt.Injekt

class MigrationContext(val dryRun: Boolean) {

    inline fun <reified T> get(): T? {
        return Injekt.getInstanceOrNull(T::class.java)
    }
}
