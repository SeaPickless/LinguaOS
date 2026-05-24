package com.linguaos.app.util

enum class Rank(val label: String, val minXp: Int, val maxXp: Int) {
    NEWCOMER          ("Newcomer",           0,      99),
    BEGINNER          ("Beginner",           100,    499),
    ELEMENTARY        ("Elementary",         500,    1499),
    INTERMEDIATE      ("Intermediate",       1500,   3999),
    UPPER_INTERMEDIATE("Upper Intermediate", 4000,   8999),
    ADVANCED          ("Advanced",           9000,   19999),
    EXPERT            ("Expert",             20000,  49999),
    MASTER            ("Master",             50000,  99999),
    LEGEND            ("Legend",             100000, Int.MAX_VALUE)
}

object RankUtils {
    fun fromXp(xp: Int): Rank = Rank.values().last { xp >= it.minXp }

    fun xpToNextRank(xp: Int): Int? {
        val next = Rank.values().firstOrNull { xp < it.minXp } ?: return null
        return next.minXp - xp
    }

    fun progressToNextRank(xp: Int): Float {
        val current = fromXp(xp)
        if (current == Rank.LEGEND) return 1f
        val range = (current.maxXp - current.minXp + 1).toFloat()
        val progress = (xp - current.minXp).toFloat()
        return (progress / range).coerceIn(0f, 1f)
    }
}
