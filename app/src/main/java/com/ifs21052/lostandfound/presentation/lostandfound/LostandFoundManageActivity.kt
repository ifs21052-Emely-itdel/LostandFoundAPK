package com.ifs21052.lostandfound.presentation.lostandfound

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.ifs21052.lostandfound.R
import com.ifs21052.lostandfound.data.model.DelcomLostandFound
import com.ifs21052.lostandfound.data.remote.MyResult
import com.ifs21052.lostandfound.databinding.ActivityLostandFoundManageBinding
import com.ifs21052.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21052.lostandfound.presentation.ViewModelFactory

class LostandFoundManageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLostandFoundManageBinding
    private val viewModel by viewModels<LostandFoundViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLostandFoundManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAtion()
    }

    private fun setupView() {
        showLoading(false)

        binding.buttonLostandFound.setOnClickListener {
            // Membuat intent untuk memilih gambar dari galeri
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            // Memulai activity untuk memilih gambar dari galeri
            launcher.launch(intent)
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri = result.data?.data
            // Lakukan sesuatu dengan URI gambar yang dipilih
            // Misalnya, tampilkan gambar tersebut di ImageView
            binding.ivImageSelected.setImageURI(selectedImageUri)
        }
    }

    private fun setupAtion() {
        val isAddLostandFound = intent.getBooleanExtra(KEY_IS_ADD, true)
        if (isAddLostandFound) {
            manageAddLostandFound()
        } else {

            val delcomTodo = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    intent.getParcelableExtra(KEY_TODO, DelcomLostandFound::class.java)
                }

                else -> {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<DelcomLostandFound>(KEY_TODO)
                }
            }
            if (delcomTodo == null) {
                finishAfterTransition()
                return
            }
            manageEditLostandFound(delcomTodo)
        }

        binding.appbarTodoManage.setNavigationOnClickListener {
            finishAfterTransition()
        }
    }

    private fun manageAddLostandFound() {
        binding.apply {
            appbarTodoManage.title = "Tambah Todo"
            btnLostandFoundManageSave.setOnClickListener {
                val title = etLostandFoundManageTitle.text.toString()
                val description = etLostandFoundManageDesc.text.toString()
                val status = etLostandFoundManageStatus.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }
                observePostLostandFound(title, description, status)
            }
        }
    }

    private fun observePostLostandFound(title: String, description: String, status : String,) {
        viewModel.postLostandFound(title, description,status,).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    val resultIntent = Intent()
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }
                is MyResult.Error -> {
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun manageEditLostandFound(lostandFound: DelcomLostandFound) {
        binding.apply {
            appbarTodoManage.title = "Ubah Barang"

            etLostandFoundManageTitle.setText(lostandFound.title)
            etLostandFoundManageDesc.setText(lostandFound.description)

            val statusArray = resources.getStringArray(R.array.status)
            val statusIndex = statusArray.indexOf(lostandFound.status)
            etLostandFoundManageStatus.setSelection(statusIndex)

            btnLostandFoundManageSave.setOnClickListener {
                val title = etLostandFoundManageTitle.text.toString()
                val description = etLostandFoundManageDesc.text.toString()
                val status = etLostandFoundManageStatus.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage("Tidak boleh ada data yang kosong!")
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    return@setOnClickListener
                }

                    observePutLostandFound(lostandFound.id, title, description, status, lostandFound.isCompleted)

            }
        }
    }

    private fun observePutLostandFound(
        lostandfoundId: Int,
        title: String,
        description: String,
        status: String,
        isCompleted: Boolean,
    ) {
        viewModel.putLostandFound(
            lostandfoundId,
            title,
            description,
            status,
            isCompleted,
        ).observeOnce { result ->
            when (result) {
                is MyResult.Loading -> {
                    showLoading(true)
                }
                is MyResult.Success -> {
                    showLoading(false)
                    val resultIntent = Intent()
                    setResult(RESULT_CODE, resultIntent)
                    finishAfterTransition()
                }
                is MyResult.Error -> {
                    AlertDialog.Builder(this@LostandFoundManageActivity).apply {
                        setTitle("Oh No!")
                        setMessage(result.error)
                        setPositiveButton("Oke") { _, _ -> }
                        create()
                        show()
                    }
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLostandFoundManage.visibility =
            if (isLoading) View.VISIBLE else View.GONE

        binding.btnLostandFoundManageSave.isActivated = !isLoading

        binding.btnLostandFoundManageSave.text =
            if (isLoading) "" else "Simpan"
    }

    companion object {
        const val KEY_IS_ADD = "is_add"
        const val KEY_TODO = "todo"
        const val RESULT_CODE = 1002
    }
}
