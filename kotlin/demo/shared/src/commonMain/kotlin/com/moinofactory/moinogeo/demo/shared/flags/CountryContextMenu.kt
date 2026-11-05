package com.moinofactory.moinogeo.demo.shared.flags

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.onClick
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import com.moinofactory.moinogeo.core.domain.model.Country
import com.moinofactory.moinogeo.demo.shared.explorer.hasCoordinates

/**
 * Wraps [content] with click / long-click / right-click gestures for country rows.
 * Primary click opens details; long-click or secondary-click opens a Go To menu.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CountryContextClickable(
    country: Country,
    onClick: () -> Unit,
    onGoTo: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var menuExpanded by remember(country.id) { mutableStateOf(false) }
    val canGoTo = country.hasCoordinates()

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true },
                )
                .onClick(
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                    onClick = { menuExpanded = true },
                ),
        ) {
            content()
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("Go to") },
                enabled = canGoTo,
                onClick = {
                    menuExpanded = false
                    onGoTo()
                },
            )
            DropdownMenuItem(
                text = { Text("Details") },
                onClick = {
                    menuExpanded = false
                    onClick()
                },
            )
        }
    }
}
