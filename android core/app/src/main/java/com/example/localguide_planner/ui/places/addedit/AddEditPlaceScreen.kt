package com.example.localguide_planner.ui.places.addedit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localguide_planner.R
import com.example.localguide_planner.domain.model.PlaceCategory
import com.example.localguide_planner.ui.places.common.LocalGuideTextField
import com.example.localguide_planner.ui.places.common.toIcon
import com.example.localguide_planner.ui.places.common.toStringRes
import com.example.localguide_planner.ui.theme.LocalGuidePlannerTheme
import kotlinx.coroutines.delay

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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isEditMode) {
                            stringResource(R.string.add_edit_place_title_edit)
                        } else {
                            stringResource(R.string.add_edit_place_title_add)
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        AddEditPlaceFormContent(
            uiState = uiState,
            onNameChange = onNameChange,
            onAddressChange = onAddressChange,
            onDescriptionChange = onDescriptionChange,
            onCategoryChange = onCategoryChange,
            onSaveClicked = onSaveClicked,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun AddEditPlaceFormContent(
    uiState: AddEditPlaceUiState,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategoryChange: (PlaceCategory) -> Unit,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            AnimatedFormField(index = 0, visible = visible) {
                PlaceNameField(
                    name = uiState.name,
                    onNameChange = onNameChange,
                    isError = uiState.nameError != null,
                    errorMessage = uiState.nameError,
                )
            }
        }
        item {
            AnimatedFormField(index = 1, visible = visible) {
                PlaceAddressField(address = uiState.address, onAddressChange = onAddressChange)
            }
        }
        item {
            AnimatedFormField(index = 2, visible = visible) {
                PlaceDescriptionField(
                    description = uiState.description,
                    onDescriptionChange = onDescriptionChange,
                )
            }
        }
        item {
            AnimatedFormField(index = 3, visible = visible) {
                PlaceCategorySelector(
                    selectedCategory = uiState.category,
                    onCategoryChange = onCategoryChange,
                )
            }
        }
        item {
            AnimatedFormField(index = 4, visible = visible) {
                SaveButton(isSaving = uiState.isSaving, onClick = onSaveClicked)
            }
        }
    }
}

@Composable
private fun PlaceNameField(
    name: String,
    onNameChange: (String) -> Unit,
    isError: Boolean = false,
    errorMessage: String? = null,
) {
    LocalGuideTextField(
        value = name,
        onValueChange = onNameChange,
        label = stringResource(R.string.add_edit_place_field_name),
        leadingIcon = Icons.Rounded.Label,
        isError = isError,
        errorMessage = errorMessage,
    )
}

@Composable
private fun PlaceAddressField(
    address: String,
    onAddressChange: (String) -> Unit,
) {
    LocalGuideTextField(
        value = address,
        onValueChange = onAddressChange,
        label = stringResource(R.string.add_edit_place_field_address),
        leadingIcon = Icons.Rounded.LocationOn,
    )
}

@Composable
private fun PlaceDescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
    LocalGuideTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = stringResource(R.string.add_edit_place_label_description),
        leadingIcon = Icons.Rounded.Description,
        maxLines = 5,
    )
}

@Composable
private fun PlaceCategorySelector(
    selectedCategory: PlaceCategory,
    onCategoryChange: (PlaceCategory) -> Unit,
) {
    CategorySelector(
        selectedCategory = selectedCategory,
        onCategorySelected = onCategoryChange,
    )
}

@Composable
private fun AnimatedFormField(
    index: Int,
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var itemVisible by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(minOf(index * 80L, 400L))
            itemVisible = true
        }
    }
    AnimatedVisibility(
        visible = itemVisible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 },
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: PlaceCategory,
    onCategorySelected: (PlaceCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.add_edit_place_field_category),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PlaceCategory.entries) { category ->
                FilterChip(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    label = { Text(stringResource(category.toStringRes())) },
                    leadingIcon = {
                        Icon(
                            imageVector = category.toIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        AnimatedContent(
            targetState = isSaving,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "SaveButtonContent",
        ) { saving ->
            if (saving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(R.string.add_edit_place_action_save))
            }
        }
    }
}

@Preview(showBackground = true, name = "AddEditPlaceScreen — Add mode")
@Composable
private fun AddEditPlaceScreenAddPreview() {
    LocalGuidePlannerTheme {
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
private fun AddEditPlaceScreenEditPreview() {
    LocalGuidePlannerTheme {
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

@Preview(showBackground = true, name = "AddEditPlaceScreen — isSaving")
@Composable
private fun AddEditPlaceScreenSavingPreview() {
    LocalGuidePlannerTheme {
        AddEditPlaceContent(
            uiState = AddEditPlaceUiState(name = "Парк", isSaving = true),
            onNameChange = {},
            onAddressChange = {},
            onDescriptionChange = {},
            onCategoryChange = {},
            onSaveClicked = {},
            onNavigateBack = {},
        )
    }
}
