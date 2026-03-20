package com.example.localguide_planner.ui.places.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme

@Composable
fun LocalGuideTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
        ),
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(imageVector = icon, contentDescription = null) }
        },
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage) }
        } else {
            null
        },
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        singleLine = maxLines == 1,
    )
}

@Preview(showBackground = true, name = "LocalGuideTextField — default")
@Composable
private fun LocalGuideTextFieldPreview() {
    LocalGuidePlannerTheme {
        LocalGuideTextField(
            value = "Центральный парк",
            onValueChange = {},
            label = "Название",
        )
    }
}

@Preview(showBackground = true, name = "LocalGuideTextField — error")
@Composable
private fun LocalGuideTextFieldErrorPreview() {
    LocalGuidePlannerTheme {
        LocalGuideTextField(
            value = "",
            onValueChange = {},
            label = "Название",
            isError = true,
            errorMessage = "Введите название места",
        )
    }
}
