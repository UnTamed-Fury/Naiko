package eu.kanade.tachiyomi.ui.setting

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class LongClickablePreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {
    var onPreferenceLongClickListener: ((Preference) -> Boolean)? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.setOnLongClickListener {
            performLongClick()
        }
    }

    private fun performLongClick(): Boolean {
        if (!isEnabled || !isSelectable) {
            return false
        }
        return onPreferenceLongClickListener?.invoke(this) ?: false
    }
}
