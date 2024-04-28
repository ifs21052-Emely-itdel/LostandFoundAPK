package com.ifs21052.lostandfound.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ifs21052.lostandfound.data.local.entity.DelcomLostandFoundEntity

// Anotasi Database menandakan kelas sebagai database Room dengan entitas DelcomLostandFoundEntity.
@Database(entities = [DelcomLostandFoundEntity::class], version = 1, exportSchema = false)
abstract class DelcomLostandFoundDatabase : RoomDatabase() {
    // Fungsi abstrak yang memberikan akses ke Data Access Object (DAO) untuk DelcomLostandFoundEntity.
    abstract fun delcomLostandFoundDao(): IDelcomLostandFoundDao

    // Companion object digunakan untuk menyediakan metode untuk mengakses instance dari database.
    companion object {
        // Nama database.
        private const val Database_NAME = "DelcomLostandFound.db"
        // Variabel volatile untuk menyimpan instance database.
        @Volatile
        private var INSTANCE: DelcomLostandFoundDatabase? = null

        // Fungsi getInstance() digunakan untuk mendapatkan instance tunggal dari database.
        @JvmStatic
        fun getInstance(context: Context): DelcomLostandFoundDatabase {
            if (INSTANCE == null) {
                synchronized(DelcomLostandFoundDatabase::class.java) {
                    // Membuat instance database menggunakan databaseBuilder dari Room.
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        DelcomLostandFoundDatabase::class.java,
                        Database_NAME
                    ).build()
                }
            }
            // Mengembalikan instance database.
            return INSTANCE as DelcomLostandFoundDatabase
        }
    }
}
