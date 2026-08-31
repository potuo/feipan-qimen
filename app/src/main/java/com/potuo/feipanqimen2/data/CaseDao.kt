package com.potuo.feipanqimen2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases WHERE id = :id")
    suspend fun getCaseById(id: Long): CaseEntity?

    @Insert
    suspend fun insert(caseEntity: CaseEntity): Long

    @Update
    suspend fun update(caseEntity: CaseEntity)

    @Delete
    suspend fun delete(caseEntity: CaseEntity)

    @Query("SELECT * FROM cases ORDER BY createTime DESC")
    suspend fun getAllCasesOnce(): List<CaseEntity>

    /**
     * 组合筛选：搜索关键词 × 事项类别 × 反馈状态（'全部'/'已反馈'/'未反馈'）。
     * 反馈状态：feedback 字段非空即已反馈。
     */
    @Query(
        """
        SELECT * FROM cases WHERE
            (:query = '' OR siZhu LIKE '%' || :query || '%' OR
             CAST(juNumber AS TEXT) LIKE '%' || :query || '%' OR
             jieQi LIKE '%' || :query || '%' OR
             tags LIKE '%' || :query || '%' OR
             note LIKE '%' || :query || '%' OR
             panDate LIKE '%' || :query || '%' OR
             dunType LIKE '%' || :query || '%')
            AND (:category = '全部' OR category = :category)
            AND (:feedbackFilter = '全部'
                 OR (:feedbackFilter = '已反馈' AND feedback != '')
                 OR (:feedbackFilter = '未反馈' AND feedback = ''))
        ORDER BY createTime DESC
        """,
    )
    fun searchCasesFiltered(query: String, category: String, feedbackFilter: String): Flow<List<CaseEntity>>

    @Query("SELECT category, COUNT(*) AS count FROM cases GROUP BY category ORDER BY count DESC")
    fun categoryStats(): Flow<List<CategoryStat>>
}
