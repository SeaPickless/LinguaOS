package com.linguaos.app.ui.gamification

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linguaos.app.R
import com.linguaos.app.ui.common.LinguaIcons

data class AchievementDef(val id: String, val iconRes: Int, val title: String, val description: String)

val ALL_ACHIEVEMENTS = listOf(
    AchievementDef("FIRST_FLASHCARD", R.drawable.ic_flashcard,           "First Card",     "Reviewed your first flashcard"),
    AchievementDef("FIRST_QUIZ",      R.drawable.ic_quiz,                "Quiz Taker",     "Completed your first quiz"),
    AchievementDef("STREAK_7",        R.drawable.ic_streak_fire,         "Week Warrior",   "Maintained a 7-day streak"),
    AchievementDef("STREAK_30",       R.drawable.ic_streak_fire,         "Monthly Master", "Maintained a 30-day streak"),
    AchievementDef("WORDS_100",       R.drawable.ic_learned,             "Century",        "Learned 100 words"),
    AchievementDef("PERFECT_QUIZ",    R.drawable.ic_star,                "Perfectionist",  "Got a perfect quiz score"),
    AchievementDef("COURSE_COMPLETE", R.drawable.ic_book,                "Graduate",       "Completed all 10 units of a course"),
    AchievementDef("RANK_ADVANCED",   R.drawable.ic_rank_advanced,       "Advanced",       "Reached the Advanced rank"),
    AchievementDef("RANK_LEGEND",     R.drawable.ic_rank_legend,         "Legend",         "Reached the Legend rank"),
    AchievementDef("COURSES_3",       R.drawable.ic_book,                "Polyglot",       "Enrolled in 3 different courses")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onBack: () -> Unit, viewModel: AchievementsViewModel = hiltViewModel()) {
    val earned by viewModel.earnedIds.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text("${earned.size} / ${ALL_ACHIEVEMENTS.size} earned",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(ALL_ACHIEVEMENTS, key = { it.id }) { def ->
                val isEarned = def.id in earned
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEarned) MaterialTheme.colorScheme.secondaryContainer
                                         else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(
                            painter = if (isEarned) painterResource(def.iconRes) else LinguaIcons.lock(),
                            contentDescription = def.title,
                            modifier = Modifier.size(32.dp),
                            colorFilter = if (!isEarned) ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)) else null
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(def.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold,
                                color = if (isEarned) MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(def.description, style = MaterialTheme.typography.bodySmall,
                                color = if (isEarned) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
