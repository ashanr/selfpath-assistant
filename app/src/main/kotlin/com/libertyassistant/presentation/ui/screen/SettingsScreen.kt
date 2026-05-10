package com.libertyassistant.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.libertyassistant.ai.ApiProvider
import com.libertyassistant.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val focusManager = LocalFocusManager.current

    var isDarkTheme by rememberSaveable { mutableStateOf(false) }
    var showApiKey by rememberSaveable { mutableStateOf(false) }
    var apiKeyInput by rememberSaveable { mutableStateOf("") }
    var apiModelInput by rememberSaveable { mutableStateOf("") }

    // One-time sync from DataStore on first composition
    LaunchedEffect(prefs.apiKey) { if (apiKeyInput != prefs.apiKey) apiKeyInput = prefs.apiKey }
    LaunchedEffect(prefs.apiModel) { if (apiModelInput != prefs.apiModel) apiModelInput = prefs.apiModel }

    // Save when screen is popped (back navigation doesn't trigger onFocusChanged)
    DisposableEffect(Unit) {
        onDispose {
            viewModel.setApiKey(apiKeyInput)
            viewModel.setApiModel(apiModelInput)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Appearance ────────────────────────────────────────────────
            SectionLabel("Appearance")
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Dark Theme") },
                    supportingContent = { Text("Toggle between light and dark mode") },
                    leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    trailingContent = {
                        Switch(checked = isDarkTheme, onCheckedChange = { isDarkTheme = it })
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── AI Backend ────────────────────────────────────────────────
            SectionLabel("AI Backend")
            SettingsCard {
                // Toggle
                ListItem(
                    headlineContent = { Text("Use API") },
                    supportingContent = {
                        Text(
                            if (prefs.useApi) "Requests route through ${prefs.apiProvider.displayName}"
                            else "Using on-device model"
                        )
                    },
                    leadingContent = {
                        Icon(
                            if (prefs.useApi) Icons.Default.Cloud else Icons.Default.SmartToy,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = prefs.useApi,
                            onCheckedChange = viewModel::setUseApi
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                AnimatedVisibility(
                    visible = prefs.useApi,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        HorizontalDivider()

                        // Provider selector
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Provider",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ApiProvider.entries.forEach { provider ->
                                    FilterChip(
                                        selected = prefs.apiProvider == provider,
                                        onClick = { viewModel.setApiProvider(provider) },
                                        label = { Text(provider.displayName) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        // API Key
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "API Key",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it; viewModel.setApiKey(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("sk-…") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(
                                            if (showApiKey) Icons.Default.VisibilityOff
                                            else Icons.Default.Visibility,
                                            contentDescription = if (showApiKey) "Hide key" else "Show key"
                                        )
                                    }
                                },
                                visualTransformation = if (showApiKey) VisualTransformation.None
                                                       else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    viewModel.setApiKey(apiKeyInput)
                                    focusManager.clearFocus()
                                }),
                                singleLine = true
                            )
                        }

                        HorizontalDivider()

                        // Model name
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "Model",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = apiModelInput,
                                onValueChange = { apiModelInput = it; viewModel.setApiModel(it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(prefs.apiProvider.defaultModel) },
                                supportingText = {
                                    Text("Leave blank to use default: ${prefs.apiProvider.defaultModel}")
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    viewModel.setApiModel(apiModelInput)
                                    focusManager.clearFocus()
                                }),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── On-Device Model ───────────────────────────────────────────
            SectionLabel("On-Device Model")
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Model Status") },
                    supportingContent = {
                        Text("Place model in app/src/main/assets/models/ and rebuild")
                    },
                    leadingContent = { Icon(Icons.Default.Memory, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text("Supported Models") },
                    supportingContent = {
                        Text("Gemma-2B (TFLite) · Qwen-0.5B (TFLite) · Min 4 GB RAM")
                    },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── About ─────────────────────────────────────────────────────
            SectionLabel("About")
            SettingsCard {
                ListItem(
                    headlineContent = { Text("Liberty Assistant Hub") },
                    supportingContent = {
                        Text("Version 1.0.0 · MIT License · On-device & API AI")
                    },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        content()
    }
}
