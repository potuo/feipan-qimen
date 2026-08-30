package com.potuo.feipanqimen2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createTime: Long = System.currentTimeMillis(),
    val panDate: String,
    val panHour: String,
    val siZhu: String,
    val jieQi: String,
    val yuan: String,
    val dunType: String,
    val juNumber: Int,
    val panJson: String,
    val category: String = "其他",
    val tags: String = "",
    val note: String = "",
    val huangLi: String = "",
)
