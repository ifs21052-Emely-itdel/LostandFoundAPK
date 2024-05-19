package com.ifs21052.lostandfound.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.ifs21052.lostandfound.data.local.entity.DelcomLostandFoundEntity
import com.ifs21052.lostandfound.data.remote.MyResult
import com.ifs21052.lostandfound.data.remote.response.AuthorLostandFoundsResponse
import com.ifs21052.lostandfound.data.remote.response.LostFoundsItemResponse

// Kelas utilitas untuk menyimpan fungsi-fungsi bantuan.
class Utils {
    companion object {
        // Fungsi untuk mengamati LiveData sekali saja.
        fun <T> LiveData<T>.observeOnce(observer: (T) -> Unit) {
            // Buat observer baru.
            val observerWrapper = object : Observer<T> {
                // Implementasi onChanged.
                override fun onChanged(value: T) {
                    // Panggil observer yang diberikan.
                    observer(value)
                    // Hapus observer setelah panggilan pertama jika nilai adalah MyResult.Success atau MyResult.Error.
                    if (value is MyResult.Success<*> || value is MyResult.Error) {
                        removeObserver(this)
                    }
                }
            }
            // Amati LiveData secara permanen dengan observer yang baru dibuat.
            observeForever(observerWrapper)
        }

        // Fungsi untuk mengonversi daftar entitas DelcomLostandFoundEntity menjadi daftar respons LostFoundsItemResponse.
        fun entitiesToResponses(entities: List<DelcomLostandFoundEntity>): List<LostFoundsItemResponse> {
            // Gunakan fungsi map untuk melakukan transformasi setiap entitas menjadi respons yang sesuai.
            return entities.map {
                // Konversi setiap entitas menjadi respons yang sesuai.
                LostFoundsItemResponse(
                    cover = it.cover ?: "", // Gunakan cover jika ada, jika tidak gunakan string kosong.
                    updatedAt = it.updatedAt,
                    userId = it.userId, // Sesuaikan dengan kebutuhan Anda, karena tidak ada field yang cocok di DelcomLostFoundEntity.
                    author = AuthorLostandFoundsResponse(
                        name = "Unknown", // Nama penulis default adalah "Unknown".
                        photo = "" // Foto penulis default adalah string kosong.
                    ),
                    description = it.description,
                    createdAt = it.createdAt,
                    id = it.id,
                    title = it.title,
                    isCompleted = it.isCompleted,
//                    isMe = 1,
                    status = it.status
                )
            }
        }
    }
}
