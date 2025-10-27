package yokai.presentation.component.recyclerview

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Add (vertical) padding to RecyclerView first and last item. Because `clipToPadding = "false"` bugged out for Bottom Sheets.
 */
class VertPaddingDecoration(private val padding: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        if (itemPosition == RecyclerView.NO_POSITION) return;

        when {
            itemPosition == 0 ->
                outRect.top = padding
            itemPosition > 0 && itemPosition == state.itemCount - 1 ->
                outRect.bottom = padding
        }
    }
}
