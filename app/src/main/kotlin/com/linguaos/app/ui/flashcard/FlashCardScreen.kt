package com.linguaos.app.ui.flashcard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguaos.app.ui.common.LinguaIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashCardScreen(
    onBack: () -> Unit,
    viewModel: FlashCardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (state.isComplete) {
        FlashCardSummary(correct = state.correct, wrong = state.wrong, xp = state.sessionXp,
            onBack = onBack, onRestart = viewModel::restart)
        return
    }

    val card = state.cards.getOrNull(state.currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcards") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress bar + counter
            LinearProgressIndicator(
                progress = { if (state.cards.isEmpty()) 0f else state.currentIndex / state.cards.size.toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${state.currentIndex + 1} / ${state.cards.size}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(painter = LinguaIcons.checkCircle(), contentDescription = null,
                            tint = Color.Unspecified, modifier = Modifier.size(14.dp))
                        Text("${state.correct}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(painter = LinguaIcons.closeCircle(), contentDescription = null,
                            tint = Color.Unspecified, modifier = Modifier.size(14.dp))
                        Text("${state.wrong}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.weight(0.2f))

            // Swipe hint row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = LinguaIcons.swipeLeft(), contentDescription = "Again",
                    tint = Color.Unspecified, modifier = Modifier.size(40.dp))
                Text(if (state.isFlipped) "Swipe to answer" else "Tap card to reveal",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
                Icon(painter = LinguaIcons.swipeRight(), contentDescription = "Know it",
                    tint = Color.Unspecified, modifier = Modifier.size(40.dp))
            }

            // The flip card
            if (card != null) {
                FlipCard(
                    targetWord      = card.card.targetWord,
                    transliteration = card.card.transliteration,
                    translation     = card.translation,
                    isFlipped       = state.isFlipped,
                    isLearned       = card.state.isLearned,
                    onFlip          = viewModel::flip,
                    onSwipeRight    = viewModel::swipeRight,
                    onSwipeLeft     = viewModel::swipeLeft
                )
            }

            Spacer(Modifier.weight(0.2f))

            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = viewModel::swipeLeft,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(painter = LinguaIcons.closeCircle(), contentDescription = null,
                        tint = Color.Unspecified, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Again  +1")
                }
                Button(
                    onClick  = viewModel::swipeRight,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(painter = LinguaIcons.checkCircle(), contentDescription = null,
                        tint = Color.Unspecified, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Know it  +2")
                }
            }
        }
    }
}

@Composable
private fun FlipCard(
    targetWord: String, transliteration: String, translation: String,
    isFlipped: Boolean, isLearned: Boolean,
    onFlip: () -> Unit, onSwipeRight: () -> Unit, onSwipeLeft: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue   = if (isFlipped) 180f else 0f,
        animationSpec = tween(380, easing = FastOutSlowInEasing),
        label         = "card_flip"
    )
    var dragX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragX > 110f  -> onSwipeRight()
                            dragX < -110f -> onSwipeLeft()
                        }
                        dragX = 0f
                    }
                ) { _, amt -> dragX += amt }
            }
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
    ) {
        if (rotation <= 90f) {
            // Front
            Card(
                modifier  = Modifier.fillMaxSize(),
                shape     = RoundedCornerShape(24.dp),
                onClick   = onFlip,
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(28.dp)) {
                    Column(modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(targetWord, style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        if (transliteration.isNotBlank()) {
                            Text(transliteration, style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f))
                        }
                    }
                    // Flip hint
                    Row(modifier = Modifier.align(Alignment.BottomCenter),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painter = LinguaIcons.flip(), contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp))
                        Text("Tap to reveal", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f))
                    }
                    // Learned badge
                    if (isLearned) {
                        Icon(painter = LinguaIcons.learned(), contentDescription = "Learned",
                            tint = Color.Unspecified, modifier = Modifier.size(24.dp).align(Alignment.TopEnd))
                    }
                }
            }
        } else {
            // Back (mirrored)
            Card(
                modifier  = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f },
                shape     = RoundedCornerShape(24.dp),
                onClick   = onFlip,
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(Modifier.fillMaxSize().padding(28.dp)) {
                    Column(modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(translation, style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Row(modifier = Modifier.align(Alignment.BottomCenter),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painter = LinguaIcons.flip(), contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp))
                        Text("Tap to flip back", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashCardSummary(correct: Int, wrong: Int, xp: Int, onBack: () -> Unit, onRestart: () -> Unit) {
    val total    = correct + wrong
    val accuracy = if (total > 0) correct * 100 / total else 0
    Column(modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(painter = LinguaIcons.trophy(), contentDescription = null,
            tint = Color.Unspecified, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Session Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Text("$accuracy% accuracy", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painter = LinguaIcons.checkCircle(), contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.size(20.dp))
            Text("$correct correct", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painter = LinguaIcons.closeCircle(), contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.size(20.dp))
            Text("$wrong to review", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(painter = LinguaIcons.xpBolt(), contentDescription = null,
                tint = Color.Unspecified, modifier = Modifier.size(20.dp))
            Text("+$xp XP earned", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Units") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("Study Again") }
    }
}
