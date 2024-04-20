package com.ifs21048.lostandfound.presentation.lostandfound

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.ifs21048.lostandfound.R
import com.ifs21048.lostandfound.data.model.DelcomLostandFound
import com.ifs21048.lostandfound.data.remote.MyResult
import com.ifs21048.lostandfound.data.remote.response.LostandFoundResponse
import com.ifs21048.lostandfound.databinding.ActivityLostandFoundDetailBinding
import com.ifs21048.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21048.lostandfound.presentation.ViewModelFactory

class LostandFoundDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostandFoundDetailBinding
    private val viewModel by viewModels<LostandFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var isChanged: Boolean = false

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostandFoundManageActivity.RESULT_CODE) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostandFoundDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
    }

    private fun setupView() {
        showComponent(false)
        showLoading(false)
    }

    private fun setupAction() {
        val lostandFoundId = intent.getIntExtra(KEY_TODO_ID, 0)
        if (lostandFoundId == 0) {
            finish()
            return
        }

        observeGetLostandFound(lostandFoundId)

        binding.appbarLostandFoundDetail.setNavigationOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra(KEY_IS_CHANGED, isChanged)
            setResult(RESULT_CODE, resultIntent)
            finishAfterTransition()
        }
    }

    private fun observeGetLostandFound(lostandfoundId: Int) {
        viewModel.getLostandFound(lostandfoundId).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    loadLostandFound(result.data.data.lostFound)
                }
                is MyResult.Error -> {
                    Toast.makeText(
                        this@LostandFoundDetailActivity,
                        result.error,
                        Toast.LENGTH_SHORT
                    ).show()
                    showLoading(false)
                    finishAfterTransition()
                }
            }
        }
    }

    private fun loadLostandFound(lostandfound: LostandFoundResponse?) {
        if (lostandfound != null) {
            showComponent(true)

            binding.apply {
                tvLostandFoundDetailTitle.text = lostandfound.title
                tvLostandFoundDetailDate.text = "Dibuat pada: ${lostandfound.createdAt}"
                tvLostandFoundDetailDesc.text = lostandfound.description

                cbLostandFoundDetailIsCompleted.isChecked = lostandfound.isCompleted == 1

                val status = if (lostandfound.status.equals("found", ignoreCase = true)) {
                    highlight("Found", Color.BLUE)
                } else {
                    highlight("LOST", Color.RED)
                }

                tvLostandFoundDetailStatus.text = status

                cbLostandFoundDetailIsCompleted.setOnCheckedChangeListener { _, isChecked ->
                    viewModel.putLostandFound(
                        lostandfound.id,
                        lostandfound.title,
                        lostandfound.description,
                        lostandfound.status,
                        isChecked
                    ).observeOnce {
                        when (it) {
                            is MyResult.Error -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@LostandFoundDetailActivity,
                                        "Gagal menyelesaikan data lost and found:  + ${lostandfound.title}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LostandFoundDetailActivity,
                                        "Gagal batal menyelesaikan data lost and found: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            is MyResult.Success -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@LostandFoundDetailActivity,
                                        "Berhasil menyelesaikan data lost and found: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@LostandFoundDetailActivity,
                                        "Berhasil batal menyelesaikan data lost and found: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                if ((lostandfound.isCompleted == 1) != isChecked) {
                                    isChanged = true
                                }
                            }

                            else -> {}
                        }
                    }
                }

                ivLostandFoundDetailActionDelete.setOnClickListener {
                    val builder = AlertDialog.Builder(this@LostandFoundDetailActivity)

                    // Menambahkan judul dan pesan pada dialog
                    builder.setTitle("Konfirmasi Hapus data lost and found")
                        .setMessage("Anda yakin ingin menghapus data lost and found ini?")

                    // Menambahkan tombol "Ya" dengan warna hijau
                    builder.setPositiveButton("Ya") { _, _ ->
                        observeDeleteLostandFound(lostandfound.id)
                    }

                    // Menambahkan tombol "Tidak" dengan warna merah
                    builder.setNegativeButton("Tidak") { dialog, _ ->
                        dialog.dismiss() // Menutup dialog
                    }

                    // Membuat dan menampilkan dialog
                    val dialog = builder.create()
                    dialog.show()

                    // Membuat kustomisasi warna teks pada tombol
                    val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    positiveButton.setTextColor(resources.getColor(R.color.green))
                    negativeButton.setTextColor(resources.getColor(R.color.red))
                }



                ivLostandFoundDetailActionEdit.setOnClickListener {
                    val delcomLostandFound = DelcomLostandFound(
                        lostandfound.id,
                        lostandfound.title,
                        lostandfound.description,
                        lostandfound.status,
                        lostandfound.isCompleted == 1,
                        lostandfound.cover
                    )

                    val intent = Intent(
                        this@LostandFoundDetailActivity,
                        LostandFoundManageActivity::class.java
                    )
                    intent.putExtra(LostandFoundManageActivity.KEY_IS_ADD, false)
                    intent.putExtra(LostandFoundManageActivity.KEY_TODO, delcomLostandFound)
                    launcher.launch(intent)
                }
            }
        }else {
            Toast.makeText(
                this@LostandFoundDetailActivity,
                "Tidak ditemukan item yang dicari",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun highlight(text: String, color: Int): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(color), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun observeDeleteLostandFound(lostandfoundId: Int) {
        showComponent(false)
        showLoading(true)

        viewModel.deleteLostandFound(lostandfoundId).observeOnce {
            when (it) {
                is MyResult.Error -> {
                    showComponent(true)
                    showLoading(false)
                    Toast.makeText(
                        this@LostandFoundDetailActivity,
                        "Gagal menghapus data lost and found: ${it.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is MyResult.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        this@LostandFoundDetailActivity,
                        "Berhasil menghapus data lost and found",
                        Toast.LENGTH_SHORT
                    ).show()

                    val resultIntent = Intent()
                    resultIntent.putExtra(KEY_IS_CHANGED, true)
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }
                else -> {}
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLostandFoundDetail.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showComponent(status: Boolean) {
        binding.llLostandFoundDetail.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    companion object {
        const val KEY_TODO_ID = "todo_id"
        const val KEY_IS_CHANGED = "is_changed"
        const val RESULT_CODE = 1001
    }
}
