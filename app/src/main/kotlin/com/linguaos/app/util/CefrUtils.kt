package com.linguaos.app.util

enum class CefrLevel(val label: String, val fullLabel: String, val unitsNeeded: IntRange) {
    NONE ("—",  "Not started",       0..0),
    A1   ("A1", "A1 Beginner",       1..2),
    A2   ("A2", "A2 Elementary",     3..4),
    B1   ("B1", "B1 Intermediate",   5..7),
    B2   ("B2", "B2 Upper Intermediate", 8..9),
    C1   ("C1", "C1 Advanced",       10..10)
}

object CefrUtils {
    fun fromCompletedUnits(completedUnits: Int): CefrLevel = when {
        completedUnits >= 10 -> CefrLevel.C1
        completedUnits >= 8  -> CefrLevel.B2
        completedUnits >= 5  -> CefrLevel.B1
        completedUnits >= 3  -> CefrLevel.A2
        completedUnits >= 1  -> CefrLevel.A1
        else                 -> CefrLevel.NONE
    }
}
