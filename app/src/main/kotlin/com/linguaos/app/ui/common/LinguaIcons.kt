package com.linguaos.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.linguaos.app.R
import com.linguaos.app.util.Rank

/** Central place that maps every icon/badge to its vector drawable. */
object LinguaIcons {

    @Composable fun streak(): Painter         = painterResource(R.drawable.ic_streak_fire)
    @Composable fun xpBolt(): Painter         = painterResource(R.drawable.ic_xp_bolt)
    @Composable fun trophy(): Painter         = painterResource(R.drawable.ic_trophy)
    @Composable fun lock(): Painter           = painterResource(R.drawable.ic_lock)
    @Composable fun star(): Painter           = painterResource(R.drawable.ic_star)
    @Composable fun book(): Painter           = painterResource(R.drawable.ic_book)
    @Composable fun flashcard(): Painter      = painterResource(R.drawable.ic_flashcard)
    @Composable fun practice(): Painter       = painterResource(R.drawable.ic_practice)
    @Composable fun quiz(): Painter           = painterResource(R.drawable.ic_quiz)
    @Composable fun leaderboard(): Painter    = painterResource(R.drawable.ic_leaderboard)
    @Composable fun audio(): Painter          = painterResource(R.drawable.ic_audio)
    @Composable fun flip(): Painter           = painterResource(R.drawable.ic_flip)
    @Composable fun checkCircle(): Painter    = painterResource(R.drawable.ic_check_circle)
    @Composable fun closeCircle(): Painter    = painterResource(R.drawable.ic_close_circle)
    @Composable fun swipeRight(): Painter     = painterResource(R.drawable.ic_swipe_right)
    @Composable fun swipeLeft(): Painter      = painterResource(R.drawable.ic_swipe_left)
    @Composable fun learned(): Painter        = painterResource(R.drawable.ic_learned)
    @Composable fun chevronRight(): Painter   = painterResource(R.drawable.ic_chevron_right)

    @Composable fun rankIcon(rank: Rank): Painter = painterResource(
        when (rank) {
            Rank.NEWCOMER          -> R.drawable.ic_rank_newcomer
            Rank.BEGINNER          -> R.drawable.ic_rank_beginner
            Rank.ELEMENTARY        -> R.drawable.ic_rank_elementary
            Rank.INTERMEDIATE      -> R.drawable.ic_rank_intermediate
            Rank.UPPER_INTERMEDIATE-> R.drawable.ic_rank_upper_intermediate
            Rank.ADVANCED          -> R.drawable.ic_rank_advanced
            Rank.EXPERT            -> R.drawable.ic_rank_expert
            Rank.MASTER            -> R.drawable.ic_rank_master
            Rank.LEGEND            -> R.drawable.ic_rank_legend
        }
    )

    /** Tint colour that matches the rank. */
    fun rankColor(rank: Rank): Color = when (rank) {
        Rank.NEWCOMER          -> Color(0xFF9CA3AF)
        Rank.BEGINNER          -> Color(0xFFCD7F32)
        Rank.ELEMENTARY        -> Color(0xFF22C55E)
        Rank.INTERMEDIATE      -> Color(0xFF3B82F6)
        Rank.UPPER_INTERMEDIATE-> Color(0xFFA855F7)
        Rank.ADVANCED          -> Color(0xFFEAB308)
        Rank.EXPERT            -> Color(0xFFEF4444)
        Rank.MASTER            -> Color(0xFF67E8F9)
        Rank.LEGEND            -> Color(0xFFFBBF24)
    }
}
