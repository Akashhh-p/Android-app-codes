package com.example.calculatorllayout

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "CalculatorLifecycle"
    }

    private lateinit var displayText: TextView
    private val engine = CalculatorEngine()
    private var waitingForChangeAmount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_main)

        displayText = findViewById(R.id.displayText)
        updateDisplay()

        setupButtons()
        setupSwitches()
    }

    private fun setupButtons() {
        // Number buttons mapping
        val numberButtonsMap = mapOf(
            R.id.btn0 to "0",
            R.id.btn00 to "00",
            R.id.btn1 to "1",
            R.id.btn2 to "2",
            R.id.btn3 to "3",
            R.id.btn4 to "4",
            R.id.btn5 to "5",
            R.id.btn6 to "6",
            R.id.btn7 to "7",
            R.id.btn8 to "8",
            R.id.btn9 to "9",
        )

        numberButtonsMap.forEach { (id, digit) ->
            findViewById<Button>(id)?.setOnClickListener {
                engine.inputNumber(digit)
                updateDisplay()
            }
        }

        // Decimal button
        findViewById<Button>(R.id.btnDot)?.setOnClickListener {
            engine.inputDecimal()
            updateDisplay()
        }

        // Basic Operators
        findViewById<Button>(R.id.btnPlus)?.setOnClickListener {
            engine.performOperator("+")
            updateDisplay()
        }
        findViewById<Button>(R.id.btnMinus)?.setOnClickListener {
            engine.performOperator("-")
            updateDisplay()
        }
        findViewById<Button>(R.id.btnMultiply)?.setOnClickListener {
            engine.performOperator("×")
            updateDisplay()
        }
        findViewById<Button>(R.id.btnDivide)?.setOnClickListener {
            engine.performOperator("÷")
            updateDisplay()
        }

        // Equals (=) and Star (*) Total buttons
        val onCalculateAction = {
            if (waitingForChangeAmount) {
                val received = try { BigDecimal(engine.getDisplay()) } catch (_: Exception) { BigDecimal.ZERO }
                engine.change(received)
                waitingForChangeAmount = false
            } else {
                engine.calculateTotal()
            }
            updateDisplay()
        }
        findViewById<Button>(R.id.btnEquals)?.setOnClickListener { onCalculateAction() }
        findViewById<Button>(R.id.btnStar)?.setOnClickListener { onCalculateAction() }

        // Clear / CE
        findViewById<Button>(R.id.btnCE)?.setOnClickListener {
            engine.clear()
            waitingForChangeAmount = false
            updateDisplay()
        }

        // Backspace / Shift (→)
        findViewById<Button>(R.id.btnBack)?.setOnClickListener {
            engine.backspace()
            updateDisplay()
        }

        // Percentage (%)
        findViewById<Button>(R.id.btnPercent)?.setOnClickListener {
            engine.percent()
            updateDisplay()
        }

        // Plus / Minus (+/-)
        findViewById<Button>(R.id.btnPlusMinus)?.setOnClickListener {
            engine.plusMinus()
            updateDisplay()
        }

        // Memory buttons
        findViewById<Button>(R.id.btnMPlus)?.setOnClickListener {
            engine.memoryAdd()
            updateDisplay()
        }
        findViewById<Button>(R.id.btnMMinus)?.setOnClickListener {
            engine.memorySubtract()
            updateDisplay()
        }
        findViewById<Button>(R.id.btnMDiamond)?.setOnClickListener {
            engine.memoryRecall()
            updateDisplay()
        }
        findViewById<Button>(R.id.btnMStar)?.setOnClickListener {
            engine.memoryClear()
            updateDisplay()
        }

        // GT, AVG, TAX+, TAX-
        findViewById<Button>(R.id.btnGT)?.setOnClickListener {
            engine.getGT()
            updateDisplay()
        }
        findViewById<Button>(R.id.btnAvg)?.setOnClickListener {
            engine.average()
            updateDisplay()
        }
        findViewById<Button>(R.id.btnTaxPlus)?.setOnClickListener {
            engine.taxPlus()
            updateDisplay()
        }
        findViewById<Button>(R.id.btnTaxMinus)?.setOnClickListener {
            engine.taxMinus()
            updateDisplay()
        }

        // COST, SELL, MGN
        findViewById<Button>(R.id.btnCost)?.setOnClickListener {
            engine.setCost()
            updateDisplay()
        }
        findViewById<Button>(R.id.btnSell)?.setOnClickListener {
            engine.setSell()
            updateDisplay()
        }
        findViewById<Button>(R.id.btnMgn)?.setOnClickListener {
            engine.setMargin()
            updateDisplay()
        }

        // CHANGE
        findViewById<Button>(R.id.btnChange)?.setOnClickListener {
            waitingForChangeAmount = true
            engine.clear()
            updateDisplay()
        }

        // Non-add / Subtotal (#/◇)
        findViewById<Button>(R.id.btnHashDiamond)?.setOnClickListener {
            if (engine.getDisplay() == "0") {
                engine.subtotal()
            } else {
                engine.nonAdd()
            }
            updateDisplay()
        }
    }

    private fun setupSwitches() {
        bindToggleSwitch(R.id.decimalSwitch, R.id.decimalKnob, initialChecked = true) { isChecked ->
            engine.decimalMode = if (isChecked) DecimalMode.D2 else DecimalMode.F
            updateDisplay()
        }
        bindToggleSwitch(R.id.gtRateSwitch, R.id.gtRateKnob, initialChecked = false) { isChecked ->
            engine.isGTEnabled = isChecked
        }
        bindToggleSwitch(R.id.roundSwitch, R.id.roundKnob, initialChecked = true) { isChecked ->
            engine.roundingMode = if (isChecked) RoundingModeType.R54 else RoundingModeType.CUT
            updateDisplay()
        }
        bindToggleSwitch(R.id.modeSwitch, R.id.modeKnob, initialChecked = true) { _ ->
            // Mode switch visual feedback
        }
    }

    private fun bindToggleSwitch(
        containerId: Int,
        knobId: Int,
        initialChecked: Boolean = false,
        onToggle: (Boolean) -> Unit,
    ) {
        val container = findViewById<View>(containerId) ?: return
        val knob = findViewById<View>(knobId) ?: return
        var isChecked = initialChecked

        fun updateKnobPosition() {
            val params = (knob.layoutParams as? FrameLayout.LayoutParams) ?: return
            params.gravity = Gravity.CENTER_VERTICAL or (
                if (isChecked) Gravity.END else Gravity.START
            )
            knob.layoutParams = params
        }

        updateKnobPosition()

        container.setOnClickListener {
            isChecked = !isChecked
            updateKnobPosition()
            onToggle(isChecked)
        }
    }

    private fun updateDisplay() {
        displayText.text = engine.getDisplay()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }
}
