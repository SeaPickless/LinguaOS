package com.linguaos.app.ui.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.linguaos.app.ui.common.LinguaIcons
import com.linguaos.app.util.RankUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit, viewModel: LeaderboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (state.isLoading) { Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return@Scaffold }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            itemsIndexed(state.users, key = { _, u -> u.id }) { index, user ->
                val rank = RankUtils.fromXp(user.totalXp)
                val isCurrentUser = user.id == state.currentUserId
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer
                                         else MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Medal podium icons using rank icon for top 3, number text otherwise
                        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                            if (index < 3) {
                                val podiumRanks = listOf(com.linguaos.app.util.Rank.ADVANCED, com.linguaos.app.util.Rank.INTERMEDIATE, com.linguaos.app.util.Rank.ELEMENTARY)
                                Icon(painter = LinguaIcons.rankIcon(podiumRanks[index]),
                                    contentDescription = null, tint = Color.Unspecified, modifier = Modifier.fillMaxSize())
                            } else {
                                Text("#${index + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center) {
                            Text(user.username.take(2).uppercase(), style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.username + if (isCurrentUser) " (you)" else "",
                                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(painter = LinguaIcons.rankIcon(rank), contentDescription = null,
                                    tint = Color.Unspecified, modifier = Modifier.size(14.dp))
                                Text(rank.label, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(painter = LinguaIcons.xpBolt(), contentDescription = null,
                                tint = Color.Unspecified, modifier = Modifier.size(14.dp))
                            Text("${user.totalXp}", style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
