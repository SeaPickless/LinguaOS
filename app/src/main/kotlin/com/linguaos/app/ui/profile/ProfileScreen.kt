package com.linguaos.app.ui.profile

import androidx.compose.foundation.background
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
import com.linguaos.app.ui.common.LinguaIcons
import com.linguaos.app.util.CefrLevel
import com.linguaos.app.util.CefrUtils
import com.linguaos.app.util.RankUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAchievements: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    if (state.isLoading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    val user = state.user ?: return
    val rank = RankUtils.fromXp(user.totalXp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = onAchievements) {
                        Icon(painter = LinguaIcons.trophy(), contentDescription = "Achievements",
                            tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Avatar
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center) {
                        Text(user.username.take(2).uppercase(), style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(user.username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painter = LinguaIcons.rankIcon(rank), contentDescription = rank.label,
                            tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                        Text(rank.label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            // Stats
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("${user.totalXp}", "Total XP")
                            StatItem("${state.currentStreak}", "Streak")
                            StatItem("${state.wordsLearned}", "Words")
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItem("${state.courseProgressList.size}", "Courses")
                            StatItem("${state.longestStreak}", "Best Streak")
                            StatItem("${state.achievementCount}", "Badges")
                        }
                    }
                }
            }
            // XP progress
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Rank Progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { RankUtils.progressToNextRank(user.totalXp) },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                        )
                        val xpToNext = RankUtils.xpToNextRank(user.totalXp)
                        if (xpToNext != null)
                            Text("$xpToNext XP to ${RankUtils.fromXp(user.totalXp + xpToNext).label}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else
                            Text("Maximum rank reached!", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            // Language CEFR badges
            if (state.courseProgressList.isNotEmpty()) {
                item { Text("Language Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(state.courseProgressList, key = { it.course.courseId }) { cp ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (cp.cefrLevel == CefrLevel.NONE) "—" else cp.cefrLevel.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cp.targetLanguage?.name ?: cp.course.targetLanguageId,
                                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(cp.cefrLevel.fullLabel, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(painter = LinguaIcons.xpBolt(), contentDescription = null,
                                    tint = Color.Unspecified, modifier = Modifier.size(14.dp))
                                Text("${cp.course.totalXpInCourse}",
                                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
