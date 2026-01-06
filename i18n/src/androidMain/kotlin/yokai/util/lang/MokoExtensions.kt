package yokai.util.lang

import android.content.Context
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.Plural
import dev.icerock.moko.resources.desc.PluralFormatted
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

fun Context.getString(stringRes: StringResource) =
    StringDesc.Resource(stringRes).toString(this)

fun Context.getString(stringRes: StringResource, vararg args: Any) =
    StringDesc.ResourceFormatted(stringRes, *args).toString(this)

fun Context.getString(stringPlural: PluralsResource, quantity: Int) =
    StringDesc.Plural(stringPlural, quantity).toString(this)

fun Context.getString(stringPlural: PluralsResource, quantity: Int, vararg args: Any) =
    StringDesc.PluralFormatted(stringPlural, quantity, *args).toString(this)
