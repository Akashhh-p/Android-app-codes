package com.example.calculatorllayout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.math.BigDecimal

data class PaperEntry(val value: String, val symbol: String)

class CalculatorViewModel : ViewModel() {
    private val engine = CalculatorEngine()
    
    var display by mutableStateOf("0")
        private set
        
    val paperTape = mutableStateListOf<PaperEntry>()
    
    var decimalMode by mutableStateOf(DecimalMode.F)
    var roundingMode by mutableStateOf(RoundingModeType.R54)
    var isGTEnabled by mutableStateOf(true)
    
    private var waitingForChangeAmount = false
    private var purchaseTotal = BigDecimal.ZERO

    var onStateChanged: (() -> Unit)? = null

    init {
        engine.onPaperEntry = { value, symbol ->
            paperTape.add(PaperEntry(value, symbol))
            if (paperTape.size > 100) {
                paperTape.removeAt(0)
            }
            onStateChanged?.invoke()
        }
    }
    
    fun onButtonClick(label: String) {
        android.util.Log.d("Calculator", "Button clicked: $label")
        when (label) {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "00" -> {
                engine.inputNumber(label)
                display = engine.getDisplay()
            }
            "." -> {
                engine.inputDecimal()
                display = engine.getDisplay()
            }
            "+", "-", "×", "÷" -> {
                engine.performOperator(label)
                display = engine.getDisplay()
            }
            "=", "*" -> {
                if (waitingForChangeAmount) {
                    val received = try { BigDecimal(display) } catch (e: Exception) { BigDecimal.ZERO }
                    engine.change(received)
                    display = engine.getDisplay()
                    waitingForChangeAmount = false
                } else {
                    engine.calculateTotal()
                    display = engine.getDisplay()
                }
            }
            "% / CE", "C/CE" -> {
                engine.clear()
                display = engine.getDisplay()
                waitingForChangeAmount = false
            }
            "→" -> {
                engine.backspace()
                display = engine.getDisplay()
            }
            "%" -> {
                engine.percent()
                display = engine.getDisplay()
            }
            "+/-" -> {
                engine.plusMinus()
                display = engine.getDisplay()
            }
            "M +", "M+" -> {
                engine.memoryAdd()
                display = engine.getDisplay()
            }
            "M -", "M-" -> {
                engine.memorySubtract()
                display = engine.getDisplay()
            }
            "♢ M" -> {
                display = engine.memoryRecall()
            }
            "* M" -> {
                engine.memoryClear()
            }
            "GT" -> {
                display = engine.getGT()
            }
            "TAX+" -> {
                engine.taxPlus()
                display = engine.getDisplay()
            }
            "TAX-" -> {
                engine.taxMinus()
                display = engine.getDisplay()
            }
            "COST" -> {
                engine.setCost()
                display = engine.getDisplay()
            }
            "SELL" -> {
                engine.setSell()
                display = engine.getDisplay()
            }
            "MGN" -> {
                engine.setMargin()
                display = engine.getDisplay()
            }
            "AVG" -> {
                engine.average()
                display = engine.getDisplay()
            }
            "CHANGE" -> {
                purchaseTotal = try { BigDecimal(display) } catch (e: Exception) { BigDecimal.ZERO }
                waitingForChangeAmount = true
                engine.clear()
                display = "0"
            }
            "# / ♢" -> {
                if (engine.getDisplay() == "0") {
                    engine.subtotal()
                } else {
                    engine.nonAdd()
                }
                display = engine.getDisplay()
            }
            "↑" -> {
                paperTape.add(PaperEntry("", ""))
                onStateChanged?.invoke()
            }
        }
        onStateChanged?.invoke()
    }
    
    fun toggleDecimalMode() {
        decimalMode = when (decimalMode) {
            DecimalMode.F -> DecimalMode.D6
            DecimalMode.D6 -> DecimalMode.D3
            DecimalMode.D3 -> DecimalMode.D2
            DecimalMode.D2 -> DecimalMode.D1
            DecimalMode.D1 -> DecimalMode.D0
            DecimalMode.D0 -> DecimalMode.ADD
            DecimalMode.ADD -> DecimalMode.F
        }
        engine.decimalMode = decimalMode
        display = engine.getDisplay() // Format might change
        onStateChanged?.invoke()
    }
    
    fun toggleRoundingMode() {
        roundingMode = when (roundingMode) {
            RoundingModeType.R54 -> RoundingModeType.UP
            RoundingModeType.UP -> RoundingModeType.CUT
            RoundingModeType.CUT -> RoundingModeType.R54
        }
        engine.roundingMode = roundingMode
        display = engine.getDisplay()
        onStateChanged?.invoke()
    }
    
    fun toggleGT() {
        isGTEnabled = !isGTEnabled
        engine.isGTEnabled = isGTEnabled
        onStateChanged?.invoke()
    }
}
