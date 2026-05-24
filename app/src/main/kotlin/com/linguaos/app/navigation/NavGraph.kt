package com.linguaos.app.navigation

object Routes {
    const val ONBOARDING  = "onboarding"
    const val LOGIN       = "login"
    const val REGISTER    = "register"
    const val FORGOT_PW   = "forgot_password"
    const val DASHBOARD   = "dashboard"
    const val PROFILE     = "profile"
    const val UNIT_LIST   = "unit_list/{courseId}"
    const val FLASHCARDS  = "flashcards/{courseId}/{unitNumber}"
    const val PRACTICE    = "practice/{courseId}/{unitNumber}"
    const val QUIZ        = "quiz/{courseId}/{unitNumber}/{quizMode}"
    const val LEADERBOARD = "leaderboard"
    const val ACHIEVEMENTS= "achievements"
    const val USER_SELECT = "user_select"

    fun unitList(courseId: String)                           = "unit_list/$courseId"
    fun flashcards(courseId: String, unitNumber: Int)        = "flashcards/$courseId/$unitNumber"
    fun practice(courseId: String, unitNumber: Int)          = "practice/$courseId/$unitNumber"
    fun quiz(courseId: String, unitNumber: Int, mode: String)= "quiz/$courseId/$unitNumber/$mode"
}
