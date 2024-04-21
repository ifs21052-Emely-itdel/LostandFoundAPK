package com.ifs21052.lostandfound.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ifs21052.lostandfound.R
import com.ifs21052.lostandfound.adapter.LostandFoundsAdapter
import com.ifs21052.lostandfound.data.remote.MyResult
import com.ifs21052.lostandfound.data.remote.response.DelcomLostandFoundsResponse
import com.ifs21052.lostandfound.data.remote.response.LostFoundsItemResponse
import com.ifs21052.lostandfound.databinding.ActivityMainBinding
import com.ifs21052.lostandfound.helper.Utils.Companion.observeOnce
import com.ifs21052.lostandfound.presentation.ViewModelFactory
import com.ifs21052.lostandfound.presentation.login.LoginActivity
import com.ifs21052.lostandfound.presentation.lostandfound.LostandFoundDetailActivity
import com.ifs21052.lostandfound.presentation.lostandfound.LostandFoundManageActivity
import com.ifs21052.lostandfound.presentation.profile.ProfileActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == LostandFoundManageActivity.RESULT_CODE || result.resultCode == LostandFoundDetailActivity.RESULT_CODE) {
            recreate()
        }
        if (result.resultCode == LostandFoundDetailActivity.RESULT_CODE){
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupAction()
    }

    private fun setupView() {
        showComponentNotEmpty(false)
        showEmptyError(false)
        showLoading(true)

        binding.appbarMain.overflowIcon =
            ContextCompat
                .getDrawable(this, R.drawable.ic_more_vert_24)

        observeGetLostandFounds()
    }

    private fun setupAction() {
        binding.appbarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mainMenuProfile -> {
                    openProfileActivity()
                    true
                }
                R.id.mainMenuLogout -> {
                    viewModel.logout()
                    openLoginActivity()
                    true
                }
                R.id.mainMenuAllData -> {
                    // Ketika menu "All Data" diklik, panggil fungsi getLostandFounds()
                    observeGetLostandFounds()
                    true
                }
                R.id.mainMenuMyData -> {
                    // Ketika menu "My Data" diklik, panggil fungsi getLostandFound()
                    observeGetMyLostandFounds()
                    true
                }
                else -> false
            }
        }

        binding.fabMainAddLostandFound.setOnClickListener {
            openAddTodoActivity()
        }

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                openLoginActivity()
            } else {
//                observeGetLostandFounds()
            }
        }
    }

    private fun observeGetLostandFounds() {
        viewModel.getLostandFounds().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }
                    is MyResult.Success -> {
                        showLoading(false)
                        loadTodosToLayout(result.data)
                    }
                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
        }
    }

    private fun loadTodosToLayout(response: DelcomLostandFoundsResponse) {
        // Periksa apakah response atau data pada response null
        if (response == null) {
            // Handle null case appropriately, misalnya menampilkan pesan error atau melakukan tindakan lainnya
            Log.e("MainActivity", "response == null")
            return
        } else if (response.data == null){
            Log.e("MainActivity", "response.data == null")
            return
        } else if (response.data.lostFounds == null){
            Log.e("MainActivity", "response.data.todos == null")
            return
        }

        val todos = response.data.lostFounds
        val layoutManager = LinearLayoutManager(this)
        binding.rvMainTodos.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(
            this,
            layoutManager.orientation
        )
        binding.rvMainTodos.addItemDecoration(itemDecoration)

        if (todos.isEmpty()) {
            showEmptyError(true)
            binding.rvMainTodos.adapter = null
        } else {
            showComponentNotEmpty(true)
            showEmptyError(false)

            val adapter = LostandFoundsAdapter()
            adapter.submitOriginalList(todos)
            binding.rvMainTodos.adapter = adapter

            adapter.setOnItemClickCallback(object : LostandFoundsAdapter.OnItemClickCallback {
                override fun onCheckedChangeListener(
                    lostandfound: LostFoundsItemResponse,
                    isChecked: Boolean
                ) {
                    adapter.filter(binding.svMain.query.toString())
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
                                        this@MainActivity,
                                        "Gagal menyelesaikan todo: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Gagal batal menyelesaikan todo: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            is MyResult.Success -> {
                                if (isChecked) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil menyelesaikan todo: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Berhasil batal menyelesaikan todo: " + lostandfound.title,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            else -> {
                            }
                        }
                    }
                }

                override fun onClickDetailListener(todoId: Int) {
                    val intent = Intent(
                        this@MainActivity,
                        LostandFoundDetailActivity::class.java
                    )
                    intent.putExtra(LostandFoundDetailActivity.KEY_TODO_ID, todoId)
                    launcher.launch(intent)
                }
            })

            binding.svMain.setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        adapter.filter(newText)
                        binding.rvMainTodos.layoutManager?.scrollToPosition(0)
                        return true
                    }
                })
        }
    }

    private fun observeGetMyLostandFounds() {
        // Panggil fungsi getLostandFounds() dengan menyertakan nilai isMe
        viewModel.getLostandFound().observe(this) { result ->
            if (result != null) {
                when (result) {
                    is MyResult.Loading -> {
                        showLoading(true)
                    }
                    is MyResult.Success -> {
                        showLoading(false)
                        loadTodosToLayout(result.data)
                    }
                    is MyResult.Error -> {
                        showLoading(false)
                        showEmptyError(true)
                    }
                }
            }
        }
    }



    private fun showLoading(isLoading: Boolean) {
        binding.pbMain.visibility =
            if (isLoading) View.VISIBLE else View.GONE
    }

    private fun openProfileActivity() {
        val intent = Intent(applicationContext, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun showComponentNotEmpty(status: Boolean) {
        binding.svMain.visibility =
            if (status) View.VISIBLE else View.GONE

        binding.rvMainTodos.visibility =
            if (status) View.VISIBLE else View.GONE
    }

    private fun showEmptyError(isError: Boolean) {
        binding.tvMainEmptyError.visibility =
            if (isError) View.VISIBLE else View.GONE
    }

    private fun openLoginActivity() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun openAddTodoActivity() {
        val intent = Intent(
            this@MainActivity,
            LostandFoundManageActivity::class.java
        )
        intent.putExtra(LostandFoundManageActivity.KEY_IS_ADD, true)
        launcher.launch(intent)
    }
}
