package com.example.calculatorllayout

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CalculatorEngineTest {
    private lateinit var engine: CalculatorEngine

    @Before
    fun setUp() {
        engine = CalculatorEngine()
    }

    @Test
    fun testAddition() {
        engine.inputNumber("1")
        engine.inputNumber("0")
        engine.performOperator("+")
        engine.inputNumber("5")
        engine.calculateTotal()
        assertEquals("15", engine.getDisplay())
    }

    @Test
    fun testSubtraction() {
        engine.inputNumber("2")
        engine.inputNumber("0")
        engine.performOperator("-")
        engine.inputNumber("8")
        engine.calculateTotal()
        assertEquals("12", engine.getDisplay())
    }

    @Test
    fun testMultiplication() {
        engine.inputNumber("7")
        engine.performOperator("×")
        engine.inputNumber("6")
        engine.calculateTotal()
        assertEquals("42", engine.getDisplay())
    }

    @Test
    fun testDivision() {
        engine.inputNumber("2")
        engine.inputNumber("0")
        engine.performOperator("÷")
        engine.inputNumber("4")
        engine.calculateTotal()
        assertEquals("5", engine.getDisplay())
    }

    @Test
    fun testDecimalAddition() {
        engine.inputNumber("7")
        engine.inputDecimal()
        engine.inputNumber("5")
        engine.performOperator("+")
        engine.inputNumber("2")
        engine.inputDecimal()
        engine.inputNumber("5")
        engine.calculateTotal()
        assertEquals("10", engine.getDisplay())
    }

    @Test
    fun testPercentageAddition() {
        engine.inputNumber("1")
        engine.inputNumber("0")
        engine.inputNumber("0")
        engine.performOperator("+")
        engine.inputNumber("1")
        engine.inputNumber("0")
        engine.percent()
        assertEquals("110", engine.getDisplay())
    }

    @Test
    fun testTaxPlus() {
        engine.inputNumber("1")
        engine.inputNumber("0")
        engine.inputNumber("0")
        engine.taxPlus()
        assertEquals("110", engine.getDisplay())
    }

    @Test
    fun testMemory() {
        engine.inputNumber("1")
        engine.inputNumber("0")
        engine.inputNumber("0")
        engine.memoryAdd()
        engine.inputNumber("5")
        engine.inputNumber("0")
        engine.memoryAdd()
        assertEquals("150", engine.memoryRecall())
    }

    @Test
    fun testGrandTotal() {
        engine.inputNumber("1")
        engine.inputNumber("0")
        engine.performOperator("+")
        engine.inputNumber("5")
        engine.calculateTotal() // 15
        
        engine.inputNumber("2")
        engine.inputNumber("0")
        engine.performOperator("+")
        engine.inputNumber("1")
        engine.inputNumber("0")
        engine.calculateTotal() // 30
        
        assertEquals("45", engine.getGT())
    }

    @Test
    fun testDivisionByZero() {
        engine.inputNumber("1")
        engine.inputNumber("0")
        engine.performOperator("÷")
        engine.inputNumber("0")
        engine.calculateTotal()
        assertEquals("ERROR", engine.getDisplay())
    }
}
