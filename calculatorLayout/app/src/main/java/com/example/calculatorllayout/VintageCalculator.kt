package com.example.calculatorllayout

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

object CalcColors {
    val Body = Color(0xFFE5E5E5)
    val DisplayBg = Color(0xFF000000)
    val DisplayDigit = Color(0xFF00E676)
    val KeyCream = Color(0xFFFFFFFF)
    val KeyGray = Color(0xFFD6D6D6)
    val KeyRed = Color(0xFFC62828)
    val KeyBlue = Color(0xFF1A237E)
    val KeyPurpleBlue = Color(0xFF3F51B5)
    val KeyOrange = Color(0xFFFF9800)
    val KeyTeal = Color(0xFF00ACC1)
}

@Composable
fun VintageCalculator(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel<CalculatorViewModel>()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFB0B8C0)),
        contentAlignment = Alignment.Center
    ) {
        // 1. Physical Calculator Body
        Surface(
            modifier = Modifier
                .width(420.dp)
                .wrapContentHeight(),
            color = CalcColors.Body,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 24.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBDBDBD))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. Display Section (Short and Wide) - NO PAPER ABOVE
                DisplaySection(viewModel.display)
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // 3. Brand and Controls
                BrandAndControls(viewModel)
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // 4. Function Button Row
                TopFunctionRow(viewModel)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 5. Main Keypad Grid
                MainKeypad(viewModel)
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DisplaySection(value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        color = CalcColors.DisplayBg,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = value,
                color = CalcColors.DisplayDigit,
                fontSize = 46.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                maxLines = 1,
                softWrap = false,
                style = TextStyle(
                    shadow = Shadow(
                        color = CalcColors.DisplayDigit.copy(alpha = 0.5f),
                        blurRadius = 8f
                    )
                )
            )
        }
    }
}

@Composable
fun BrandAndControls(viewModel: CalculatorViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(text = "SHARP", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.Black)
            Text(text = "EL-1801V", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4080A0))
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PhysicalSwitch("F 6 3 2 1 0 A", viewModel.decimalMode.name) { viewModel.toggleDecimalMode() }
            PhysicalSwitch("GT / SET", if(viewModel.isGTEnabled) "ON" else "OFF") { viewModel.toggleGT() }
            PhysicalSwitch("5/4", viewModel.roundingMode.name) { viewModel.toggleRoundingMode() }
            PhysicalSwitch("OFF P P•IC ON", "ON") { }
        }
    }
}

@Composable
fun PhysicalSwitch(label: String, state: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
        Text(text = label, fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, maxLines = 1)
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(14.dp)
                .background(Color(0xFFBDBDBD), RoundedCornerShape(2.dp))
                .border(0.5.dp, Color.Gray, RoundedCornerShape(2.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.width(10.dp).height(18.dp),
                color = Color.White,
                shape = RoundedCornerShape(1.dp),
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.LightGray)
            ) {}
        }
    }
}

@Composable
fun TopFunctionRow(viewModel: CalculatorViewModel) {
    val buttons = listOf(
        "→" to CalcColors.KeyGray,
        "CHANGE" to CalcColors.KeyTeal,
        "COST" to CalcColors.KeyGray,
        "SELL" to CalcColors.KeyGray,
        "MGN" to CalcColors.KeyGray,
        "+/-" to CalcColors.KeyGray,
        "AVG" to CalcColors.KeyGray,
        "GT" to CalcColors.KeyBlue,
        "TAX+" to CalcColors.KeyOrange,
        "TAX-" to CalcColors.KeyOrange
    )
    
    Row(
        modifier = Modifier.fillMaxWidth().height(38.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        buttons.forEach { (label, color) ->
            CalculatorButton(
                text = label,
                modifier = Modifier.weight(if (label == "CHANGE") 1.25f else 1f),
                backgroundColor = color,
                contentColor = if (color == CalcColors.KeyGray) Color.Black else Color.White,
                fontSize = if (label.length > 5) 6.sp else 8.sp,
                height = 38.dp,
                onClick = { viewModel.onButtonClick(label) }
            )
        }
    }
}

@Composable
fun MainKeypad(viewModel: CalculatorViewModel) {
    val hG = 6.dp
    val vG = 6.dp

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(hG)) {
        // Column 1: Left Operators
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(vG)) {
            CalculatorButton("÷", backgroundColor = CalcColors.KeyGray, onClick = { viewModel.onButtonClick("÷") })
            CalculatorButton("=", backgroundColor = CalcColors.KeyGray, onClick = { viewModel.onButtonClick("=") })
            CalculatorButton("×", backgroundColor = CalcColors.KeyGray, onClick = { viewModel.onButtonClick("×") })
            CalculatorButton("C/CE", backgroundColor = CalcColors.KeyGray, fontSize = 9.sp, onClick = { viewModel.onButtonClick("C/CE") })
        }

        // Columns 2-4: Number Grid
        Column(modifier = Modifier.weight(3.1f), verticalArrangement = Arrangement.spacedBy(vG)) {
            val rows = listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
                listOf("0", "00", ".")
            )
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(hG)) {
                    row.forEach { num ->
                        CalculatorButton(num, modifier = Modifier.weight(1f), backgroundColor = CalcColors.KeyCream, onClick = { viewModel.onButtonClick(num) })
                    }
                }
            }
        }

        // Column 5: Red Minus and Tall Plus
        Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(vG)) {
            CalculatorButton("-", backgroundColor = CalcColors.KeyRed, contentColor = Color.White, onClick = { viewModel.onButtonClick("-") })
            CalculatorButton("+", modifier = Modifier.fillMaxHeight(), backgroundColor = CalcColors.KeyGray, onClick = { viewModel.onButtonClick("+") })
        }

        // Column 6: Right side operators
        Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(vG)) {
            CalculatorButton("%", backgroundColor = CalcColors.KeyGray, onClick = { viewModel.onButtonClick("%") })
            CalculatorButton("# / ♢", backgroundColor = CalcColors.KeyGray, fontSize = 8.sp, onClick = { viewModel.onButtonClick("# / ♢") })
            CalculatorButton("*", modifier = Modifier.fillMaxHeight(), backgroundColor = CalcColors.KeyGray, fontSize = 20.sp, onClick = { viewModel.onButtonClick("*") })
        }

        // Column 7: Memory buttons
        Column(modifier = Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(vG)) {
            CalculatorButton("* M", backgroundColor = CalcColors.KeyPurpleBlue, contentColor = Color.White, fontSize = 8.sp, onClick = { viewModel.onButtonClick("* M") })
            CalculatorButton("♢ M", backgroundColor = CalcColors.KeyPurpleBlue, contentColor = Color.White, fontSize = 8.sp, onClick = { viewModel.onButtonClick("♢ M") })
            CalculatorButton("M -", backgroundColor = CalcColors.KeyPurpleBlue, contentColor = Color.White, fontSize = 8.sp, onClick = { viewModel.onButtonClick("M -") })
            CalculatorButton("M +", backgroundColor = CalcColors.KeyPurpleBlue, contentColor = Color.White, fontSize = 8.sp, onClick = { viewModel.onButtonClick("M +") })
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = CalcColors.KeyGray,
    contentColor: Color = Color.Black,
    fontSize: TextUnit = 16.sp,
    height: Dp = 50.dp,
    onClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val translationY by animateDpAsState(if (isPressed) 2.dp else 0.dp, label = "trans")

    Surface(
        modifier = modifier
            .padding(1.dp)
            .height(height)
            .offset(y = translationY)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(3.dp),
        shadowElevation = if (isPressed) 1.dp else 5.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.Black.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    if (!isPressed) {
                        drawRect(
                            color = Color.White.copy(alpha = 0.35f),
                            topLeft = Offset.Zero,
                            size = size.copy(height = 1.5.dp.toPx())
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.15f),
                            topLeft = Offset(0f, size.height - 2.5.dp.toPx()),
                            size = size.copy(height = 2.5.dp.toPx())
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = fontSize
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 650)
@Composable
fun VintageCalculatorPreview() {
    MaterialTheme {
        VintageCalculator()
    }
}
