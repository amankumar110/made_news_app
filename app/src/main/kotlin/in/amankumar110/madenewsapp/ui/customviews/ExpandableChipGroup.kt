package `in`.amankumar110.madenewsapp.ui.customviews

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import `in`.amankumar110.madenewsapp.databinding.ExpandableChipViewLayoutBinding
import `in`.amankumar110.madenewsapp.utils.SpacingItemDecoration

class ExpandableChipGroup(
    context: Context,
    attrs: AttributeSet
) : RecyclerView(context, attrs) {

    val expandableInfo = mutableListOf<ExpandableInfo>()
    private val expandableAdapter = ExpandableChipGroupAdapter(expandableInfo)
    private var selectedPosition = -1
    private var onChipClickListener: OnChipClickListener? = null

    // Custom click listener interface
    fun interface OnChipClickListener {
        fun onClick(expandableInfo: ExpandableInfo?)
    }

    init {
        this.adapter = expandableAdapter
        this.layoutManager = FlexboxLayoutManager(this.context).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }

        this.addItemDecoration(SpacingItemDecoration(context,10,
            includeEdgeSpacing = false,
            isGridLikeLayout = true
        ))
    }

    fun setChips(data: List<ExpandableInfo>) {
        expandableInfo.clear()
        expandableInfo.addAll(data)
        selectedPosition = -1
        expandableAdapter.notifyDataSetChanged()
    }

    /**
     * Programmatically select a chip by ExpandableInfo object
     * @param info The ExpandableInfo to select, or null to clear selection
     */
    fun setSelectedInfo(info: ExpandableInfo?) {
        val newPosition = if (info == null) {
            -1
        } else {
            expandableInfo.indexOfFirst { it == info }
        }

        if (newPosition == selectedPosition) return // Already selected or not found

        val previousSelection = selectedPosition
        selectedPosition = newPosition

        // Handle selection change with animations
        when {
            // Case 1: Clear selection
            newPosition == -1 && previousSelection != -1 -> {
                updateDescriptionVisibility(previousSelection, false)
                animateAllItemsToAlpha(1f)
            }

            // Case 2: First selection or switch selection
            newPosition != -1 -> {
                if (previousSelection != -1) {
                    // Hide previous selection
                    updateDescriptionVisibility(previousSelection, false)
                    animateItemAtPosition(previousSelection, 0.5f)
                } else {
                    // First selection - animate others to 0.5f
                    animateOtherItemsToAlpha(newPosition, 0.5f)
                }

                // Show new selection
                updateDescriptionVisibility(newPosition, true)
                animateItemAtPosition(newPosition, 1f)
            }
        }
    }

    /**
     * Set click listener for chip clicks
     * @param listener Lambda that receives the clicked ExpandableInfo or null if deselected
     */
    fun setOnChipCLickListener(listener: OnChipClickListener?) {
        this.onChipClickListener = listener
    }

    /**
     * Get currently selected ExpandableInfo
     * @return Selected ExpandableInfo or null if nothing is selected
     */
    fun getSelectedInfo(): ExpandableInfo? {
        return if (selectedPosition >= 0 && selectedPosition < expandableInfo.size) {
            expandableInfo[selectedPosition]
        } else {
            null
        }
    }

    // Helper functions for programmatic selection
    private fun updateDescriptionVisibility(position: Int, isVisible: Boolean) {
        val viewHolder = findViewHolderForAdapterPosition(position) as? ExpandableChipViewHolder
        viewHolder?.binding?.tvDescription?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun animateItemAtPosition(position: Int, targetAlpha: Float) {
        val viewHolder = findViewHolderForAdapterPosition(position) as? ExpandableChipViewHolder
        viewHolder?.binding?.root?.let { view ->
            if (view.alpha != targetAlpha) {
                view.animate()
                    .alpha(targetAlpha)
                    .setDuration(300)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun animateOtherItemsToAlpha(excludePosition: Int, targetAlpha: Float) {
        for (i in 0 until expandableInfo.size) {
            if (i != excludePosition) {
                animateItemAtPosition(i, targetAlpha)
            }
        }
    }

    private fun animateAllItemsToAlpha(targetAlpha: Float) {
        for (i in 0 until expandableInfo.size) {
            animateItemAtPosition(i, targetAlpha)
        }
    }

    data class ExpandableInfo(val label: String, val description: String, val colorResId: Int)

    private inner class ExpandableChipGroupAdapter(
        private val items: List<ExpandableInfo>
    ) : RecyclerView.Adapter<ExpandableChipViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandableChipViewHolder {
            val binding = ExpandableChipViewLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ExpandableChipViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ExpandableChipViewHolder, position: Int) {

            val info = items[position]
            val isSelected = position == selectedPosition

            with(holder.binding) {
                tvTitle.text = info.label
                tvDescription.text = info.description
                root.setCardBackgroundColor(context.getColor(info.colorResId))

                // Description visibility
                tvDescription.visibility = if (isSelected) View.VISIBLE else View.GONE

                // Smart Alpha Animation - only animate when alpha actually changes
                val targetAlpha = if (selectedPosition == -1) {
                    1f  // No selection - all items full alpha
                } else if (isSelected) {
                    1f  // This item is selected - full alpha
                } else {
                    0.5f  // All other items - reduced alpha
                }

                // Only animate if the alpha value is actually changing
                if (root.alpha != targetAlpha) {
                    root.animate()
                        .alpha(targetAlpha)
                        .setDuration(300)
                        .setInterpolator(AccelerateDecelerateInterpolator())
                        .start()
                } else {
                    // If alpha is already correct, just set it without animation
                    root.alpha = targetAlpha
                }

                root.setOnClickListener {
                    val currentClickedItemInfo = items[position]

                    if (selectedPosition == position) {
                        // Item is already selected and it's clicked again.
                        // Per new requirement, do nothing to the selection state.
                        // The item remains selected.
                        onChipClickListener?.onClick(currentClickedItemInfo) // Notify listener about the click
                        return@setOnClickListener // Selection does not change, no need to call notifyDataSetChanged()
                    }

                    // If a different item is clicked, or if no item was selected previously:
                    selectedPosition = position
                    notifyDataSetChanged() // Rebind items to reflect new selection and alpha states
                    onChipClickListener?.onClick(currentClickedItemInfo) // Notify listener about the click
                }
            }
        }

        override fun getItemCount(): Int = items.size
    }

    private data class ExpandableChipViewHolder(
        val binding: ExpandableChipViewLayoutBinding
    ) : ViewHolder(binding.root)

    private class ExpandableChipView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
    ) : LinearLayout(context, attrs) {

        val binding = ExpandableChipViewLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            false
        )

        var label : String = ""
            set(value) {
                field = value
                binding.tvTitle.text = value
            }

        var description : String = ""
            set(value) {
                field = value
                binding.tvDescription.text = value
            }
    }
}
