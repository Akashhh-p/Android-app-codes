package com.example.calculatorllayout

import java.math.BigDecimal
import java.math.RoundingMode

enum class DecimalMode { F, D6, D3, D2, D1, D0, ADD }
enum class RoundingModeType { UP, CUT, R54 }

class CalculatorEngine {
    private var displayValue: String = "0"
    private var currentOperand: BigDecimal = BigDecimal.ZERO
    private var storedOperand: BigDecimal = BigDecimal.ZERO
    private var pendingOperator: String? = null
    
    private var memoryValue: BigDecimal = BigDecimal.ZERO
    private var grandTotal: BigDecimal = BigDecimal.ZERO
    private var taxRate: BigDecimal = BigDecimal("10")
    
    private var cost: BigDecimal? = null
    private var sell: BigDecimal? = null
    private var margin: BigDecimal? = null
    
    private var history = mutableListOf<BigDecimal>()
    
    private var isNewInput = true
    private var error = false
    
    var decimalMode: DecimalMode = DecimalMode.F
    var roundingMode: RoundingModeType = RoundingModeType.R54
    var isGTEnabled: Boolean = true

    var onPaperEntry: ((String, String) -> Unit)? = null

    fun inputNumber(num: String) {
        if (error) return
        
        if (decimalMode == DecimalMode.ADD && isNewInput && num != "." && num != "00") {
            currentOperand = BigDecimal(num).divide(BigDecimal("100"))
            displayValue = formatDisplay(currentOperand)
            isNewInput = false
            return
        }

        if (isNewInput) {
            displayValue = if (num == "00") "0" else num
            isNewInput = false
        } else {
            if (displayValue == "0" && num != ".") {
                displayValue = if (num == "00") "0" else num
            } else {
                if (displayValue.replace(".", "").length < 12) {
                    displayValue += num
                }
            }
        }
        currentOperand = try { BigDecimal(displayValue) } catch (e: Exception) { BigDecimal.ZERO }
    }

    fun inputDecimal() {
        if (error) return
        if (isNewInput) {
            displayValue = "0."
            isNewInput = false
        } else if (!displayValue.contains(".")) {
            displayValue += "."
        }
        currentOperand = try { BigDecimal(displayValue) } catch (e: Exception) { BigDecimal.ZERO }
    }

    fun performOperator(op: String) {
        if (error) return
        
        if (pendingOperator != null && !isNewInput) {
            calculate()
        } else {
            storedOperand = currentOperand
        }
        
        pendingOperator = op
        isNewInput = true
        addPaperEntry(storedOperand, op)
    }

    fun calculateTotal() {
        if (error) return
        if (pendingOperator != null) {
            val prevVal = currentOperand
            calculate()
            addPaperEntry(prevVal, "=")
            addPaperEntry(storedOperand, "*", true)
            
            if (isGTEnabled) {
                grandTotal = grandTotal.add(storedOperand)
            }
            pendingOperator = null
            isNewInput = true
        } else {
            addPaperEntry(currentOperand, "*", true)
            if (isGTEnabled) {
                grandTotal = grandTotal.add(currentOperand)
            }
            isNewInput = true
        }
    }

    fun subtotal() {
        if (error) return
        if (pendingOperator != null) {
            val tempTotal = when (pendingOperator) {
                "+" -> storedOperand.add(currentOperand)
                "-" -> storedOperand.subtract(currentOperand)
                "×" -> storedOperand.multiply(currentOperand)
                "÷" -> if (currentOperand.compareTo(BigDecimal.ZERO) != 0) storedOperand.divide(currentOperand, 12, RoundingMode.HALF_UP) else storedOperand
                else -> currentOperand
            }
            addPaperEntry(tempTotal, "♢")
            displayValue = formatDisplay(tempTotal)
        } else {
            addPaperEntry(currentOperand, "♢")
            displayValue = formatDisplay(currentOperand)
        }
        isNewInput = true
    }

    fun nonAdd() {
        addPaperEntry(currentOperand, "#")
        isNewInput = true
    }

    private fun calculate() {
        try {
            storedOperand = when (pendingOperator) {
                "+" -> storedOperand.add(currentOperand)
                "-" -> storedOperand.subtract(currentOperand)
                "×" -> storedOperand.multiply(currentOperand)
                "÷" -> {
                    if (currentOperand.compareTo(BigDecimal.ZERO) == 0) {
                        throw ArithmeticException("Division by zero")
                    }
                    storedOperand.divide(currentOperand, 12, RoundingMode.HALF_UP)
                }
                else -> currentOperand
            }
            displayValue = formatDisplay(storedOperand)
            currentOperand = storedOperand
        } catch (e: Exception) {
            error = true
            displayValue = "ERROR"
        }
    }

    fun clear() {
        if (displayValue != "0" || !isNewInput) {
            displayValue = "0"
            currentOperand = BigDecimal.ZERO
            isNewInput = true
        } else {
            allClear()
        }
    }

    private fun allClear() {
        displayValue = "0"
        currentOperand = BigDecimal.ZERO
        storedOperand = BigDecimal.ZERO
        pendingOperator = null
        isNewInput = true
        error = false
    }

    fun plusMinus() {
        if (error) return
        currentOperand = currentOperand.negate()
        displayValue = formatDisplay(currentOperand)
    }

    fun percent() {
        if (error) return
        if (pendingOperator == "+" || pendingOperator == "-") {
            val percentValue = storedOperand.multiply(currentOperand).divide(BigDecimal("100"))
            currentOperand = percentValue
            calculate()
            addPaperEntry(percentValue, "%")
            addPaperEntry(storedOperand, "*", true)
        } else if (pendingOperator == "×" || pendingOperator == "÷") {
            val result = if (pendingOperator == "×") {
                storedOperand.multiply(currentOperand).divide(BigDecimal("100"))
            } else {
                storedOperand.divide(currentOperand, 12, RoundingMode.HALF_UP).multiply(BigDecimal("100"))
            }
            addPaperEntry(currentOperand, "%")
            addPaperEntry(result, "*", true)
            storedOperand = result
            currentOperand = result
            displayValue = formatDisplay(result)
            pendingOperator = null
        } else {
            currentOperand = currentOperand.divide(BigDecimal("100"))
            displayValue = formatDisplay(currentOperand)
        }
        isNewInput = true
    }

    fun memoryAdd() {
        if (error) return
        if (pendingOperator != null) calculateTotal()
        memoryValue = memoryValue.add(currentOperand)
        addPaperEntry(currentOperand, "M+")
        isNewInput = true
    }

    fun memorySubtract() {
        if (error) return
        if (pendingOperator != null) calculateTotal()
        memoryValue = memoryValue.subtract(currentOperand)
        addPaperEntry(currentOperand, "M-")
        isNewInput = true
    }

    fun memoryRecall(): String {
        displayValue = formatDisplay(memoryValue)
        currentOperand = memoryValue
        isNewInput = true
        addPaperEntry(memoryValue, "MR")
        return displayValue
    }

    fun memoryClear() {
        memoryValue = BigDecimal.ZERO
        addPaperEntry(BigDecimal.ZERO, "MC")
    }

    fun getGT(): String {
        displayValue = formatDisplay(grandTotal)
        currentOperand = grandTotal
        isNewInput = true
        addPaperEntry(grandTotal, "GT")
        return displayValue
    }

    fun taxPlus() {
        val taxAmount = currentOperand.multiply(taxRate).divide(BigDecimal("100"))
        val result = currentOperand.add(taxAmount)
        addPaperEntry(currentOperand, "TAX+")
        addPaperEntry(taxAmount, "TAX")
        addPaperEntry(result, "*", true)
        currentOperand = result
        displayValue = formatDisplay(result)
        isNewInput = true
    }

    fun taxMinus() {
        val base = currentOperand.divide(BigDecimal.ONE.add(taxRate.divide(BigDecimal("100"))), 12, RoundingMode.HALF_UP)
        val taxAmount = currentOperand.subtract(base)
        addPaperEntry(currentOperand, "TAX-")
        addPaperEntry(taxAmount, "TAX")
        addPaperEntry(base, "*", true)
        currentOperand = base
        displayValue = formatDisplay(base)
        isNewInput = true
    }
    
    fun setCost() {
        cost = currentOperand
        addPaperEntry(currentOperand, "COST")
        calculateCSM()
        isNewInput = true
    }
    
    fun setSell() {
        sell = currentOperand
        addPaperEntry(currentOperand, "SELL")
        calculateCSM()
        isNewInput = true
    }
    
    fun setMargin() {
        margin = currentOperand
        addPaperEntry(currentOperand, "MGN")
        calculateCSM()
        isNewInput = true
    }
    
    private fun calculateCSM() {
        try {
            if (cost != null && sell != null) {
                margin = sell!!.subtract(cost!!).divide(sell!!, 12, RoundingMode.HALF_UP).multiply(BigDecimal("100"))
                displayValue = formatDisplay(margin!!)
                addPaperEntry(margin!!, "MGN %")
            } else if (cost != null && margin != null) {
                val m = margin!!.divide(BigDecimal("100"))
                if (m.compareTo(BigDecimal.ONE) >= 0) throw ArithmeticException("Invalid margin")
                sell = cost!!.divide(BigDecimal.ONE.subtract(m), 12, RoundingMode.HALF_UP)
                displayValue = formatDisplay(sell!!)
                addPaperEntry(sell!!, "SELL")
            } else if (sell != null && margin != null) {
                val m = margin!!.divide(BigDecimal("100"))
                cost = sell!!.multiply(BigDecimal.ONE.subtract(m))
                displayValue = formatDisplay(cost!!)
                addPaperEntry(cost!!, "COST")
            }
        } catch (e: Exception) {
            error = true
            displayValue = "ERROR"
        }
    }
    
    fun average() {
        if (history.isEmpty()) return
        val sum = history.reduce { acc, bigDecimal -> acc.add(bigDecimal) }
        val avg = sum.divide(BigDecimal(history.size), 12, RoundingMode.HALF_UP)
        displayValue = formatDisplay(avg)
        currentOperand = avg
        addPaperEntry(avg, "AVG")
        isNewInput = true
    }

    fun change(received: BigDecimal) {
        val change = received.subtract(currentOperand)
        addPaperEntry(currentOperand, "TOTAL")
        addPaperEntry(received, "CASH")
        addPaperEntry(change, "CHANGE", true)
        displayValue = formatDisplay(change)
        currentOperand = change
        isNewInput = true
    }

    private fun addPaperEntry(value: BigDecimal, symbol: String, isTotal: Boolean = false) {
        val formatted = formatDisplay(value)
        onPaperEntry?.invoke(formatted, symbol)
        if (isTotal) {
            history.add(value)
        }
    }

    private fun formatDisplay(value: BigDecimal): String {
        val rounding = when (roundingMode) {
            RoundingModeType.UP -> RoundingMode.CEILING
            RoundingModeType.CUT -> RoundingMode.FLOOR
            RoundingModeType.R54 -> RoundingMode.HALF_UP
        }
        
        val scaled = when (decimalMode) {
            DecimalMode.F -> value.stripTrailingZeros()
            DecimalMode.D6 -> value.setScale(6, rounding)
            DecimalMode.D3 -> value.setScale(3, rounding)
            DecimalMode.D2 -> value.setScale(2, rounding)
            DecimalMode.D1 -> value.setScale(1, rounding)
            DecimalMode.D0 -> value.setScale(0, rounding)
            DecimalMode.ADD -> value.setScale(2, rounding)
        }
        
        return scaled.toPlainString()
    }

    fun backspace() {
        if (error || isNewInput) return
        if (displayValue.isNotEmpty() && displayValue != "0") {
            displayValue = displayValue.substring(0, displayValue.length - 1)
            if (displayValue.isEmpty() || displayValue == "-") displayValue = "0"
            currentOperand = try { BigDecimal(displayValue) } catch (e: Exception) { BigDecimal.ZERO }
        }
    }

    fun getDisplay(): String = displayValue
}
