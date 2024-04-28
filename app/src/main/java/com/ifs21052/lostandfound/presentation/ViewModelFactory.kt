package com.ifs21052.lostandfound.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ifs21052.lostandfound.data.repository.AuthRepository
import com.ifs21052.lostandfound.data.repository.LocalLostandFoundRepository
import com.ifs21052.lostandfound.data.repository.LostandFoundRepository
import com.ifs21052.lostandfound.data.repository.UserRepository
import com.ifs21052.lostandfound.di.Injection
import com.ifs21052.lostandfound.presentation.login.LoginViewModel
import com.ifs21052.lostandfound.presentation.lostandfound.LostandFoundViewModel
import com.ifs21052.lostandfound.presentation.main.MainViewModel
import com.ifs21052.lostandfound.presentation.profile.ProfileViewModel
import com.ifs21052.lostandfound.presentation.register.RegisterViewModel

// Kelas ViewModelFactory bertanggung jawab untuk memberikan instance ViewModel yang sesuai.
class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val lostandfoundRepository: LostandFoundRepository,
    private val localLostandFoundRepository: LocalLostandFoundRepository
) : ViewModelProvider.NewInstanceFactory() {

    // Override fungsi create untuk membuat instance ViewModel yang sesuai.
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Mengembalikan instance ViewModel yang sesuai berdasarkan tipe modelClass.
        return when {
            // Jika modelClass adalah RegisterViewModel, kembalikan instance RegisterViewModel.
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel.getInstance(authRepository) as T
            }
            // Jika modelClass adalah LoginViewModel, kembalikan instance LoginViewModel.
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel.getInstance(authRepository) as T
            }
            // Jika modelClass adalah MainViewModel, kembalikan instance MainViewModel.
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel.getInstance(authRepository, lostandfoundRepository) as T
            }
            // Jika modelClass adalah ProfileViewModel, kembalikan instance ProfileViewModel.
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel.getInstance(authRepository, userRepository) as T
            }
            // Jika modelClass adalah LostandFoundViewModel, kembalikan instance LostandFoundViewModel.
            modelClass.isAssignableFrom(LostandFoundViewModel::class.java) -> {
                LostandFoundViewModel
                    .getInstance(lostandfoundRepository, localLostandFoundRepository) as T
            }
            // Jika modelClass tidak cocok dengan ViewModel yang dikenal, lemparkan IllegalArgumentException.
            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: " + modelClass.name
            )
        }
    }

    companion object {
        // Variabel untuk menyimpan instance singleton dari ViewModelFactory.
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        // Fungsi untuk mendapatkan instance singleton dari ViewModelFactory.
        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            synchronized(ViewModelFactory::class.java) {
                // Buat atau kembalikan instance ViewModelFactory jika sudah ada.
                INSTANCE = ViewModelFactory(
                    Injection.provideAuthRepository(context),
                    Injection.provideUserRepository(context),
                    Injection.provideLostandFoundRepository(context),
                    Injection.provideLocalLostandFoundRepository(context),
                )
            }
            return INSTANCE as ViewModelFactory
        }
    }
}
