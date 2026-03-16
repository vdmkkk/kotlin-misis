package com.example.kotlinmisis

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kotlinmisis.presentation.habits.detail.HabitDetailUiEvent
import com.example.kotlinmisis.presentation.habits.detail.HabitDetailUiState
import com.example.kotlinmisis.presentation.habits.detail.HabitDetailViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class HabitDetailActivity : AppCompatActivity() {
    private val habitId: String by lazy {
        intent.getStringExtra(EXTRA_HABIT_ID) ?: ""
    }

    private val viewModel: HabitDetailViewModel by viewModels {
        val app = (application as HabitsApplication).appContainer
        HabitDetailViewModel.Factory(
            habitId,
            app.observeHabitDetailUseCase,
            app.toggleHabitCompletionUseCase,
            app.deleteHabitUseCase
        )
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var loading: ProgressBar
    private lateinit var content: ScrollView
    private lateinit var colorBanner: View
    private lateinit var titleText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var frequencyText: TextView
    private lateinit var statusText: TextView
    private lateinit var actionButton: MaterialButton
    private lateinit var currentStreakText: TextView
    private lateinit var bestStreakText: TextView
    private lateinit var historyText: TextView
    private lateinit var deleteButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_detail)

        bindViews()
        setupToolbar()
        setupActions()
        observeViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.detailToolbar)
        loading = findViewById(R.id.detailLoading)
        content = findViewById(R.id.detailContent)
        colorBanner = findViewById(R.id.detailColorBanner)
        titleText = findViewById(R.id.detailTitleText)
        descriptionText = findViewById(R.id.detailDescriptionText)
        frequencyText = findViewById(R.id.detailFrequencyText)
        statusText = findViewById(R.id.detailStatusText)
        actionButton = findViewById(R.id.detailActionButton)
        currentStreakText = findViewById(R.id.currentStreakText)
        bestStreakText = findViewById(R.id.bestStreakText)
        historyText = findViewById(R.id.detailHistoryText)
        deleteButton = findViewById(R.id.detailDeleteButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.detail_toolbar_title)
    }

    private fun setupActions() {
        actionButton.setOnClickListener { viewModel.onToggleCompletion() }

        deleteButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.detail_delete_confirm_title))
                .setMessage(getString(R.string.detail_delete_confirm_message))
                .setPositiveButton(getString(R.string.detail_delete_confirm_yes)) { _, _ ->
                    viewModel.onDeleteConfirmed()
                }
                .setNegativeButton(getString(R.string.detail_delete_confirm_no), null)
                .show()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    private fun render(state: HabitDetailUiState) {
        loading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        content.visibility = if (!state.isLoading && !state.notFound) View.VISIBLE else View.GONE

        if (state.notFound) {
            Toast.makeText(this, getString(R.string.detail_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        runCatching { colorBanner.setBackgroundColor(Color.parseColor(state.colorHex)) }
        titleText.text = state.title
        descriptionText.text = state.description
        descriptionText.visibility = if (state.description.isBlank()) View.GONE else View.VISIBLE
        frequencyText.text = state.frequencyLabel
        statusText.text = state.statusLabel
        actionButton.text = state.actionLabel
        currentStreakText.text = state.currentStreakLabel
        bestStreakText.text = state.bestStreakLabel

        historyText.text = if (state.completionHistory.isEmpty()) {
            getString(R.string.detail_no_history)
        } else {
            state.completionHistory.joinToString("\n")
        }
    }

    private fun handleEvent(event: HabitDetailUiEvent) {
        when (event) {
            is HabitDetailUiEvent.ShowMessage -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
            HabitDetailUiEvent.HabitDeleted -> {
                Toast.makeText(this, getString(R.string.detail_deleted_toast), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
    }
}
