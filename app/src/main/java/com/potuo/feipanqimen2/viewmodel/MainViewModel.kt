package com.potuo.feipanqimen2.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.potuo.feipanqimen2.data.AppDatabase
import com.potuo.feipanqimen2.data.CaseEntity
import com.potuo.feipanqimen2.data.CaseRepository
import com.potuo.feipanqimen2.log.LogManager
import com.potuo.feipanqimen2.qimen.HuangLiInfo
import com.potuo.feipanqimen2.qimen.HuangLiService
import com.potuo.feipanqimen2.qimen.QimenCalculator
import com.potuo.feipanqimen2.qimen.QimenConstants
import com.potuo.feipanqimen2.qimen.QimenResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CaseRepository(AppDatabase.getInstance(application).caseDao())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedHourIndex = MutableStateFlow(0)
    val selectedHourIndex: StateFlow<Int> = _selectedHourIndex.asStateFlow()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    private val _selectedCategory = MutableStateFlow("其他")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _tags = MutableStateFlow("")
    val tags: StateFlow<String> = _tags.asStateFlow()

    private val _qimenResult = MutableStateFlow<QimenResult?>(null)
    val qimenResult: StateFlow<QimenResult?> = _qimenResult.asStateFlow()

    private val _huangLi = MutableStateFlow<HuangLiInfo?>(null)
    val huangLi: StateFlow<HuangLiInfo?> = _huangLi.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow("全部")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    private val _feedbackFilter = MutableStateFlow("全部")
    val feedbackFilter: StateFlow<String> = _feedbackFilter.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val cases = combine(_searchQuery, _categoryFilter, _feedbackFilter) { q, c, f -> Triple(q, c, f) }
        .flatMapLatest { (q, c, f) ->
            repository.searchCasesFiltered(q, c, f)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoryStats = repository.categoryStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDate(date: LocalDate) { _selectedDate.value = date }
    fun setHourIndex(index: Int) { _selectedHourIndex.value = index }
    fun setNote(note: String) { _note.value = note }
    fun setTags(tags: String) { _tags.value = tags }
    fun setCategory(category: String) { _selectedCategory.value = category }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategoryFilter(category: String) { _categoryFilter.value = category }
    fun setFeedbackFilter(filter: String) { _feedbackFilter.value = filter }
    fun clearMessage() { _message.value = null }

    fun calculate() {
        val dt = buildDateTime()
        val label = "${_selectedDate.value} ${QimenConstants.HOUR_NAMES[_selectedHourIndex.value]}"
        LogManager.log("排盘", "请求排盘：$label")
        try {
            _qimenResult.value = QimenCalculator.calculate(dt)
            _huangLi.value = HuangLiService.getHuangLi(dt)
            val r = _qimenResult.value
            LogManager.log(
                "排盘",
                "成功：${r?.siZhu} ${r?.jieQi}${r?.yuan}${r?.dunType}${r?.juNumber}局 值符${r?.zhiFuStar}@${r?.zhiFuPalace} 值使${r?.zhiShiGate}@${r?.zhiShiPalace}",
            )
        } catch (e: Throwable) {
            LogManager.logException("排盘 $label", e)
            throw e
        }
    }

    fun saveCase() {
        val result = _qimenResult.value ?: return
        val huangLi = _huangLi.value
        viewModelScope.launch {
            try {
                val entity = CaseEntity(
                    panDate = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    panHour = QimenConstants.HOUR_NAMES[_selectedHourIndex.value],
                    siZhu = result.siZhu,
                    jieQi = result.jieQi,
                    yuan = result.yuan,
                    dunType = result.dunType,
                    juNumber = result.juNumber,
                    panJson = repository.serializePan(result),
                    category = _selectedCategory.value,
                    tags = _tags.value,
                    note = _note.value,
                    huangLi = huangLi?.summary ?: "",
                )
                repository.insert(entity)
                LogManager.log("案例", "保存：${entity.siZhu} ${entity.dunType}${entity.juNumber}局")
                _message.value = "案例已保存"
            } catch (e: Throwable) {
                LogManager.logException("保存案例", e)
                _message.value = "保存失败"
            }
        }
    }

    fun loadCase(case: CaseEntity) {
        _selectedDate.value = LocalDate.parse(case.panDate)
        val hourIdx = QimenConstants.HOUR_NAMES.indexOf(case.panHour).coerceAtLeast(0)
        _selectedHourIndex.value = hourIdx
        _note.value = case.note
        _tags.value = case.tags
        _qimenResult.value = repository.deserializePan(case.panJson)
        _huangLi.value = if (case.huangLi.isNotBlank()) {
            HuangLiService.getHuangLi(buildDateTime())
        } else null
    }

    fun updateCase(case: CaseEntity, tags: String, note: String, feedback: String) {
        viewModelScope.launch {
            repository.update(case.copy(tags = tags, note = note, feedback = feedback))
            _message.value = "已保存"
        }
    }

    fun deleteCase(case: CaseEntity) {
        viewModelScope.launch {
            repository.delete(case)
            _message.value = "已删除"
        }
    }

    suspend fun getCaseById(id: Long): CaseEntity? = repository.getCaseById(id)

    fun exportAll(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val cases = repository.getAllCasesOnce()
                val json = repository.exportCases(cases)
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("无法写入文件")
                _message.value = "已导出 ${cases.size} 条案例"
            }.onFailure {
                _message.value = "导出失败：${it.message}"
            }
        }
    }

    fun exportOne(case: CaseEntity, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val json = repository.exportCases(listOf(case))
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("无法写入文件")
                _message.value = "已导出案例"
            }.onFailure {
                _message.value = "导出失败：${it.message}"
            }
        }
    }

    fun importCases(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val json = getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                    it.readBytes().toString(Charsets.UTF_8)
                } ?: throw IllegalStateException("无法读取文件")
                val count = repository.importCases(json).getOrThrow()
                _message.value = "导入成功 $count 条"
            }.onFailure {
                _message.value = "导入失败：${it.message}"
            }
        }
    }

    private fun buildDateTime(): LocalDateTime {
        val date = _selectedDate.value
        val hourRange = QimenConstants.HOUR_RANGES[_selectedHourIndex.value]
        val hour = if (hourRange.first == 23) 23 else hourRange.first
        return LocalDateTime.of(date.year, date.month, date.dayOfMonth, hour, 0)
    }
}
