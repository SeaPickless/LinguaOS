package com.linguaos.app.ui.gamification

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.linguaos.app.ui.common.LinguaIcons
import com.linguaos.app.util.Rank

@Composable
fun RankUpDialog(newRank: Rank, onDismiss: () -> Unit) {
    var triggered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (triggered) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "rankup_scale"
    )
    LaunchedEffect(Unit) { triggered = true }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier  = Modifier.fillMaxWidth().scale(scale),
            shape     = RoundedCornerShape(24.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier            = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Rank Up!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Icon(
                    painter           = LinguaIcons.rankIcon(newRank),
                    contentDescription= newRank.label,
                    tint              = Color.Unspecified,
                    modifier          = Modifier.size(80.dp)
                )
                Text(newRank.label, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = LinguaIcons.rankColor(newRank))
                Text("You've reached ${newRank.label}!\nKeep studying to reach the next tier.",
                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Awesome!") }
            }
        }
    }
}
