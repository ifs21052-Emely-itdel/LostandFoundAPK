package com.ifs21052.lostandfound.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ifs21052.lostandfound.data.local.entity.DelcomLostandFoundEntity
import com.ifs21052.lostandfound.data.local.room.DelcomLostandFoundDatabase
import com.ifs21052.lostandfound.data.local.room.IDelcomLostandFoundDao
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Repository bertanggung jawab untuk menyediakan akses ke data.
class LocalLostandFoundRepository(context: Context) {
    private val mDelcomLostandFoundDao: IDelcomLostandFoundDao
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        // Menginisialisasi database dan mendapatkan DAO.
        val db = DelcomLostandFoundDatabase.getInstance(context)
        mDelcomLostandFoundDao = db.delcomLostandFoundDao()
    }

    // Fungsi untuk mendapatkan semua entri LostandFound dari database.
    fun getAllLostandFounds(): LiveData<List<DelcomLostandFoundEntity>?> = mDelcomLostandFoundDao.getAllLostandFounds()

    // Fungsi untuk mendapatkan entri LostandFound berdasarkan id.
    fun get(todoId: Int): LiveData<DelcomLostandFoundEntity?> = mDelcomLostandFoundDao.get(todoId)

    // Fungsi untuk menyisipkan entri LostandFound ke dalam database.
    fun insert(todo: DelcomLostandFoundEntity) {
        executorService.execute { mDelcomLostandFoundDao.insert(todo) }
    }

    // Fungsi untuk menghapus entri LostandFound dari database.
    fun delete(todo: DelcomLostandFoundEntity) {
        executorService.execute { mDelcomLostandFoundDao.delete(todo) }
    }

    // Companion object digunakan untuk menyediakan metode untuk mendapatkan instance tunggal dari repository.
    companion object {
        @Volatile
        private var INSTANCE: LocalLostandFoundRepository? = null

        // Fungsi getInstance() digunakan untuk mendapatkan instance tunggal dari repository.
        fun getInstance(
            context: Context
        ): LocalLostandFoundRepository {
            synchronized(LocalLostandFoundRepository::class.java) {
                // Membuat atau mengembalikan instance repository jika sudah ada.
                INSTANCE = INSTANCE ?: LocalLostandFoundRepository(context)
            }
            return INSTANCE as LocalLostandFoundRepository
        }
    }
}
