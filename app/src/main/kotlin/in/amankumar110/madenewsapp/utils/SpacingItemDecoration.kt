package `in`.amankumar110.madenewsapp.utils

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(
    context: Context,
    dpSpacing: Int,
    private val includeEdgeSpacing: Boolean = false,
    private val isGridLikeLayout: Boolean = false // new flag for grid/flex layout
) : RecyclerView.ItemDecoration() {

    private val spacingPx = dpToPx(context, dpSpacing)

    init {
        Log.v("Spacing", "Initialized with spacing: $spacingPx px, edge: $includeEdgeSpacing, grid-like: $isGridLikeLayout")
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: return

        if (position == RecyclerView.NO_POSITION) return

        val layoutManager = parent.layoutManager
        val isHorizontal = layoutManager?.canScrollHorizontally() == true
        val isVertical = layoutManager?.canScrollVertically() == true

        if (isGridLikeLayout) {
            // Use spacing on all sides for FlexboxLayoutManager or grid-like layouts
            outRect.top = spacingPx / 2
            outRect.bottom = spacingPx / 2
            outRect.left = spacingPx / 2
            outRect.right = spacingPx / 2

            if (includeEdgeSpacing) {
                // Add full spacing to edges
                if (position == 0) {
                    outRect.left = spacingPx
                    outRect.top = spacingPx
                }
                if (position == itemCount - 1) {
                    outRect.right = spacingPx
                    outRect.bottom = spacingPx
                }
            }
        } else {
            // Standard vertical/horizontal list
            if (isHorizontal) {
                if (includeEdgeSpacing) {
                    outRect.left = if (position == 0) spacingPx else spacingPx / 2
                    outRect.right = if (position == itemCount - 1) spacingPx else spacingPx / 2
                } else {
                    if (position < itemCount - 1) {
                        outRect.right = spacingPx
                    }
                }
            } else {
                if (includeEdgeSpacing) {
                    outRect.top = if (position == 0) spacingPx else spacingPx / 2
                    outRect.bottom = if (position == itemCount - 1) spacingPx else spacingPx / 2
                } else {
                    if (position < itemCount - 1) {
                        outRect.bottom = spacingPx
                    }
                }
            }
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
