package com.guruai.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruai.app.ui.theme.Primary
import com.guruai.app.ui.theme.SurfaceVariant
import com.guruai.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onKeysSaved: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showGemini by remember { mutableStateOf(false) }
    var showGrok by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onKeysSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0D0D0D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "API Keys",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Gemini aur Grok keys yahan daalo. Securely save hoti hain.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            KeyCard(
                title = "Gemini API Key",
                value = uiState.geminiKey,
                onValueChange = viewModel::updateGeminiKey,
                show = showGemini,
                onToggleShow = { showGemini = !showGemini },
                placeholder = "AIzaSy..."
            )

            KeyCard(
                title = "Grok API Key",
                value = uiState.grokKey,
                onValueChange = viewModel::updateGrokKey,
                show = showGrok,
                onToggleShow = { showGrok = !showGrok },
                placeholder = "xai-..."
            )

            Text("Preferred AI", color = Color.White, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = uiState.aiProvider == "gemini",
                    onClick = { viewModel.updateProvider("gemini") },
                    label = { Text("Gemini") }
                )
                FilterChip(
                    selected = uiState.aiProvider == "grok",
                    onClick = { viewModel.updateProvider("grok") },
                    label = { Text("Grok") }
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI Online Mode", color = Color.White)
                        Switch(
                            checked = uiState.onlineMode,
                            onCheckedChange = viewModel::updateOnlineMode
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Voice Reply (TTS)", color = Color.White)
                        Switch(
                            checked = uiState.ttsEnabled,
                            onCheckedChange = viewModel::updateTts
                        )
                    }
                }
            }

            Text("Theme", color = Color.White, fontWeight = FontWeight.SemiBold)
            uiState.themeNames.forEachIndexed { index, name ->
                FilterChip(
                    selected = uiState.themeIndex == index,
                    onClick = { viewModel.updateTheme(index) },
                    label = { Text(name) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("Optional: Web Search", color = Color.White, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = uiState.searchApiKey,
                onValueChange = viewModel::updateSearchKey,
                label = { Text("Google Search API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors()
            )
            OutlinedTextField(
                value = uiState.searchCx,
                onValueChange = viewModel::updateSearchCx,
                label = { Text("Search Engine CX") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors()
            )

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    if (uiState.isSaved) "✓ Saved" else "Save All Settings",
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (uiState.isSaved) {
                Text(
                    "Settings saved successfully!",
                    color = Color(0xFF03DAC5),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun KeyCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    show: Boolean,
    onToggleShow: () -> Unit,
    placeholder: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Primary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                singleLine = true,
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleShow) {
                        Icon(
                            if (show) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = textFieldColors(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = Color.DarkGray,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Primary,
    focusedLabelColor = Primary,
    unfocusedLabelColor = Color.Gray
)
