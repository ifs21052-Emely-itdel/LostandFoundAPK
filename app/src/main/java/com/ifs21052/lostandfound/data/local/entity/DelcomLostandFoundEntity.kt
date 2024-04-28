package com.ifs21052.lostandfound.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// Menandai kelas sebagai Parcelable untuk memudahkan pengiriman antar komponen Android.
@Parcelize
// Menandai kelas sebagai sebuah entitas dalam Room dengan nama tabel "delcom_lostandfounds".
@Entity(tableName = "delcom_lostandfounds")
data class DelcomLostandFoundEntity(
    // Menandai properti sebagai primary key dari tabel, dengan id yang tidak di-generate otomatis.
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    // Menandai properti sebagai kolom "title" dalam tabel.
    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "description")
    var description: String,

    @ColumnInfo(name = "is_completed")
    var isCompleted: Int,

    @ColumnInfo(name = "cover")
    var cover: String?,

    @ColumnInfo(name = "created_at")
    var createdAt: String,

    @ColumnInfo(name = "updated_at")
    var updatedAt: String,

    @ColumnInfo(name = "status")
    var status: String,

    @ColumnInfo(name = "userId")
    var userId: Int
) : Parcelable
