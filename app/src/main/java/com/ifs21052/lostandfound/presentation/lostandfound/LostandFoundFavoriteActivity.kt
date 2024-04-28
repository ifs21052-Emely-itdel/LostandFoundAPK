package com.ifs21052.lostandfound.presentation.lostandfound

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21052.lostandfound.R
import com.ifs21052.lostandfound.adapter.LostandFoundsAdapter
import com.ifs21052.lostandfound.data.local.entity.DelcomLostandFoundEntity
import com.ifs21052.lostandfound.data.remote.MyResult
import com.ifs21052.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21052.lostandfound.databinding.ActivityLostandFoundFavoriteBinding
import com.ifs21052.lostandfound.helper.Utils.Companion.entitiesToResponses
import com.ifs21052.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21052.lostandfound.presentation.ViewModelFactory

// Deklarasi kelas LostandFoundFavoriteActivity yang merupakan turunan dari AppCompatActivity
class LostandFoundFavoriteActivity : AppCompatActivity() {
    // Deklarasi variabel view binding yang akan digunakan di dalam Activity ini
    private lateinit var binding: ActivityLostandFoundFavoriteBinding
    // Mendapatkan instance dari ViewModel menggunakan viewModels() dari library Jetpack
    private val viewModel by viewModels<LostandFoundViewModel> {
        // Mendapatkan ViewModelFactory yang telah dibuat sebelumnya
        ViewModelFactory.getInstance(this)
    }
    // Inisialisasi launcher untuk memulai aktivitas dengan hasil
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle hasil aktivitas yang kembali
        if (result.resultCode == LostandFoundDetailActivity.RESULT_CODE) {
            // Jika ada perubahan, restart Activity untuk memperbarui data
            result.data?.let {
                val isChanged = it.getBooleanExtra(
                    LostandFoundDetailActivity.KEY_IS_CHANGED,
                    false
                )
                if (isChanged) {
                    recreate()
                }
            }
        }
    }

    // Metode onCreate() yang dipanggil saat Activity pertama kali dibuat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout menggunakan view binding
        binding = ActivityLostandFoundFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Panggil metode untuk menyiapkan tampilan dan aksi
        setupView()
        setupAction()
    }

    // Metode untuk menyiapkan aksi yang akan dilakukan di dalam Activity
    private fun setupAction() {
        // Menentukan aksi saat tombol navigasi di app bar diklik
        binding.appbarLostFoundFavorite.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(LostandFoundDetailActivity.KEY_IS_CHANGED, true)
            setResult(LostandFoundDetailActivity.RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }

    // Metode untuk menyiapkan tampilan Activity
    private fun setupView() {
        // Menampilkan komponen UI yang sesuai
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)
        // Mengatur ikon overflow di app bar
        binding.appbarLostFoundFavorite.overflowIcon =
            ContextCompat.getDrawable(this, R.drawable.ic_more_vert_24)
        // Memanggil metode untuk mengamati daftar Lost and Found favorit
        observeGetLostFounds()
    }

    // Metode untuk mengamati daftar Lost and Found favorit dari ViewModel
    private fun observeGetLostFounds() {
        viewModel.getLocalLostFounds().observe(this) { lostfounds ->
            // Memuat data Lost and Found favorit ke dalam tampilan RecyclerView
            loadLostFoundsToLayout(lostfounds)
        }
    }

    // Metode untuk memuat data Lost and Found favorit ke dalam tampilan RecyclerView
    private fun loadLostFoundsToLayout(lostfounds: List<DelcomLostandFoundEntity>?) {
        // Menyembunyikan loading indicator
        showLoading(false)
        val layoutManager = LinearLayoutManager(this)
        // Menetapkan LinearLayoutManager ke RecyclerView
        binding.rvLostFoundFavoriteLostFounds.layoutManager = layoutManager
        // Menambahkan dekorasi pembatas antar item di RecyclerView
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvLostFoundFavoriteLostFounds.addItemDecoration(itemDecoration)
        if (lostfounds.isNullOrEmpty()) {
            // Menampilkan pesan jika daftar Lost and Found favorit kosong
            showEmptyError(true)
            binding.rvLostFoundFavoriteLostFounds.adapter = null
        } else {
            // Menampilkan daftar Lost and Found favorit ke dalam RecyclerView
            showComponentNotEmpty(true)
            showEmptyError(false)
            val adapter = LostandFoundsAdapter()
            // Mengirimkan daftar Lost and Found favorit ke adapter
            adapter.submitOriginalList(entitiesToResponses(lostfounds))
            binding.rvLostFoundFavoriteLostFounds.adapter = adapter
            // Menentukan aksi yang akan dilakukan saat item di RecyclerView diklik
            adapter.setOnItemClickCallback(
                object : LostandFoundsAdapter.OnItemClickCallback {
                    override fun onCheckedChangeListener(
                        lostfound: LostFoundsItemResponse,
                        isChecked: Boolean
                    ) {
                        // Handle perubahan status centang pada item di RecyclerView
                    }

                    override fun onClickDetailListener(lostfoundId: Int) {
                        // Handle klik pada item di RecyclerView untuk menampilkan detail
                    }
                })
            // Menentukan aksi yang akan dilakukan saat pengguna melakukan pencarian
            binding.svLostFoundFavorite.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        // Handle pengiriman query pencarian
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        // Handle perubahan teks pada kotak pencarian
                        adapter.filter(newText)
                        binding.rvLostFoundFavoriteLostFounds
                            .layoutManager?.scrollToPosition(0)
                        return true
                    }
                })
        }
    }

    // Metode untuk menampilkan komponen UI jika daftar Lost and Found favorit tidak kosong
    private fun showComponentNotEmpty(status: Boolean) {
        binding.svLostFoundFavorite.visibility =
            if (status) View.VISIBLE else View.GONE
        binding.rvLostFoundFavoriteLostFounds.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    // Metode untuk menampilkan pesan jika daftar Lost and Found favorit kosong
    private fun showEmptyError(isError: Boolean) {
        binding.tvLostFoundFavoriteEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }

    // Metode untuk menampilkan loading indicator
    private fun showLoading(isLoading: Boolean) {
        binding.pbLostFoundFavorite.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }
}
