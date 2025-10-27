package eu.kanade.tachiyomi.util.system

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.databinding.CustomDialogTitleMessageBinding
import eu.kanade.tachiyomi.databinding.DialogQuadstateBinding
import eu.kanade.tachiyomi.databinding.DialogTextInputBinding
import eu.kanade.tachiyomi.widget.TriStateCheckBox
import eu.kanade.tachiyomi.widget.materialdialogs.TriStateMultiChoiceDialogAdapter
import eu.kanade.tachiyomi.widget.materialdialogs.TriStateMultiChoiceListener
import yokai.util.lang.getString

fun Context.materialAlertDialog() = MaterialAlertDialogBuilder(withOriginalWidth())

fun MaterialAlertDialogBuilder.addCheckBoxPrompt(
    stringRes: StringResource,
    isChecked: Boolean = false,
    listener: MaterialAlertDialogBuilderOnCheckClickListener? = null,
): MaterialAlertDialogBuilder {
    return addCheckBoxPrompt(context.getString(stringRes), isChecked, listener)
}

fun MaterialAlertDialogBuilder.addCheckBoxPrompt(
    text: CharSequence,
    isChecked: Boolean = false,
    listener: MaterialAlertDialogBuilderOnCheckClickListener? = null,
): MaterialAlertDialogBuilder {
    return setMultiChoiceItems(
        arrayOf(text),
        booleanArrayOf(isChecked),
    ) { dialog, _, checked ->
        listener?.onClick(dialog, checked)
    }
}

fun AlertDialog.disableItems(items: Array<String>) {
    val listView = listView ?: return
    listView.setOnHierarchyChangeListener(
        object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View) {
                val text = (child as? AppCompatCheckedTextView)?.text ?: return
                if (items.contains(text)) {
                    child.setOnClickListener(null)
                    child.isEnabled = false
                } else {
                    child.isEnabled = true
                }
            }

            override fun onChildViewRemoved(view: View?, view1: View?) {}
        },
    )
}

fun MaterialAlertDialogBuilder.setCustomTitleAndMessage(title: StringResource, message: String): MaterialAlertDialogBuilder {
    return setCustomTitle(
        (CustomDialogTitleMessageBinding.inflate(LayoutInflater.from(context))).apply {
            if (title.resourceId != 0) {
                alertTitle.text = context.getString(title)
            } else {
                alertTitle.isVisible = false
            }
            this.message.text = message
        }.root,
    )
}

fun MaterialAlertDialogBuilder.setCustomTitleAndMessage(title: Int, message: String): MaterialAlertDialogBuilder {
    return setCustomTitle(
        (CustomDialogTitleMessageBinding.inflate(LayoutInflater.from(context))).apply {
            if (title != 0) {
                alertTitle.text = context.getString(title)
            } else {
                alertTitle.isVisible = false
            }
            this.message.text = message
        }.root,
    )
}

/**
 * A variant of listItemsMultiChoice that allows for checkboxes that supports 3 states instead.
 */
@CheckResult
internal fun MaterialAlertDialogBuilder.setTriStateItems(
    message: String? = null,
    items: List<CharSequence>,
    disabledIndices: IntArray? = null,
    initialSelection: IntArray = IntArray(items.size),
    skipChecked: Boolean = false,
    selection: TriStateMultiChoiceListener?,
): MaterialAlertDialogBuilder {
    val binding = DialogQuadstateBinding.inflate(LayoutInflater.from(context))
    binding.list.layoutManager = LinearLayoutManager(context)
    binding.list.adapter = TriStateMultiChoiceDialogAdapter(
        dialog = this,
        items = items,
        disabledItems = disabledIndices,
        initialSelection = initialSelection,
        skipChecked = skipChecked,
        listener = selection,
    )
    val updateScrollIndicators = {
        binding.scrollIndicatorUp.isVisible = binding.list.canScrollVertically(-1)
        binding.scrollIndicatorDown.isVisible = binding.list.canScrollVertically(1)
    }
    binding.list.setOnScrollChangeListener { _, _, _, _, _ ->
        updateScrollIndicators()
    }
    binding.list.post {
        updateScrollIndicators()
    }

    if (message != null) {
        binding.message.text = message
        binding.message.isVisible = true
    }
    return setView(binding.root)
}

internal fun MaterialAlertDialogBuilder.setNegativeStateItems(
    items: List<CharSequence>,
    initialSelection: BooleanArray = BooleanArray(items.size),
    listener: DialogInterface.OnMultiChoiceClickListener,
): MaterialAlertDialogBuilder {
    return setTriStateItems(
        items = items,
        initialSelection = initialSelection.map {
            if (it) {
                TriStateCheckBox.State.IGNORE.ordinal
            } else {
                TriStateCheckBox.State.UNCHECKED.ordinal
            }
        }
            .toIntArray(),
        skipChecked = true,
    ) { _, _, _, index, state ->
        listener.onClick(null, index, state == TriStateCheckBox.State.IGNORE.ordinal)
    }
}

val DialogInterface.isPromptChecked: Boolean
    get() = (this as? AlertDialog)?.listView?.isItemChecked(0) ?: false

fun interface MaterialAlertDialogBuilderOnCheckClickListener {
    fun onClick(var1: DialogInterface?, var3: Boolean)
}

fun MaterialAlertDialogBuilder.setTextInput(
    hint: String? = null,
    prefill: String? = null,
    onTextChanged: (String) -> Unit,
): MaterialAlertDialogBuilder {
    val binding = DialogTextInputBinding.inflate(LayoutInflater.from(context))
    binding.textField.hint = hint
    binding.textField.editText?.apply {
        setText(prefill, TextView.BufferType.EDITABLE)
        doAfterTextChanged {
            onTextChanged(it?.toString() ?: "")
        }
        post {
            requestFocusFromTouch()
            context.getSystemService<InputMethodManager>()?.showSoftInput(this, 0)
        }
    }
    return setView(binding.root)
}
