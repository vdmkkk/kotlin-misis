package com.example.kotlinmisis

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmisis.presentation.habits.list.HabitAdapter
import com.example.kotlinmisis.presentation.habits.list.HabitFilter
import com.example.kotlinmisis.presentation.habits.list.HabitsUiEvent
import com.example.kotlinmisis.presentation.habits.list.HabitsUiState
import com.example.kotlinmisis.presentation.habits.list.HabitsViewModel
import com.example.kotlinmisis.presentation.habits.list.SwipeToDeleteCallback
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: HabitsViewModel by viewModels {
        val app = (application as HabitsApplication).appContainer
        HabitsViewModel.Factory(
            observeHabitsUseCase = app.observeHabitsUseCase,
            toggleHabitCompletionUseCase = app.toggleHabitCompletionUseCase,
            deleteHabitUseCase = app.deleteHabitUseCase,
            refreshHabitsUseCase = app.refreshHabitsUseCase
        )
    }

    private val habitAdapter = HabitAdapter(
        onHabitClicked = { habitId -> viewModel.onHabitClicked(habitId) },
        onHabitActionClicked = { habitId -> viewModel.onHabitCompletionClicked(habitId) }
    )

    private lateinit var rootView: View
    private lateinit var toolbar: MaterialToolbar
    private lateinit var summaryText: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var addHabitButton: FloatingActionButton
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipActive: Chip
    private lateinit var chipCompleted: Chip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupToolbar()
        setupRecyclerView()
        setupSwipeToDelete()
        setupFilterChips()
        setupActions()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun bindViews() {
        rootView = findViewById(R.id.mainContent)
        toolbar = findViewById(R.id.mainToolbar)
        summaryText = findViewById(R.id.summaryText)
        emptyStateText = findViewById(R.id.emptyStateText)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        habitsRecyclerView = findViewById(R.id.habitsRecyclerView)
        addHabitButton = findViewById(R.id.addHabitButton)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        chipAll = findViewById(R.id.chipAll)
        chipActive = findViewById(R.id.chipActive)
        chipCompleted = findViewById(R.id.chipCompleted)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.habits_title)
    }

    private fun setupRecyclerView() {
        habitsRecyclerView.layoutManager = LinearLayoutManager(this)
        habitsRecyclerView.adapter = habitAdapter
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = SwipeToDeleteCallback(habitAdapter) { habitId ->
            viewModel.onSwipeToDelete(habitId)
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(habitsRecyclerView)
    }

    private fun setupFilterChips() {
        chipAll.setOnClickListener { viewModel.onFilterSelected(HabitFilter.ALL) }
        chipActive.setOnClickListener { viewModel.onFilterSelected(HabitFilter.ACTIVE) }
        chipCompleted.setOnClickListener { viewModel.onFilterSelected(HabitFilter.COMPLETED) }
    }

    private fun setupActions() {
        addHabitButton.setOnClickListener { viewModel.onAddHabitRequested() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    private fun render(state: HabitsUiState) {
        summaryText.text = state.summaryText
        emptyStateText.visibility = if (state.emptyStateVisible) View.VISIBLE else View.GONE
        loadingIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        when (state.selectedFilter) {
            HabitFilter.ALL -> chipAll.isChecked = true
            HabitFilter.ACTIVE -> chipActive.isChecked = true
            HabitFilter.COMPLETED -> chipCompleted.isChecked = true
        }

        habitAdapter.submitList(state.habits)
    }

    private fun handleEvent(event: HabitsUiEvent) {
        when (event) {
            HabitsUiEvent.NavigateToCreateHabit -> {
                startActivity(Intent(this, AddHabitActivity::class.java))
            }

            is HabitsUiEvent.NavigateToDetail -> {
                startActivity(
                    Intent(this, HabitDetailActivity::class.java).apply {
                        putExtra(HabitDetailActivity.EXTRA_HABIT_ID, event.habitId)
                    }
                )
            }

            is HabitsUiEvent.ConfirmDelete -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.detail_delete_confirm_title))
                    .setMessage(getString(R.string.delete_confirm_message, event.habitTitle))
                    .setPositiveButton(getString(R.string.detail_delete_confirm_yes)) { _, _ ->
                        viewModel.onDeleteConfirmed(event.habitId)
                    }
                    .setNegativeButton(getString(R.string.detail_delete_confirm_no), null)
                    .show()
            }

            is HabitsUiEvent.ShowMessage -> {
                Snackbar.make(rootView, event.message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
