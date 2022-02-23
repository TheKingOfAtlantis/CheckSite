package com.sksulai.checksite.ui

import java.time.Duration

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

enum class DurationUnit {
    Minutes,
    Hours,
    Days,
    Weeks,
}

fun getDurationString(duration: Duration) = when {
    // TODO: Fix plural
    duration.toDays() > 6 &&
    duration.toDays() % 7 == 0L -> "Every ${duration.toDays()/7} weeks"
    duration.toDays() > 0       -> "Every ${duration.toDays()} days"
    duration.toHours() > 0      -> "Every ${duration.toHours()} hours"
    duration.toMinutes() > 0    -> "Every ${duration.toMinutes()} minutes"
    else -> throw IllegalArgumentException()
}

@ExperimentalMaterialApi
@Composable private fun CustomDuration(
    onValueSelected: (Duration) -> Unit,
    onDismissRequest: () -> Unit
) = Dialog(onDismissRequest) { Surface(
    shape = MaterialTheme.shapes.medium
) {
    Column(Modifier.padding(
        horizontal = 8.dp,
        vertical = 16.dp
    )) {
        var input by rememberSaveable { mutableStateOf(1) }

        Box(
            Modifier
                .padding(
                    top = 24.dp,
                    bottom = 24.dp,
                    start = 16.dp
                )
        ) {
            Text("Custom Reminder")
        }

        NumberField(
            value = input,
            onValueChange = { input = it },
            onFormatError = { str, e -> },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
        )

        var durationUnit by rememberSaveable { mutableStateOf(DurationUnit.Minutes) }
        Column(
            Modifier
                .padding(start = 16.dp)
                .selectableGroup()
        ) {
            @Composable
            fun option(
                value: DurationUnit,
                text: @Composable () -> Unit
            ) = Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = durationUnit == value,
                    onClick = { durationUnit = value }
                )
                Box(Modifier.padding(start = 8.dp)) {
                    text()
                }
            }

            @Composable
            fun option(value: DurationUnit) = option(value) { Text("Every " + value.name) }

            DurationUnit.values().forEach { option(it) }
        }

        Row(
            Modifier
                .padding(
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp
                )
                .align(Alignment.End)
        ) {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                onValueSelected(
                    when (durationUnit) {
                        DurationUnit.Minutes -> Duration.ofMinutes(input.toLong())
                        DurationUnit.Hours   -> Duration.ofHours(input.toLong())
                        DurationUnit.Days    -> Duration.ofDays(input.toLong())
                        DurationUnit.Weeks   -> Duration.ofDays(7 * input.toLong())
                    }
                )
            }) { Text("Add") }
        }
    }
} }

@ExperimentalMaterialApi
@Composable fun DurationList(
    onValueSelected: (Duration) -> Unit,
    onDismissRequest: () -> Unit,
    onCustomRequest: () -> Unit,
) = Dialog(onDismissRequest) { Surface(
    shape = MaterialTheme.shapes.medium
) {
    Column(
        Modifier.padding(horizontal = 8.dp)
    ) {
        // TODO: Cache user created reminders
        val durations = listOf(
            Duration.ofHours(1),
            Duration.ofHours(12),
            Duration.ofDays(1),
            Duration.ofDays(7),
        )

        LazyColumn {
            items(durations) {
                ListItem(
                    modifier = Modifier.clickable { onValueSelected(it) },
                    text = { Text(getDurationString(it)) },
                )
            }
            item {
                ListItem(
                    modifier = Modifier.clickable(onClick = onCustomRequest),
                    text = { Text("Custom") },
                )
            }
        }
    }
} }

@ExperimentalMaterialApi
@Composable fun DurationDialog(
    onValueSelected: (Duration) -> Unit,
    onDismissRequest: () -> Unit
) {
    var showCustom by rememberSaveable { mutableStateOf(false) }

    if(!showCustom) DurationList(
        onValueSelected,
        onDismissRequest,
        onCustomRequest = { showCustom = true }
    ) else CustomDuration(
        onValueSelected,
        onDismissRequest
    )
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable fun DurationField(
    value: Duration,
    onValueChange: (Duration) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    var showReminderDialog by rememberSaveable { mutableStateOf(false) }

    TextField(
        modifier = modifier,
        label = label,
        value = DurationFormatter.format(value),
        onValueChange = { },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        readOnly = true,
        singleLine = true,
        isError = isError,
        interactionSource = interactionSource,
    )

    val isPressed by interactionSource.collectIsPressedAsState()
    if(isPressed) showReminderDialog = true

    if(showReminderDialog) DurationDialog(
        onValueSelected = {
            onValueChange(it)
            showReminderDialog = false
        },
        onDismissRequest = { showReminderDialog = false }
    )
}
