package com.moinofactory.moinogeo.demo.shared.flags.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moinofactory.moinogeo.demo.shared.flags.FlagViewMode

@Composable
fun FlagOptionsBar(
    selectedViewMode: FlagViewMode,
    onViewModeSelected: (FlagViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FlagViewMode.entries.forEach { mode ->
                ElevatedFilterChip(
                    selected = selectedViewMode == mode,
                    onClick = { onViewModeSelected(mode) },
                    label = { Text(mode.title, fontSize = 12.sp) },
                )
            }
        }
    }
}
