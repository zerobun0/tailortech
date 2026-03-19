package com.tailortech.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailortech.app.MainViewModel
import com.tailortech.app.MeasurementField
import com.tailortech.app.TailorTab
import com.tailortech.app.data.UnitSystem
import com.tailortech.app.data.UserMeasurements
import com.tailortech.app.data.round1
import com.tailortech.app.data.toInches
import com.tailortech.app.domain.FitInsightsEngine
import com.tailortech.app.domain.AppSettings
import com.tailortech.app.domain.AppSettingsStore
import com.tailortech.app.domain.GeminiFitAdvisor
import com.tailortech.app.domain.GlobalSizeAdvisor
import com.tailortech.app.domain.OutfitAdviceResult
import com.tailortech.app.domain.sizeForCountry
import com.tailortech.app.ui.theme.AccentLime
import com.tailortech.app.ui.theme.Border
import com.tailortech.app.ui.theme.Surface
import com.tailortech.app.ui.theme.Surface2
import com.tailortech.app.ui.theme.TextMuted
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TailorTechApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val settingsStore = remember(context) { AppSettingsStore(context) }
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    var appSettings by remember { mutableStateOf(settingsStore.load()) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    GeminiFitAdvisor.setRuntimeApiKeyOverride(appSettings.geminiApiKeyOverride)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TAILORTECH", style = MaterialTheme.typography.titleLarge)
                        Text("Men's Measurement Intelligence", style = MaterialTheme.typography.labelMedium)
                    }
                },
                actions = {
                    TextButton(onClick = { showSettingsSheet = true }) {
                        Text("SETTINGS")
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TailorTabs(
                selected = selectedTab,
                onSelect = viewModel::selectTab
            )

            when (selectedTab) {
                TailorTab.DASHBOARD -> DashboardScreen(
                    measurements = measurements,
                    selectedCountry = selectedCountry,
                    appSettings = appSettings
                )
                TailorTab.STUDIO -> StudioScreen(
                    measurements,
                    onUpdate = viewModel::updateField,
                    onMeasurementSaved = {
                        settingsStore.updateLastMeasurementNow()
                        appSettings = settingsStore.load()
                    }
                )
                TailorTab.SIZE_INSIGHTS -> SizeInsightsScreen(
                    measurements = measurements,
                    selectedCountry = selectedCountry,
                    onSelectCountry = viewModel::selectCountry
                )
            }
        }
    }

    if (showSettingsSheet) {
        SettingsSheet(
            measurements = measurements,
            appSettings = appSettings,
            onDismiss = { showSettingsSheet = false },
            onToggleUnit = { viewModel.toggleUnitSystem() },
            onSaveSettings = { updated ->
                settingsStore.save(updated)
                appSettings = settingsStore.load()
                GeminiFitAdvisor.setRuntimeApiKeyOverride(appSettings.geminiApiKeyOverride)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TailorTabs(selected: TailorTab, onSelect: (TailorTab) -> Unit) {
    val tabs = listOf(
        TailorTab.DASHBOARD to Icons.Default.AutoGraph,
        TailorTab.STUDIO to Icons.Default.EditNote,
        TailorTab.SIZE_INSIGHTS to Icons.Default.Straighten
    )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (tab, icon) ->
            FilterChip(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                label = { Text(tab.name.replace('_', ' ')) },
                leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    measurements: UserMeasurements,
    selectedCountry: String,
    appSettings: AppSettings
) {
    val scroll = rememberScrollState()
    var selectedStat by remember { mutableStateOf<StatDetail?>(null) }
    val sizes = GlobalSizeAdvisor.advise(measurements)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (appSettings.remindersEnabled) {
            ReminderCard(appSettings = appSettings)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .border(1.dp, Border)
        ) {
            StatCard(
                label = "Height",
                value = displayValue(measurements.heightCm, measurements.unitSystem),
                unitSystem = measurements.unitSystem,
                onClick = {
                    selectedStat = StatDetail(
                        title = "Height",
                        cm = measurements.heightCm,
                        sizeHint = "CN tops ${sizes.chinaAsia.tops} | Suit ${sizes.ukUs.suit} baseline"
                    )
                }
            )
            StatCard(
                label = "Chest",
                value = displayValue(measurements.chestUpperCm, measurements.unitSystem),
                unitSystem = measurements.unitSystem,
                onClick = {
                    selectedStat = StatDetail(
                        title = "Chest Upper",
                        cm = measurements.chestUpperCm,
                        sizeHint = "UK/US Tops ${sizes.ukUs.tops} | EU ${sizes.eu.tops} | CN ${sizes.chinaAsia.tops}"
                    )
                }
            )
            StatCard(
                label = "Waist",
                value = displayValue(measurements.waistPantsLevelCm, measurements.unitSystem),
                unitSystem = measurements.unitSystem,
                onClick = {
                    selectedStat = StatDetail(
                        title = "Waist (Pants Level)",
                        cm = measurements.waistPantsLevelCm,
                        sizeHint = "Bottoms ${sizes.ukUs.bottoms} | EU ${sizes.eu.bottoms} | CN ${sizes.chinaAsia.bottoms}"
                    )
                }
            )
            StatCard(
                label = "Hip",
                value = displayValue(measurements.hipCm, measurements.unitSystem),
                unitSystem = measurements.unitSystem,
                onClick = {
                    selectedStat = StatDetail(
                        title = "Hip",
                        cm = measurements.hipCm,
                        sizeHint = "Use with waist for trouser fit and taper advice"
                    )
                }
            )
            StatCard(
                label = "Inseam",
                value = displayValue(measurements.inseamCm, measurements.unitSystem),
                unitSystem = measurements.unitSystem,
                onClick = {
                    selectedStat = StatDetail(
                        title = "Inseam",
                        cm = measurements.inseamCm,
                        sizeHint = "Bottom length ${sizes.ukUs.bottoms} | Usually maps to L30"
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        DataSection(
            title = "Head and Neck",
            rows = listOf(
                "Neck" to displayBoth(measurements.neckCm),
                "Head Front-to-Back" to displayBoth(measurements.headFrontToBackCm)
            )
        )
        DataSection(
            title = "Torso",
            rows = listOf(
                "Chest Upper" to displayBoth(measurements.chestUpperCm),
                "Chest Lower" to displayBoth(measurements.chestLowerCm),
                "Waist Natural" to displayBoth(measurements.waistNaturalCm),
                "Waist Pants Level" to displayBoth(measurements.waistPantsLevelCm),
                "Hip" to displayBoth(measurements.hipCm)
            )
        )
        DataSection(
            title = "Calculated",
            rows = listOf(
                "Inseam" to displayBoth(measurements.inseamCm),
                "Total Arm Length" to displayBoth(measurements.totalArmLengthCm),
                "Drop (Chest - Waist)" to displayBoth(measurements.dropCm)
            )
        )

        GeminiQuickPanel(
            measurements = measurements,
            selectedCountry = selectedCountry
        )
    }

    selectedStat?.let { stat ->
        ModalBottomSheet(
            onDismissRequest = { selectedStat = null },
            containerColor = Surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stat.title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${stat.cm.round1()} cm", style = MaterialTheme.typography.bodyLarge, color = AccentLime)
                Text("${stat.cm.toInches().round1()} in", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Size Insight", style = MaterialTheme.typography.titleMedium)
                Text(stat.sizeHint, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, unitSystem: UnitSystem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .background(Surface)
            .border(1.dp, Border)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = AccentLime)
            Spacer(modifier = Modifier.width(4.dp))
            Text(unitLabel(unitSystem), style = MaterialTheme.typography.labelMedium, color = TextMuted)
        }
    }
}

@Composable
private fun DataSection(title: String, rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface2)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )

        rows.forEach { (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, Border)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsSheet(
    measurements: UserMeasurements,
    appSettings: AppSettings,
    onDismiss: () -> Unit,
    onToggleUnit: () -> Unit,
    onSaveSettings: (AppSettings) -> Unit
) {
    var remindersEnabled by remember(appSettings) { mutableStateOf(appSettings.remindersEnabled) }
    var reminderDays by remember(appSettings) { mutableStateOf(appSettings.reminderIntervalDays) }
    var geminiKey by remember(appSettings) { mutableStateOf(appSettings.geminiApiKeyOverride) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge, color = AccentLime)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Measurement Units", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Current: ${if (measurements.unitSystem == UnitSystem.CM) "Centimeters (cm)" else "Inches (in)"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                TextButton(onClick = onToggleUnit) {
                    Text(if (measurements.unitSystem == UnitSystem.CM) "Switch to Inches" else "Switch to CM")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Measurement Reminders", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable reminder nudges", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = remindersEnabled, onCheckedChange = { remindersEnabled = it })
            }

            if (remindersEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 14, 30).forEach { days ->
                        FilterChip(
                            selected = reminderDays == days,
                            onClick = { reminderDays = days },
                            label = { Text("Every $days days") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Gemini API Key Override", style = MaterialTheme.typography.titleMedium)
            Text(
                "Leave blank to use GEMINI_API_KEY from Gradle properties.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            OutlinedTextField(
                value = geminiKey,
                onValueChange = { geminiKey = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                label = { Text("Gemini Key") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Button(onClick = {
                    onSaveSettings(
                        appSettings.copy(
                            remindersEnabled = remindersEnabled,
                            reminderIntervalDays = reminderDays,
                            geminiApiKeyOverride = geminiKey.trim()
                        )
                    )
                    onDismiss()
                }) {
                    Text("Save Settings")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ReminderCard(appSettings: AppSettings) {
    val daysSinceMeasure = if (appSettings.lastMeasurementEpochMs <= 0L) {
        Int.MAX_VALUE
    } else {
        val elapsedMs = System.currentTimeMillis() - appSettings.lastMeasurementEpochMs
        TimeUnit.MILLISECONDS.toDays(elapsedMs).toInt().coerceAtLeast(0)
    }

    val dueDays = appSettings.reminderIntervalDays.coerceAtLeast(1)
    val daysLeft = dueDays - daysSinceMeasure
    val status = if (daysSinceMeasure == Int.MAX_VALUE) {
        "No measurements saved in this session yet."
    } else if (daysLeft <= 0) {
        "Reminder due now. Update your key body measurements today."
    } else {
        "Next reminder in $daysLeft day(s)."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Measurement Reminder", style = MaterialTheme.typography.titleMedium, color = AccentLime)
            Text("Cadence: every ${appSettings.reminderIntervalDays} days", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(status, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GeminiQuickPanel(
    measurements: UserMeasurements,
    selectedCountry: String
) {
    var clothingPaste by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var aiResult by remember { mutableStateOf<OutfitAdviceResult?>(null) }
    var aiError by remember { mutableStateOf<String?>(null) }
    var aiLoading by remember { mutableStateOf(false) }
    var selectedPrompt by remember { mutableStateOf(geminiMasterPrompts().first()) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Quick AI Fit Check", style = MaterialTheme.typography.titleMedium, color = AccentLime)
            Text(
                "Paste clothing details and optional image link for instant fit + risk guidance.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                geminiMasterPrompts().forEach { prompt ->
                    FilterChip(
                        selected = selectedPrompt.mode == prompt.mode,
                        onClick = { selectedPrompt = prompt },
                        label = { Text(prompt.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(selectedPrompt.template, style = MaterialTheme.typography.bodyMedium, color = TextMuted)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = clothingPaste,
                onValueChange = { clothingPaste = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                label = { Text("Product details / size chart text") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Optional image URL") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    aiLoading = true
                    aiError = null
                    aiResult = null

                    val mergedText = buildString {
                        append(selectedPrompt.template)
                        append("\n\n")
                        append(clothingPaste)
                    }

                    scope.launch {
                        val result = GeminiFitAdvisor.analyzeOutfit(
                            measurements = measurements,
                            clothingPaste = mergedText,
                            countryCode = selectedCountry,
                            analysisMode = selectedPrompt.mode,
                            imageUrl = imageUrl.ifBlank { null }
                        )

                        result
                            .onSuccess { aiResult = it }
                            .onFailure { aiError = it.message ?: "Gemini analysis failed" }
                        aiLoading = false
                    }
                },
                enabled = !aiLoading && clothingPaste.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (aiLoading) "Analyzing..." else "Run Quick Fit Analysis")
            }

            if (aiResult != null) {
                val result = aiResult!!
                Spacer(modifier = Modifier.height(10.dp))
                Text("Recommendation: ${result.recommendation}", style = MaterialTheme.typography.bodyLarge, color = AccentLime)
                Text(result.summary, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Risk Score: ${result.riskScore}/100", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = { result.riskScore / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (result.riskScore >= 70) MaterialTheme.colorScheme.secondary else AccentLime,
                    trackColor = Surface2
                )

                if (result.risks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Risks", style = MaterialTheme.typography.titleMedium)
                    result.risks.forEach { Text("- $it", style = MaterialTheme.typography.bodyMedium, color = TextMuted) }
                }
                if (result.fitNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fit Notes", style = MaterialTheme.typography.titleMedium)
                    result.fitNotes.forEach { Text("- $it", style = MaterialTheme.typography.bodyMedium, color = TextMuted) }
                }
            }

            if (aiError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Error: $aiError", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Text(
                    "Tip: open Settings (top-right) to paste a Gemini key or set GEMINI_API_KEY in Gradle properties.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun StudioScreen(
    measurements: UserMeasurements,
    onUpdate: (MeasurementField, Double) -> Unit,
    onMeasurementSaved: () -> Unit
) {
    var selectedField by remember { mutableStateOf<MeasurementField?>(null) }
    var inputText by remember { mutableStateOf("") }
    val groupedPrompts = remember { studioPromptsByGroup() }
    var activeGroup by remember { mutableStateOf(groupedPrompts.keys.first()) }
    var studioMode by remember { mutableStateOf(StudioMode.BROWSE) }
    var guidedIndex by remember { mutableStateOf(0) }
    var reviewedFields by remember { mutableStateOf(setOf<MeasurementField>()) }

    val allPrompts = remember(groupedPrompts) { groupedPrompts.values.flatten() }
    val totalCount = allPrompts.size.coerceAtLeast(1)
    val completedCount = allPrompts.count { reviewedFields.contains(it.field) }
    val overallProgress = completedCount.toFloat() / totalCount.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = "Measurement Studio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No silhouette view. Use guided categories and measurement instructions.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text("Session Progress: $completedCount / $totalCount", style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(
            progress = { overallProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            color = AccentLime,
            trackColor = Surface2
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = studioMode == StudioMode.BROWSE,
                onClick = { studioMode = StudioMode.BROWSE },
                label = { Text("Browse") }
            )
            FilterChip(
                selected = studioMode == StudioMode.GUIDED,
                onClick = { studioMode = StudioMode.GUIDED },
                label = { Text("Guided Session") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groupedPrompts.keys.forEach { group ->
                FilterChip(
                    selected = activeGroup == group,
                    onClick = { activeGroup = group },
                    label = { Text(group) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (studioMode == StudioMode.BROWSE) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .border(1.dp, Border),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val groupPrompts = groupedPrompts[activeGroup].orEmpty()
                    val groupCompleted = groupPrompts.count { reviewedFields.contains(it.field) }
                    val groupProgress = if (groupPrompts.isEmpty()) 0f else groupCompleted.toFloat() / groupPrompts.size.toFloat()

                    Text(
                        text = "$activeGroup Measurements",
                        style = MaterialTheme.typography.titleMedium,
                        color = AccentLime
                    )
                    Text(
                        text = "Completed $groupCompleted / ${groupPrompts.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    LinearProgressIndicator(
                        progress = { groupProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 6.dp),
                        color = AccentLime,
                        trackColor = Surface2
                    )

                    groupPrompts.forEach { prompt ->
                        val value = currentFieldValue(measurements, prompt.field)
                        val valid = prompt.isInExpectedRange(value)
                        val reviewed = reviewedFields.contains(prompt.field)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedField = prompt.field
                                    inputText = value.toString()
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${prompt.code} ${prompt.field.label}", style = MaterialTheme.typography.bodyLarge)
                                Text(displayBoth(value), style = MaterialTheme.typography.bodyMedium, color = AccentLime)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BadgeText(if (prompt.required) "Required" else "Optional", if (prompt.required) AccentLime else TextMuted)
                                BadgeText(if (valid) "Range OK" else "Check Range", if (valid) AccentLime else MaterialTheme.colorScheme.secondary)
                                if (reviewed) BadgeText("Reviewed", AccentLime)
                            }
                            Text(prompt.instruction, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                    }
                }
            }
        } else {
            val prompt = allPrompts[guidedIndex.coerceIn(0, allPrompts.lastIndex)]
            val value = currentFieldValue(measurements, prompt.field)
            val valid = prompt.isInExpectedRange(value)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Step ${guidedIndex + 1} of ${allPrompts.size}", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    Text("${prompt.code} ${prompt.field.label}", style = MaterialTheme.typography.titleMedium, color = AccentLime)
                    Text(prompt.instruction, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(displayBoth(value), style = MaterialTheme.typography.bodyLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgeText(if (prompt.required) "Required" else "Optional", if (prompt.required) AccentLime else TextMuted)
                        BadgeText(if (valid) "Range OK" else "Check Range", if (valid) AccentLime else MaterialTheme.colorScheme.secondary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedField = prompt.field; inputText = value.toString() },
                            modifier = Modifier.weight(1f)
                        ) { Text("Edit") }
                        Button(
                            onClick = {
                                reviewedFields = reviewedFields + prompt.field
                                if (guidedIndex < allPrompts.lastIndex) guidedIndex += 1
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Mark + Next") }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { if (guidedIndex > 0) guidedIndex -= 1 }) { Text("Previous") }
                        TextButton(onClick = { if (guidedIndex < allPrompts.lastIndex) guidedIndex += 1 }) { Text("Next") }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = "Quick Edit All Measurements",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )

            MeasurementField.entries.forEach { field ->
                val value = currentFieldValue(measurements, field)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedField = field
                            inputText = value.toString()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(field.label, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    Text(displayBoth(value), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    val field = selectedField
    if (field != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedField = null },
            containerColor = Surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Edit ${field.label}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Value in cm") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { selectedField = null }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = {
                        inputText.toDoubleOrNull()?.let {
                            onUpdate(field, it)
                            onMeasurementSaved()
                            reviewedFields = reviewedFields + field
                        }
                        selectedField = null
                    }) {
                        Text("Save", color = AccentLime)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeInsightsScreen(
    measurements: UserMeasurements,
    selectedCountry: String,
    onSelectCountry: (String) -> Unit
) {
    val profile = GlobalSizeAdvisor.advise(measurements)
    val insights = FitInsightsEngine.buildInsights(measurements)
    val activeCountry = profile.sizeForCountry(selectedCountry)
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text("Global Size Advisor", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = activeCountry.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("My Country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                profile.countryMappings.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSelectCountry(option.code)
                            expanded = false
                        }
                    )
                }
            }
        }

        SizeRegionCard(
            region = "My Country: ${activeCountry.label}",
            tops = activeCountry.size.tops,
            bottoms = activeCountry.size.bottoms,
            shoes = activeCountry.size.shoes,
            collar = activeCountry.size.collar,
            suit = activeCountry.size.suit
        )

        SizeRegionCard(
            region = "UK / US",
            tops = profile.ukUs.tops,
            bottoms = profile.ukUs.bottoms,
            shoes = profile.ukUs.shoes,
            collar = profile.ukUs.collar,
            suit = profile.ukUs.suit
        )
        SizeRegionCard(
            region = "EU",
            tops = profile.eu.tops,
            bottoms = profile.eu.bottoms,
            shoes = profile.eu.shoes,
            collar = profile.eu.collar,
            suit = profile.eu.suit
        )
        SizeRegionCard(
            region = "China / Asia",
            tops = profile.chinaAsia.tops,
            bottoms = profile.chinaAsia.bottoms,
            shoes = profile.chinaAsia.shoes,
            collar = profile.chinaAsia.collar,
            suit = profile.chinaAsia.suit
        )

        DataSection(
            title = "Brand Specifics",
            rows = profile.brandNotes.mapIndexed { index, note ->
                "Note ${index + 1}" to note
            }
        )

        DataSection(
            title = "Fit Insights",
            rows = insights.map { it.title to it.detail }
        )

    }
}

@Composable
private fun SizeRegionCard(
    region: String,
    tops: String,
    bottoms: String,
    shoes: String,
    collar: String,
    suit: String
) {
    DataSection(
        title = region,
        rows = listOf(
            "Tops" to tops,
            "Bottoms" to bottoms,
            "Shoes" to shoes,
            "Collar" to collar,
            "Suit" to suit
        )
    )
}

private data class StatDetail(
    val title: String,
    val cm: Double,
    val sizeHint: String
)

private data class MeasurementPrompt(
    val code: String,
    val field: MeasurementField,
    val instruction: String,
    val required: Boolean = true,
    val expectedRangeCm: ClosedFloatingPointRange<Double>? = null
)

private enum class StudioMode {
    BROWSE,
    GUIDED
}

private data class GeminiMasterPrompt(
    val label: String,
    val mode: String,
    val template: String
)

private fun geminiMasterPrompts(): List<GeminiMasterPrompt> {
    return listOf(
        GeminiMasterPrompt(
            label = "Fit Check",
            mode = "FIT_CHECK",
            template = "Evaluate exact fit based on chest/waist/hip/inseam proportions. Flag tight zones and suggest best size choice."
        ),
        GeminiMasterPrompt(
            label = "Buy Risk",
            mode = "BUY_RISK",
            template = "Estimate purchase risk from inconsistent size charts, unknown stretch, brand sizing drift, and return difficulty."
        ),
        GeminiMasterPrompt(
            label = "Style Match",
            mode = "STYLE_MATCH",
            template = "Assess if garment cut (slim/regular/relaxed/oversized) suits this body profile and suggest better alternatives."
        ),
        GeminiMasterPrompt(
            label = "Research",
            mode = "MARKET_RESEARCH",
            template = "Use details to infer likely brand behavior, fabric quirks, and where fit failures happen most for similar items."
        )
    )
}

private fun MeasurementPrompt.isInExpectedRange(value: Double): Boolean {
    val range = expectedRangeCm ?: return true
    return value in range
}

private fun studioPromptsByGroup(): Map<String, List<MeasurementPrompt>> {
    return linkedMapOf(
        "Head and Torso" to listOf(
            MeasurementPrompt("A", MeasurementField.NECK, "Wrap tape around the base of neck, level and snug.", expectedRangeCm = 30.0..50.0),
            MeasurementPrompt("B", MeasurementField.CHEST_UPPER, "Measure around fullest chest with relaxed posture.", expectedRangeCm = 75.0..140.0),
            MeasurementPrompt("C", MeasurementField.CHEST_LOWER, "Measure just below chest line, tape parallel to floor.", expectedRangeCm = 70.0..130.0),
            MeasurementPrompt("D", MeasurementField.WAIST_NATURAL, "Measure natural waist at narrowest point.", expectedRangeCm = 60.0..130.0),
            MeasurementPrompt("E", MeasurementField.HIP, "Measure around widest hip/glute point.", expectedRangeCm = 70.0..150.0),
            MeasurementPrompt("M", MeasurementField.NECK_TO_WAIST, "From back neck point straight to waist level.", expectedRangeCm = 30.0..70.0),
            MeasurementPrompt("N", MeasurementField.RISE, "Front waist through crotch to back waist.", expectedRangeCm = 45.0..85.0),
            MeasurementPrompt("Q", MeasurementField.NECK_TO_SHOULDER, "From neck base to shoulder edge.", expectedRangeCm = 10.0..25.0),
            MeasurementPrompt("L", MeasurementField.SHOULDER_TO_SHOULDER, "Across back from shoulder bone to shoulder bone.", expectedRangeCm = 35.0..60.0),
            MeasurementPrompt("I", MeasurementField.ARMSCYE, "Around arm opening where sleeve seam sits.", expectedRangeCm = 30.0..60.0)
        ),
        "Arms and Hands" to listOf(
            MeasurementPrompt("J1", MeasurementField.BICEP_FLEXED, "Around widest bicep while flexed.", expectedRangeCm = 20.0..60.0),
            MeasurementPrompt("J2", MeasurementField.BICEP_RELAXED, "Around widest bicep while arm is relaxed.", required = false, expectedRangeCm = 18.0..55.0),
            MeasurementPrompt("K", MeasurementField.WRIST, "Around wrist bone snugly.", expectedRangeCm = 12.0..30.0),
            MeasurementPrompt("R", MeasurementField.SHOULDER_TO_ELBOW, "From shoulder edge to elbow tip.", expectedRangeCm = 20.0..45.0),
            MeasurementPrompt("S", MeasurementField.ELBOW_TO_WRIST, "From elbow tip to wrist bone.", expectedRangeCm = 18.0..45.0),
            MeasurementPrompt("WF", MeasurementField.WRIST_TO_MIDDLE_FINGER, "From wrist crease to middle fingertip.", required = false, expectedRangeCm = 15.0..30.0)
        ),
        "Legs and Feet" to listOf(
            MeasurementPrompt("F", MeasurementField.THIGH_WIDEST, "Around widest thigh point.", expectedRangeCm = 35.0..90.0),
            MeasurementPrompt("G", MeasurementField.KNEE, "Around kneecap center with leg straight.", expectedRangeCm = 25.0..60.0),
            MeasurementPrompt("H", MeasurementField.CALF_WIDEST, "Around widest calf.", expectedRangeCm = 25.0..60.0),
            MeasurementPrompt("A2", MeasurementField.ANKLE, "Around narrowest ankle point.", expectedRangeCm = 16.0..35.0),
            MeasurementPrompt("O", MeasurementField.INNER_THIGH_TO_KNEE, "Inner thigh point down to knee.", expectedRangeCm = 25.0..60.0),
            MeasurementPrompt("P", MeasurementField.KNEE_TO_ANKLE, "Knee center down to ankle bone.", expectedRangeCm = 25.0..60.0),
            MeasurementPrompt("T", MeasurementField.OUTER_THIGH, "Side waist to knee on outer seam line.", required = false, expectedRangeCm = 35.0..80.0),
            MeasurementPrompt("FL", MeasurementField.FOOT_LENGTH, "Heel to longest toe.", expectedRangeCm = 20.0..35.0),
            MeasurementPrompt("FW", MeasurementField.FOOT_WIDTH, "Across widest forefoot.", required = false, expectedRangeCm = 7.0..15.0)
        )
    )
}

@Composable
private fun BadgeText(label: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

private fun displayValue(cm: Double, unitSystem: UnitSystem): String {
    return if (unitSystem == UnitSystem.CM) {
        "${cm.round1()}"
    } else {
        "${cm.toInches().round1()}"
    }
}

private fun unitLabel(unitSystem: UnitSystem): String {
    return if (unitSystem == UnitSystem.CM) "cm" else "in"
}

private fun displayBoth(cm: Double): String {
    return "${cm.round1()} cm / ${cm.toInches().round1()} in"
}

private fun currentFieldValue(measurements: UserMeasurements, field: MeasurementField): Double {
    return when (field) {
        MeasurementField.HEIGHT -> measurements.heightCm
        MeasurementField.HEAD_FRONT_TO_BACK -> measurements.headFrontToBackCm
        MeasurementField.NECK -> measurements.neckCm
        MeasurementField.CHEST_UPPER -> measurements.chestUpperCm
        MeasurementField.CHEST_LOWER -> measurements.chestLowerCm
        MeasurementField.WAIST_NATURAL -> measurements.waistNaturalCm
        MeasurementField.WAIST_PANTS -> measurements.waistPantsLevelCm
        MeasurementField.HIP -> measurements.hipCm
        MeasurementField.SHOULDER_TO_SHOULDER -> measurements.shoulderToShoulderCm
        MeasurementField.NECK_TO_SHOULDER -> measurements.neckToShoulderCm
        MeasurementField.NECK_TO_WAIST -> measurements.neckToWaistCm
        MeasurementField.ARMSCYE -> measurements.armscyeCm
        MeasurementField.BICEP_FLEXED -> measurements.bicepFlexedCm
        MeasurementField.BICEP_RELAXED -> measurements.bicepRelaxedCm
        MeasurementField.WRIST -> measurements.wristCm
        MeasurementField.WRIST_TO_MIDDLE_FINGER -> measurements.wristToMiddleFingerCm
        MeasurementField.INNER_THIGH_TO_KNEE -> measurements.innerThighToKneeCm
        MeasurementField.KNEE_TO_ANKLE -> measurements.kneeToAnkleCm
        MeasurementField.KNEE -> measurements.kneeCm
        MeasurementField.CALF_WIDEST -> measurements.calfWidestCm
        MeasurementField.ANKLE -> measurements.ankleCm
        MeasurementField.OUTER_THIGH -> measurements.outerThighCm
        MeasurementField.FOOT_LENGTH -> measurements.footLengthCm
        MeasurementField.FOOT_WIDTH -> measurements.footWidthCm
        MeasurementField.SHOULDER_TO_ELBOW -> measurements.shoulderToElbowCm
        MeasurementField.ELBOW_TO_WRIST -> measurements.elbowToWristCm
        MeasurementField.RISE -> measurements.riseCrotchCm
        MeasurementField.THIGH_WIDEST -> measurements.thighWidestCm
    }
}
