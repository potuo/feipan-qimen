package com.potuo.feipanqimen2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CaseEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        /** v1 → v2：案例新增「事项类别」字段 */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cases ADD COLUMN category TEXT NOT NULL DEFAULT '其他'")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "feipan_qimen.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
