package com.linguaos.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.linguaos.app.data.db.dao.LanguageDao
import com.linguaos.app.data.db.dao.UnitDao
import com.linguaos.app.data.db.entity.LanguageEntity
import com.linguaos.app.data.db.entity.UnitEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class LangJson(
    val id: String,
    val name: String,
    val nativeName: String,
    val rtl: Boolean = false,
    val learnerRank: Int = 0
)

@Singleton
class LanguageSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val languageDao: LanguageDao,
    private val unitDao: UnitDao
) {
    private val unitDefs = listOf(
        Triple(1, "unit_basics",      "A1"),
        Triple(2, "unit_travel",      "A1"),
        Triple(3, "unit_food_drink",  "A2"),
        Triple(4, "unit_family",      "A2"),
        Triple(5, "unit_work_school", "B1"),
        Triple(6, "unit_shopping",    "B1"),
        Triple(7, "unit_health",      "B1"),
        Triple(8, "unit_nature",      "B2"),
        Triple(9, "unit_culture",     "B2"),
        Triple(10,"unit_advanced",    "C1")
    )

    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        if (languageDao.count() > 0) return@withContext

        val json = context.assets.open("languages.json")
            .bufferedReader().use { it.readText() }
        val root = Gson().fromJson(json, JsonObject::class.java)

        val baseList = Gson().fromJson(root.getAsJsonArray("base_languages"), Array<LangJson>::class.java)
        val targetList = Gson().fromJson(root.getAsJsonArray("target_languages"), Array<LangJson>::class.java)

        // Merge: a language can be both base and target
        val allIds = (baseList.map { it.id } + targetList.map { it.id }).toSet()
        val baseIds = baseList.map { it.id }.toSet()
        val targetIds = targetList.map { it.id }.toSet()
        val targetMap = targetList.associateBy { it.id }
        val baseMap = baseList.associateBy { it.id }

        val entities = allIds.map { id ->
            val src = targetMap[id] ?: baseMap[id]!!
            LanguageEntity(
                id = id,
                name = src.name,
                nativeName = src.nativeName,
                isRtl = src.rtl,
                isBaseLanguage = id in baseIds,
                isTargetLanguage = id in targetIds,
                learnerRank = targetMap[id]?.learnerRank ?: 0
            )
        }
        languageDao.insertAll(entities)

        // Seed unit templates for every target language
        if (unitDao.count() == 0) {
            val unitEntities = targetList.flatMap { lang ->
                unitDefs.map { (num, nameKey, cefr) ->
                    UnitEntity(
                        unitId = "${lang.id}_unit$num",
                        targetLanguageId = lang.id,
                        unitNumber = num,
                        nameKey = nameKey,
                        cefrLevel = cefr
                    )
                }
            }
            unitDao.insertAll(unitEntities)
        }
    }
}
