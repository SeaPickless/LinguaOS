package com.linguaos.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.linguaos.app.data.db.entity.UnitEntity
import com.linguaos.app.data.db.entity.UserProgressEntity
import com.linguaos.app.ui.common.LinguaIcons
import com.linguaos.app.util.RankUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToUnitList: (String) -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    if (state.isLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    val user = state.user ?: return
    val rank = RankUtils.fromXp(user.totalXp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${greeting()}, ${user.username}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToLeaderboard) {
                        Icon(painter = LinguaIcons.leaderboard(), contentDescription = "Leaderboard", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Text(user.username.take(2).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Rank / XP card
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(painter = LinguaIcons.rankIcon(rank), contentDescription = rank.label,
                                tint = Color.Unspecified, modifier = Modifier.size(40.dp))
                            Column {
                                Text(rank.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(painter = LinguaIcons.xpBolt(), contentDescription = null,
                                        tint = Color.Unspecified, modifier = Modifier.size(16.dp))
                                    Text("${user.totalXp} XP", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
                                }
                            }
                        }
                        LinearProgressIndicator(
                            progress = { state.xpProgressFraction },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                        val xpToNext = state.xpToNextRank
                        if (xpToNext != null)
                            Text("$xpToNext XP to next rank", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        else
                            Text("Maximum rank reached!", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            // Streak card
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(painter = LinguaIcons.streak(), contentDescription = "Streak",
                            tint = Color.Unspecified, modifier = Modifier.size(36.dp))
                        Column {
                            Text("${user.streakCount} day streak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Keep it up! Study every day.", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            // Units
            if (state.activeCourse != null) {
                item { Text("Units", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                items(state.units, key = { it.unitId }) { unit ->
                    UnitTile(unit = unit, progress = state.progressMap[unit.unitNumber],
                        onClick = { onNavigateToUnitList(state.activeCourse!!.courseId) })
                }
            } else {
                item { Text("No active course. Complete onboarding to begin.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun UnitTile(unit: UnitEntity, progress: UserProgressEntity?, onClick: () -> Unit) {
    val isComplete    = progress?.isUnitComplete == true
    val flashProgress = if (unit.flashcardCount > 0) (progress?.flashcardsCompleted ?: 0) / unit.flashcardCount.toFloat() else 0f
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isComplete) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                .background(if (isComplete) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center) {
                if (isComplete)
                    Icon(painter = LinguaIcons.checkCircle(), contentDescription = "Complete",
                        tint = Color.Unspecified, modifier = Modifier.size(28.dp))
                else
                    Text(unit.unitNumber.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(unitName(unit.nameKey), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    AssistChip(onClick = {}, label = { Text(unit.cefrLevel, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.height(22.dp))
                }
                LinearProgressIndicator(progress = { flashProgress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)))
                Text("${progress?.flashcardsCompleted ?: 0}/${unit.flashcardCount} words",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(painter = LinguaIcons.chevronRight(), contentDescription = null, tint = Color.Unspecified)
        }
    }
}

private fun unitName(key: String) = when (key) {
    "unit_basics" -> "Basics"; "unit_travel" -> "Travel"; "unit_food_drink" -> "Food & Drink"
    "unit_family" -> "Family"; "unit_work_school" -> "Work & School"; "unit_shopping" -> "Shopping"
    "unit_health" -> "Health"; "unit_nature" -> "Nature"; "unit_culture" -> "Culture"
    "unit_advanced" -> "Advanced"; else -> key
}
private fun greeting() = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening"
}
