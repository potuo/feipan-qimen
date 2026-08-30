package com.potuo.feipanqimen2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY createTime DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :id")
    suspend fun getCaseById(id: Long): CaseEntity?

    @Query(
        """
        SELECT * FROM cases WHERE
            siZhu LIKE '%' || :query || '%' OR
            CAST(juNumber AS TEXT) LIKE '%' || :query || '%' OR
            jieQi LIKE '%' || :query || '%' OR
            tags LIKE '%' || :query || '%' OR
            note LIKE '%' || :query || '%' OR
            panDate LIKE '%' || :query || '%' OR
            dunType LIKE '%' || :query || '%'
        ORDER BY createTime DESC
        """,
    )
    fun searchCases(query: String): Flow<List<CaseEntity>>

    @Insert
    suspend fun insert(caseEntity: CaseEntity): Long

    @Update
    suspend fun update(caseEntity: CaseEntity)

    @Delete
    suspend fun delete(caseEntity: CaseEntity)

    @Query("DELETE FROM cases")
    suspend fun deleteAll()

    @Query("SELECT * FROM cases ORDER BY createTime DESC")
    suspend fun getAllCasesOnce(): List<CaseEntity>
}
