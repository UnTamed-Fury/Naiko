package eu.kanade.tachiyomi.ui.source.browse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import eu.kanade.tachiyomi.databinding.SourceFilterSheetSavedSearchBinding
import yokai.domain.source.browse.filter.models.SavedSearch
import yokai.presentation.theme.YokaiTheme

class SavedSearchesAdapter(
    val searches: () -> List<SavedSearch>,
    val onSavedSearchClicked: (Long) -> Unit,
    val onDeleteSavedSearchClicked: (Long) -> Unit,
) :
    RecyclerView.Adapter<SavedSearchesAdapter.SavedSearchesViewHolder>() {

    private lateinit var binding: SourceFilterSheetSavedSearchBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedSearchesViewHolder {
        binding = SourceFilterSheetSavedSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedSearchesViewHolder(binding.root)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: SavedSearchesViewHolder, position: Int) {
        holder.bind()
    }

    inner class SavedSearchesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            binding.savedSearches.setContent {
                YokaiTheme {
                    Content()
                }
            }
            binding.savedSearches.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        }

        @Composable
        fun Content() {
            binding.savedSearchesTitle.isVisible = searches().isNotEmpty()

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                searches().forEach { search ->
                    val inputChipInteractionSource = remember { MutableInteractionSource() }
                    Box {
                        ElevatedSuggestionChip(
                            label = { Text(search.name) },
                            onClick = { },
                            interactionSource = inputChipInteractionSource,
                            colors = SuggestionChipDefaults.elevatedSuggestionChipColors().copy(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                        // Workaround to add long click to chips
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .combinedClickable(
                                    onLongClick = { onDeleteSavedSearchClicked(search.id) },
                                    onClick = { onSavedSearchClicked(search.id) },
                                    interactionSource = inputChipInteractionSource,
                                    indication = null,
                                )
                        )
                    }
                }
            }
        }
    }
}
