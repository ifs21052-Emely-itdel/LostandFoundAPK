package com.ifs21052.lostandfound.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ifs21052.lostandfound.data.local.entity.DelcomLostandFoundEntity

// Anotasi Dao menandakan kelas sebagai Data Access Object untuk Room.
@Dao
interface IDelcomLostandFoundDao {
    // Fungsi untuk menyisipkan data ke dalam tabel database, dengan strategi replace jika konflik.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(delcomLostandFound: DelcomLostandFoundEntity)

    // Fungsi untuk menghapus data dari tabel database.
    @Delete
    fun delete(delcomLostandFound: DelcomLostandFoundEntity)

    // Fungsi untuk mendapatkan data berdasarkan id dari tabel database, dengan LiveData untuk pemantauan perubahan data secara real-time.
    @Query("SELECT * FROM delcom_lostandfounds WHERE id = :id LIMIT 1")
    fun get(id: Int): LiveData<DelcomLostandFoundEntity?>

    // Fungsi untuk mendapatkan semua data dari tabel database, diurutkan berdasarkan waktu dibuat, dengan LiveData untuk pemantauan perubahan data secara real-time.
    @Query("SELECT * FROM delcom_lostandfounds ORDER BY created_at DESC")
    fun getAllLostandFounds(): LiveData<List<DelcomLostandFoundEntity>?>
}
