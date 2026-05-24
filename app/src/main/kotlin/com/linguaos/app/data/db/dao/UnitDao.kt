package com.linguaos.app.data.db.dao

import androidx.room.*
import com.linguaos.app.data.db.entity.UnitEntity

@Dao
interface UnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(units: List<UnitEntity>)

    @Query("SELECT * FROM units WHERE targetLanguageId = :langId ORDER BY unitNumber ASC")
    suspend fun getUnitsForLanguage(langId: String): List<UnitEntity>

    @Query("SELECT * FROM units WHERE unitId = :unitId LIMIT 1")
    suspend fun getUnit(unitId: String): UnitEntity?

    @Query("SELECT COUNT(*) FROM units")
    suspend fun count(): Int
}
