package com.ifs21052.lostandfound.di

import android.content.Context
import com.ifs21052.lostandfound.data.pref.UserPreference
import com.ifs21052.lostandfound.data.pref.dataStore
import com.ifs21052.lostandfound.data.remote.retrofit.ApiConfig
import com.ifs21052.lostandfound.data.remote.retrofit.IApiService
import com.ifs21052.lostandfound.data.repository.AuthRepository
import com.ifs21052.lostandfound.data.repository.LocalLostandFoundRepository
import com.ifs21052.lostandfound.data.repository.LostandFoundRepository
import com.ifs21052.lostandfound.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    // Fungsi untuk menyediakan repository untuk otentikasi.
    fun provideAuthRepository(context: Context): AuthRepository {
        // Mendapatkan instance UserPreference dari dataStore.
        val pref = UserPreference.getInstance(context.dataStore)
        // Mendapatkan data pengguna dari sesi.
        val user = runBlocking { pref.getSession().first() }
        // Mendapatkan instance IApiService dengan token pengguna.
        val apiService: IApiService = ApiConfig.getApiService(user.token)
        // Mengembalikan instance AuthRepository.
        return AuthRepository.getInstance(pref, apiService)
    }

    // Fungsi untuk menyediakan repository untuk pengguna.
    fun provideUserRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService: IApiService = ApiConfig.getApiService(user.token)
        return UserRepository.getInstance(apiService)
    }

    // Fungsi untuk menyediakan repository untuk entri LostandFound dari server.
    fun provideLostandFoundRepository(context: Context): LostandFoundRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService: IApiService = ApiConfig.getApiService(user.token)
        return LostandFoundRepository.getInstance(apiService)
    }

    // Fungsi untuk menyediakan repository untuk entri LostandFound lokal.
    fun provideLocalLostandFoundRepository(context: Context): LocalLostandFoundRepository {
        // Mengembalikan instance LocalLostandFoundRepository.
        return LocalLostandFoundRepository.getInstance(context)
    }
}
