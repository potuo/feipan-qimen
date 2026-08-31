package com.potuo.feipanqimen2.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.potuo.feipanqimen2.qimen.QimenResult
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ExportBundle(
    val version: Int = 1,
    val exportTime: String,
    val cases: List<CaseEntity>,
)

/** 类别统计：类别名 + 案例数 */
data class CategoryStat(
    val category: String,
    val count: Int,
)

/** 案例事项类别选项（保存/筛选/统计共用） */
val CASE_CATEGORIES = listOf("求财", "事业", "婚姻", "健康", "出行", "考试", "其他")

class CaseRepository(private val dao: CaseDao) {
    private val gson = Gson()

    fun getAllCases(): Flow<List<CaseEntity>> = dao.getAllCases()

    fun searchCases(query: String): Flow<List<CaseEntity>> =
        if (query.isBlank()) dao.getAllCases() else dao.searchCases(query)

    suspend fun getCaseById(id: Long): CaseEntity? = dao.getCaseById(id)

    suspend fun insert(caseEntity: CaseEntity): Long = dao.insert(caseEntity)

    suspend fun update(caseEntity: CaseEntity) = dao.update(caseEntity)

    suspend fun delete(caseEntity: CaseEntity) = dao.delete(caseEntity)

    suspend fun getAllCasesOnce(): List<CaseEntity> = dao.getAllCasesOnce()

    fun getCasesByCategory(category: String): Flow<List<CaseEntity>> = dao.getCasesByCategory(category)

    fun searchCasesByCategory(query: String, category: String): Flow<List<CaseEntity>> =
        dao.searchCasesByCategory(query, category)

    fun searchCasesFiltered(query: String, category: String, feedbackFilter: String): Flow<List<CaseEntity>> =
        dao.searchCasesFiltered(query, category, feedbackFilter)

    fun categoryStats(): Flow<List<CategoryStat>> = dao.categoryStats()

    fun serializePan(result: QimenResult): String = gson.toJson(result)

    fun deserializePan(json: String): QimenResult = gson.fromJson(json, QimenResult::class.java)

    fun exportCases(cases: List<CaseEntity>): String {
        val bundle = ExportBundle(
            exportTime = DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
            cases = cases,
        )
        return gson.toJson(bundle)
    }

    suspend fun importCases(json: String): Result<Int> = runCatching {
        val obj = gson.fromJson(json, JsonObject::class.java)
        val version = obj.get("version")?.asInt ?: throw IllegalArgumentException("缺少 version 字段")
        if (version != 1) throw IllegalArgumentException("不支持的版本: $version")

        val casesArray = obj.getAsJsonArray("cases")
            ?: throw IllegalArgumentException("缺少 cases 字段")

        val type = object : TypeToken<List<CaseEntity>>() {}.type
        val cases: List<CaseEntity> = gson.fromJson(casesArray, type)

        var count = 0
        for (case in cases) {
            val required = listOf(case.panDate, case.panHour, case.siZhu, case.panJson)
            if (required.any { it.isBlank() }) {
                throw IllegalArgumentException("案例缺少必填字段")
            }
            val entity = case.copy(id = 0, createTime = System.currentTimeMillis() + count)
            dao.insert(entity)
            count++
        }
        count
    }
}
