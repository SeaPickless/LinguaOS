package com.linguaos.app.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguaos.app.ui.common.LinguaIcons
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.mode == QuizMode.SPEED_ROUND && !state.isComplete && !state.isLoading) {
        LaunchedEffect(state.isComplete) {
            while (!state.isComplete) { delay(1000L); viewModel.tickTimer() }
        }
    }

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (state.isComplete) {
        QuizResultScreen(state = state, onBack = onBack)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (state.mode) {
                        QuizMode.MULTIPLE_CHOICE -> "Multiple Choice"
                        QuizMode.TYPE_ANSWER     -> "Type the Answer"
                        QuizMode.MATCHING        -> "Match Pairs"
                        QuizMode.SPEED_ROUND     -> "Speed Round"
                    })
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (state.mode == QuizMode.SPEED_ROUND) {
                        Text(
                            "${state.timeLeft}s",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.mode != QuizMode.MATCHING) {
                LinearProgressIndicator(
                    progress = { state.currentIndex / state.questions.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )
                Text("${state.currentIndex + 1} / ${state.questions.size}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            when (state.mode) {
                QuizMode.MULTIPLE_CHOICE, QuizMode.SPEED_ROUND ->
                    MultipleChoiceSection(state, onSelect = viewModel::selectOption, onNext = viewModel::next)
                QuizMode.TYPE_ANSWER ->
                    TypeAnswerSection(state, onType = viewModel::updateTyped, onSubmit = viewModel::submitTyped, onNext = viewModel::next)
                QuizMode.MATCHING ->
                    MatchingSection(state, onLeft = viewModel::selectMatchLeft, onRight = viewModel::selectMatchRight)
            }
        }
    }
}

@Composable
private fun MultipleChoiceSection(state: QuizUiState, onSelect: (String) -> Unit, onNext: () -> Unit) {
    val q = state.questions.getOrNull(state.currentIndex) ?: return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(q.question, modifier = Modifier.fillMaxWidth().padding(24.dp),
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        q.options.forEach { opt ->
            val isSelected   = state.selectedOption == opt
            val isCorrectOpt = state.isAnswered && opt == q.answer
            val isWrong      = state.isAnswered && isSelected && !state.isCorrect
            val bg     = when { isCorrectOpt -> MaterialTheme.colorScheme.secondaryContainer; isWrong -> MaterialTheme.colorScheme.errorContainer; isSelected -> MaterialTheme.colorScheme.primaryContainer; else -> MaterialTheme.colorScheme.surface }
            val border = when { isCorrectOpt -> MaterialTheme.colorScheme.secondary; isWrong -> MaterialTheme.colorScheme.error; isSelected -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) }
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg)
                    .border(if (isSelected || isCorrectOpt) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
                    .clickable(enabled = !state.isAnswered) { onSelect(opt) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.isAnswered && (isCorrectOpt || isWrong)) {
                    Icon(
                        painter = if (isCorrectOpt) LinguaIcons.checkCircle() else LinguaIcons.closeCircle(),
                        contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(18.dp)
                    )
                }
                Text(opt, style = MaterialTheme.typography.bodyLarge)
            }
        }
        if (state.isAnswered) {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Next") }
        }
    }
}

@Composable
private fun TypeAnswerSection(state: QuizUiState, onType: (String) -> Unit, onSubmit: () -> Unit, onNext: () -> Unit) {
    val q = state.questions.getOrNull(state.currentIndex) ?: return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(q.question, modifier = Modifier.fillMaxWidth().padding(24.dp),
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        OutlinedTextField(
            value = state.typedAnswer, onValueChange = onType,
            label = { Text("Type the translation") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            enabled = !state.isAnswered, shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (!state.isAnswered) onSubmit() })
        )
        if (state.isAnswered) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = if (state.isCorrect) MaterialTheme.colorScheme.secondaryContainer
                                 else MaterialTheme.colorScheme.errorContainer)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(painter = if (state.isCorrect) LinguaIcons.checkCircle() else LinguaIcons.closeCircle(),
                        contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                    Text(if (state.isCorrect) "Correct!" else "Answer: ${q.answer}",
                        fontWeight = FontWeight.SemiBold)
                }
            }
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Next") }
        } else {
            Button(onClick = onSubmit, enabled = state.typedAnswer.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Check") }
        }
    }
}

@Composable
private fun MatchingSection(state: QuizUiState, onLeft: (String) -> Unit, onRight: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Match each word to its translation", style = MaterialTheme.typography.titleMedium)
        Text("${state.matchedPairs.size} / ${state.matchingLeft.size} matched",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.matchingLeft.forEach { (word, id) ->
                    val matched  = id in state.matchedPairs
                    val selected = state.selectedLeft == word
                    MatchChip(word, matched, selected) { if (!matched) onLeft(word) }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.matchingRight.forEach { (word, id) ->
                    val matched = id in state.matchedPairs
                    MatchChip(word, matched, false) { if (!matched) onRight(word) }
                }
            }
        }
    }
}

@Composable
private fun MatchChip(text: String, matched: Boolean, selected: Boolean, onClick: () -> Unit) {
    val bg     = when { matched -> MaterialTheme.colorScheme.secondaryContainer; selected -> MaterialTheme.colorScheme.primaryContainer; else -> MaterialTheme.colorScheme.surface }
    val border = when { matched -> MaterialTheme.colorScheme.secondary; selected -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) }
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(bg)
            .border(if (selected || matched) 2.dp else 1.dp, border, RoundedCornerShape(10.dp))
            .clickable(enabled = !matched, onClick = onClick).padding(12.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center) }
}

@Composable
private fun QuizResultScreen(state: QuizUiState, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isPerfect) {
            Icon(painter = LinguaIcons.star(), contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.size(64.dp))
        } else {
            Icon(painter = LinguaIcons.quiz(), contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(if (state.isPerfect) "Perfect Score!" else "Quiz Complete!",
            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Text("${state.score}%", style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        if (state.bestScore > 0) {
            Spacer(Modifier.height(4.dp))
            Text("Personal best: ${state.bestScore}%",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painter = LinguaIcons.xpBolt(), contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.size(20.dp))
            Text("+${state.xpEarned} XP earned", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Units") }
    }
}
