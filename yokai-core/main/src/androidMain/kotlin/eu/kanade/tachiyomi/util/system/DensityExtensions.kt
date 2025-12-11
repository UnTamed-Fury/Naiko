package eu.kanade.tachiyomi.util.system

import android.content.res.Resources
import android.util.TypedValue

/**
 * Converts to dp.
 */
val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Float.pxToDp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)

/**
 * Converts to px.
 */
val Int.dpToPx: Int
    get() = this.toFloat().dpToPx.toInt()

val Int.spToPx: Int
    get() = this.toFloat().spToPx.toInt()

val Float.spToPx: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

val Float.dpToPx: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
