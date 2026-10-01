package com.example.movieposterlayout

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.movieposterlayout.fragments.*

class MainActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var cbTable: CheckBox
    private lateinit var cbGrid: CheckBox
    private lateinit var cbScroll: CheckBox
    private lateinit var cbRecycler: CheckBox
    private lateinit var spinner: Spinner

    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radioGroup = findViewById(R.id.radioGroup)
        cbTable = findViewById(R.id.cbTable)
        cbGrid = findViewById(R.id.cbGrid)
        cbScroll = findViewById(R.id.cbScroll)
        cbRecycler = findViewById(R.id.cbRecycler)
        spinner = findViewById(R.id.spinner)

        setupSpinner()
        setupListeners()

        // Default selection: Table
        updateSelection(0)
    }

    private fun setupSpinner() {
        val options = arrayOf("Table view", "Grid view", "Scroll view & card view", "Recycler view")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupListeners() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdating) return@setOnCheckedChangeListener
            val index = when (checkedId) {
                R.id.rbTable -> 0
                R.id.rbGrid -> 1
                R.id.rbScroll -> 2
                R.id.rbRecycler -> 3
                else -> 0
            }
            updateSelection(index)
        }

        val checkBoxListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isUpdating || !isChecked) return@OnCheckedChangeListener
            val index = when (buttonView.id) {
                R.id.cbTable -> 0
                R.id.cbGrid -> 1
                R.id.cbScroll -> 2
                R.id.cbRecycler -> 3
                else -> 0
            }
            updateSelection(index)
        }

        cbTable.setOnCheckedChangeListener(checkBoxListener)
        cbGrid.setOnCheckedChangeListener(checkBoxListener)
        cbScroll.setOnCheckedChangeListener(checkBoxListener)
        cbRecycler.setOnCheckedChangeListener(checkBoxListener)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isUpdating) return
                updateSelection(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateSelection(index: Int) {
        isUpdating = true

        // 1. Replace Fragment
        val fragment: Fragment = when (index) {
            0 -> TableViewFragment()
            1 -> GridViewFragment()
            2 -> ScrollViewFragment()
            3 -> RecycleViewFragment()
            else -> TableViewFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        // 2. Update RadioButtons
        when (index) {
            0 -> radioGroup.check(R.id.rbTable)
            1 -> radioGroup.check(R.id.rbGrid)
            2 -> radioGroup.check(R.id.rbScroll)
            3 -> radioGroup.check(R.id.rbRecycler)
        }

        // 3. Update CheckBoxes
        cbTable.isChecked = index == 0
        cbGrid.isChecked = index == 1
        cbScroll.isChecked = index == 2
        cbRecycler.isChecked = index == 3

        // 4. Update Spinner
        spinner.setSelection(index)

        isUpdating = false
    }
}
