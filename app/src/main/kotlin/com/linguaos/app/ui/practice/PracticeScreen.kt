package com.linguaos.app.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguaos.app.ui.common.LinguaIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    onBack: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (state.isComplete) {
        PracticeSummary(correct = state.correct, total = state.items.size, xp = state.sessionXp,
            onBack = onBack, onRestart = viewModel::restart)
        return
    }

    val item = state.items.getOrNull(state.currentIndex) ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phrase Practice") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 16.dp)) {
                        Icon(painter = LinguaIcons.xpBolt(), contentDescription = null,
                            tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                        Text("+${state.sessionXp}", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { state.currentIndex / state.items.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )
            Text("${state.currentIndex + 1} / ${state.items.size}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Translation hint card
            Card(modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(item.translation, modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            when (item.mode) {
                PracticeMode.FILL_BLANK -> FillBlankExercise(
                    blanked  = item.phrase.blankedPhrase,
                    options  = item.options,
                    selected = state.selectedAnswer,
                    answered = state.isAnswered,
                    correct  = state.isCorrect,
                    answer   = item.phrase.answer,
                    onSelect = viewModel::selectOption
                )
                PracticeMode.WORD_ORDER -> WordOrderExercise(
                    placed    = state.placedWords,
                    available = state.availableWords,
                    answered  = state.isAnswered,
                    correct   = state.isCorrect,
                    answer    = item.phrase.answer,
                    onTap     = viewModel::tapWord,
                    onRemove  = viewModel::removePlacedWord
                )
            }

            Spacer(Modifier.weight(1f))

            if (state.isAnswered) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = if (state.isCorrect) MaterialTheme.colorScheme.secondaryContainer
                                         else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            painter = if (state.isCorrect) LinguaIcons.checkCircle() else LinguaIcons.closeCircle(),
                            contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (state.isCorrect) "Correct! +5 XP" else "Answer: ${item.phrase.answer}",
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Button(onClick = viewModel::next, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
            }
        }
    }
}

@Composable
private fun FillBlankExercise(
    blanked: String, options: List<String>, selected: String,
    answered: Boolean, correct: Boolean, answer: String, onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(blanked, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        options.forEach { opt ->
            val isSelected   = selected == opt
            val isCorrectOpt = answered && opt == answer
            val isWrongOpt   = answered && isSelected && !correct
            val borderColor = when { isCorrectOpt -> MaterialTheme.colorScheme.secondary; isWrongOpt -> MaterialTheme.colorScheme.error; isSelected -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) }
            val bgColor     = when { isCorrectOpt -> MaterialTheme.colorScheme.secondaryContainer; isWrongOpt -> MaterialTheme.colorScheme.errorContainer; isSelected -> MaterialTheme.colorScheme.primaryContainer; else -> MaterialTheme.colorScheme.surface }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(if (isSelected || isCorrectOpt) 2.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
                    .clickable(enabled = !answered) { onSelect(opt) }
                    .padding(14.dp)
            ) {
                Text(opt, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun WordOrderExercise(
    placed: List<String>, available: List<String>,
    answered: Boolean, correct: Boolean, answer: String,
    onTap: (String) -> Unit, onRemove: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (placed.isEmpty()) {
                Text("Tap words below to build the sentence",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(placed) { word -> WordChip(word, !answered, { onRemove(word) }, true) }
                }
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(available) { word -> WordChip(word, !answered, { onTap(word) }, false) }
        }
    }
}

@Composable
private fun WordChip(word: String, enabled: Boolean, onClick: () -> Unit, filled: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (filled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) { Text(word, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
}

@Composable
private fun PracticeSummary(correct: Int, total: Int, xp: Int, onBack: () -> Unit, onRestart: () -> Unit) {
    val acc = if (total > 0) correct * 100 / total else 0
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(painter = LinguaIcons.practice(), contentDescription = null,
            tint = Color.Unspecified, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text("Practice Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Text("$acc% accuracy", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painter = LinguaIcons.checkCircle(), contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.size(18.dp))
            Text("$correct / $total correct", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painter = LinguaIcons.xpBolt(), contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.size(18.dp))
            Text("+$xp XP earned", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Units") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Try Again") }
    }
}
