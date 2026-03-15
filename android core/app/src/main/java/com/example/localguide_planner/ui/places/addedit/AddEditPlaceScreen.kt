package com.example.localguide_planner.ui.places.addedit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.theme.LocalGuide_PlannerTheme

@Composable
fun AddEditPlaceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditPlaceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is AddEditPlaceUiEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    AddEditPlaceContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onAddressChange = viewModel::onAddressChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onCategoryChange = viewModel::onCategoryChange,
        onSaveClicked = viewModel::onSaveClicked,
        onNavigateBack = viewModel::onNavigateBackClicked,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddEditPlaceContent(
    uiState: AddEditPlaceUiState,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (PlaceCategory) -> Unit,
    onSaveClicked: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AddEditPlaceTopBar(
                isEditMode = uiState.isEditMode,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AddEditPlaceForm(
                uiState = uiState,
                onNameChange = onNameChange,
                onAddressChange = onAddressChange,
                onDescriptionChange = onDescriptionChange,
                onCategoryChange = onCategoryChange,
                onSaveClicked = onSaveClicked,
            )
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPlaceTopBar(
    isEditMode: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = if (isEditMode) {
        stringResource(R.string.add_edit_place_title_edit)
    } else {
        stringResource(R.string.add_edit_place_title_add)
    }
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPlaceForm(
    uiState: AddEditPlaceUiState,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (PlaceCategory) -> Unit,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        NameTextField(
            name = uiState.name,
            nameError = uiState.nameError,
            onNameChange = onNameChange,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.address,
            onValueChange = onAddressChange,
            label = { Text(text = stringResource(R.string.add_edit_place_field_address)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text(text = stringResource(R.string.add_edit_place_field_description)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        CategoryDropdown(
            selectedCategory = uiState.category,
            onCategoryChange = onCategoryChange,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSaveClicked,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.add_edit_place_action_save))
        }
    }
}

@Composable
private fun NameTextField(
    name: String,
    nameError: String?,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(text = stringResource(R.string.add_edit_place_field_name)) },
        isError = nameError != null,
        supportingText = if (nameError != null) {
            { Text(text = stringResource(R.string.add_edit_place_error_name_empty)) }
        } else {
            null
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategory: PlaceCategory,
    onCategoryChange: (PlaceCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = stringResource(selectedCategory.toStringRes()),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.add_edit_place_field_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            PlaceCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(category.toStringRes())) },
                    onClick = {
                        onCategoryChange(category)
                        expanded = false
                    },
                )
            }
        }
    }
}

internal fun PlaceCategory.toStringRes(): Int = when (this) {
    PlaceCategory.RESTAURANT -> R.string.category_restaurant
    PlaceCategory.CAFE -> R.string.category_cafe
    PlaceCategory.PARK -> R.string.category_park
    PlaceCategory.MUSEUM -> R.string.category_museum
    PlaceCategory.SHOP -> R.string.category_shop
    PlaceCategory.LANDMARK -> R.string.category_landmark
    PlaceCategory.OTHER -> R.string.category_other
}

@Preview(showBackground = true, name = "AddEditPlaceScreen — Add mode")
@Composable
fun AddEditPlaceScreenAddPreview() {
    LocalGuide_PlannerTheme {
        AddEditPlaceContent(
            uiState = AddEditPlaceUiState(),
            onNameChange = {},
            onAddressChange = {},
            onDescriptionChange = {},
            onCategoryChange = {},
            onSaveClicked = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, name = "AddEditPlaceScreen — Edit mode")
@Composable
fun AddEditPlaceScreenEditPreview() {
    LocalGuide_PlannerTheme {
        AddEditPlaceContent(
            uiState = AddEditPlaceUiState(
                name = "Центральный парк",
                address = "ул. Ленина, 1",
                description = "Красивый городской парк с фонтанами и аллеями для прогулок.",
                category = PlaceCategory.PARK,
                isEditMode = true,
            ),
            onNameChange = {},
            onAddressChange = {},
            onDescriptionChange = {},
            onCategoryChange = {},
            onSaveClicked = {},
            onNavigateBack = {},
        )
    }
}
