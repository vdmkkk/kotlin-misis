package com.example.kotlinmisis

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlinmisis.presentation.habits.create.ColorPickerAdapter
import com.example.kotlinmisis.presentation.habits.create.CreateHabitUiEvent
import com.example.kotlinmisis.presentation.habits.create.CreateHabitUiState
import com.example.kotlinmisis.presentation.habits.create.CreateHabitViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class AddHabitActivity : AppCompatActivity() {
    private val viewModel: CreateHabitViewModel by viewModels {
        val appContainer = (application as HabitsApplication).appContainer
        CreateHabitViewModel.Factory(appContainer.createHabitUseCase)
    }

    private val colorAdapter = ColorPickerAdapter { colorHex ->
        viewModel.onColorSelected(colorHex)
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var titleEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var frequencySpinner: Spinner
    private lateinit var colorPickerRecyclerView: RecyclerView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        bindViews()
        setupToolbar()
        setupFrequencySpinner()
        setupColorPicker()
        setupListeners()
        observeViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
    }

    private fun bindViews() {
        toolbar = findViewById(R.id.addHabitToolbar)
        titleInputLayout = findViewById(R.id.titleInputLayout)
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        frequencySpinner = findViewById(R.id.frequencySpinner)
        colorPickerRecyclerView = findViewById(R.id.colorPickerRecyclerView)
        saveButton = findViewById(R.id.saveHabitButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_habit_title)
    }

    private fun setupFrequencySpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.habit_frequency_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frequencySpinner.adapter = adapter
    }

    private fun setupColorPicker() {
        colorPickerRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        colorPickerRecyclerView.adapter = colorAdapter
    }

    private fun setupListeners() {
        titleEditText.doAfterTextChanged { text ->
            viewModel.onTitleChanged(text?.toString().orEmpty())
        }

        descriptionEditText.doAfterTextChanged { text ->
            viewModel.onDescriptionChanged(text?.toString().orEmpty())
        }

        frequencySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                viewModel.onFrequencySelected(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        saveButton.setOnClickListener { viewModel.onSaveClicked() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    private fun render(state: CreateHabitUiState) {
        titleInputLayout.error = state.titleError
        saveButton.isEnabled = !state.isSaving
        saveButton.text = if (state.isSaving) {
            getString(R.string.saving_button_label)
        } else {
            getString(R.string.save_habit_button)
        }

        if (frequencySpinner.selectedItemPosition != state.selectedFrequency.ordinal) {
            frequencySpinner.setSelection(state.selectedFrequency.ordinal)
        }

        colorAdapter.submitData(state.colorOptions, state.selectedColorHex)

        toolbar.subtitle = if (state.isSaving) getString(R.string.saving_habit_subtitle) else null
    }

    private fun handleEvent(event: CreateHabitUiEvent) {
        when (event) {
            CreateHabitUiEvent.CloseAfterSave -> {
                setResult(RESULT_OK)
                finish()
            }
            is CreateHabitUiEvent.ShowMessage -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
