package com.moinofactory.moinogeo.demo.shared.explorer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moinofactory.moinogeo.demo.shared.ui.icon.Icons
import com.moinofactory.moinogeo.demo.shared.ui.icon.close

/**
 * Non-editable search affordance that opens the place-search dialog.
 */
@Composable
internal fun PlaceSearchBar(
    query: String,
    onOpenSearch: () -> Unit,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = {},
            readOnly = true,
            label = { Text("Search places...") },
            placeholder = { Text("Countries, cities, regions...") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                if (query.isNotEmpty() && onClear != null) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.close,
                            contentDescription = "Clear search and selection",
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (query.isNotEmpty() && onClear != null) {
                        Modifier.padding(end = 48.dp)
                    } else {
                        Modifier
                    }
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onOpenSearch,
                ),
        )
    }
}
