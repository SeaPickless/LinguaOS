package com.linguaos.app.ui.units

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguaos.app.data.db.entity.UnitEntity
import com.linguaos.app.data.db.entity.UserProgressEntity
import com.linguaos.app.ui.common.LinguaIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitListScreen(
    onBack: () -> Unit,
    onFlashCards: (String, Int) -> Unit,
    onPractice: (String, Int) -> Unit,
    onQuiz: (String, Int, String) -> Unit,
    viewModel: UnitListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.targetLanguageName.ifBlank { "Units" }) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(state.units, key = { it.unitId }) { unit ->
                UnitCard(
                    unit       = unit,
                    progress   = state.progressMap[unit.unitNumber],
                    courseId   = state.courseId,
                    onFlash    = { onFlashCards(state.courseId, unit.unitNumber) },
                    onPractice = { onPractice(state.courseId, unit.unitNumber) },
                    onQuiz     = { mode -> onQuiz(state.courseId, unit.unitNumber, mode) }
                )
            }
        }
    }
}

@Composable
private fun UnitCard(
    unit: UnitEntity,
    progress: UserProgressEntity?,
    courseId: String,
    onFlash: () -> Unit,
    onPractice: () -> Unit,
    onQuiz: (String) -> Unit
) {
    val isComplete = progress?.isUnitComplete == true
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (isComplete) MaterialTheme.colorScheme.secondaryContainer
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Header row
            Row(
                modifier = Modifier.clickable { expanded = !expanded }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(if (isComplete) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (isComplete)
                        Icon(painter = LinguaIcons.checkCircle(), contentDescription = "Complete",
                            tint = Color.Unspecified, modifier = Modifier.size(28.dp))
                    else
                        Text(unit.unitNumber.toString(), style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(unitName(unit.nameKey), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        AssistChip(onClick = {}, label = { Text(unit.cefrLevel, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(22.dp))
                    }
                    Text(
                        "${progress?.flashcardsCompleted ?: 0}/${unit.flashcardCount} words  ·  Quiz: ${if (progress?.quizCompleted == true) "${progress.quizScore}%" else "—"}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Expanded activities
            if (expanded) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivityRow(
                        iconPainter = LinguaIcons.flashcard(),
                        label       = "Flashcards",
                        progress    = "${progress?.flashcardsCompleted ?: 0}/${unit.flashcardCount}",
                        onClick     = onFlash
                    )
                    ActivityRow(
                        iconPainter = LinguaIcons.practice(),
                        label       = "Phrase Practice",
                        progress    = "${progress?.phrasesCompleted ?: 0}/${unit.phraseCount}",
                        onClick     = onPractice
                    )
                    Text("Quiz Modes", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "MULTIPLE_CHOICE" to "Multiple Choice",
                            "TYPE_ANSWER"     to "Type Answer",
                            "MATCHING"        to "Matching",
                            "SPEED_ROUND"     to "Speed"
                        ).forEach { (mode, label) ->
                            OutlinedButton(
                                onClick          = { onQuiz(mode) },
                                modifier         = Modifier.weight(1f),
                                contentPadding   = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    label: String,
    progress: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(painter = iconPainter, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(progress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Icon(painter = LinguaIcons.chevronRight(), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(16.dp))
    }
}

private fun unitName(key: String) = when (key) {
    "unit_basics"      -> "Basics"
    "unit_travel"      -> "Travel"
    "unit_food_drink"  -> "Food & Drink"
    "unit_family"      -> "Family"
    "unit_work_school" -> "Work & School"
    "unit_shopping"    -> "Shopping"
    "unit_health"      -> "Health"
    "unit_nature"      -> "Nature"
    "unit_culture"     -> "Culture"
    "unit_advanced"    -> "Advanced"
    else               -> key
}
