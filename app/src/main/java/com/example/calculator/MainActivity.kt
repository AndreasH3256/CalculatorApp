
package com.example.calculator

import androidx.compose.ui.tooling.preview.Preview
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorApp()
        }
    }
}

@Composable
fun CalculatorApp() {
    var darkMode by remember { mutableStateOf(false) }

    val background = if (darkMode) Color(0xFF121212) else Color(0xFFF5F5F5)
    val displayColor = if (darkMode) Color(0xFF202020) else Color.White
    val textColor = if (darkMode) Color.White else Color(0xFF202020)

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = background
        ) {
            CalculatorScreen(
                darkMode = darkMode,
                onThemeChange = { darkMode = it },
                background = background,
                displayColor = displayColor,
                textColor = textColor
            )
        }
    }
}

@Composable
fun CalculatorScreen(
    darkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    background: Color,
    displayColor: Color,
    textColor: Color
) {
    var display by remember { mutableStateOf("0") }
    var firstNumber by remember { mutableStateOf<BigDecimal?>(null) }
    var operation by remember { mutableStateOf<String?>(null) }
    var startNewNumber by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    val haptics = LocalHapticFeedback.current
    val orange = Color(0xFFFF9800)
    val numberButton = if (darkMode) Color(0xFF303030) else Color.White
    val specialButton = if (darkMode) Color(0xFF424242) else Color(0xFFE0E0E0)

    fun formatNumber(number: BigDecimal): String {
        return number.stripTrailingZeros().toPlainString()
    }

    fun calculate() {
        val first = firstNumber ?: return
        val second = display.toBigDecimalOrNull() ?: return
        try {
            val result = when (operation) {
                "+" -> first.add(second)
                "−" -> first.subtract(second)
                "×" -> first.multiply(second)
                "÷" -> {
                    if (second.compareTo(BigDecimal.ZERO) == 0) {
                        message = "Cannot divide by zero"
                        display = "Error"
                        firstNumber = null
                        operation = null
                        startNewNumber = true
                        return
                    }
                    first.divide(second, 10, RoundingMode.HALF_UP)
                }
                else -> return
            }
            display = formatNumber(result)
            message = ""
            firstNumber = null
            operation = null
            startNewNumber = true
        } catch (_: ArithmeticException) {
            display = "Error"
            message = "Invalid calculation"
            firstNumber = null
            operation = null
            startNewNumber = true
        }
    }

    fun press(label: String) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        message = if (display == "Error") "" else message

        when {
            label in "0123456789" -> {
                if (display == "Error" || startNewNumber) {
                    display = label
                    startNewNumber = false
                } else if (display.length < 15) {
                    display = if (display == "0") label else display + label
                }
            }

            label == "." -> {
                if (display == "Error" || startNewNumber) {
                    display = "0."
                    startNewNumber = false
                } else if (!display.contains(".")) {
                    display += "."
                }
            }

            label == "C" -> {
                display = "0"
                firstNumber = null
                operation = null
                startNewNumber = true
                message = ""
            }

            label == "⌫" -> {
                if (display != "Error") {
                    display = if (display.length <= 1 ||
                        (display.length == 2 && display.startsWith("-"))) {
                        "0"
                    } else {
                        display.dropLast(1)
                    }
                    startNewNumber = false
                } else {
                    display = "0"
                    startNewNumber = true
                }
            }

            label == "%" -> {
                val value = display.toBigDecimalOrNull()
                if (value != null) {
                    display = formatNumber(
                        value.divide(BigDecimal("100"))
                    )
                    startNewNumber = true
                }
            }

            label in listOf("+", "−", "×", "÷") -> {
                val value = display.toBigDecimalOrNull()
                if (value != null) {
                    firstNumber = value
                    operation = label
                    startNewNumber = true
                    message = " $label "
                }
            }

            label == "=" -> calculate()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (darkMode) "☀ Light mode" else "☾ Dark mode",
                color = textColor,
                fontSize = 16.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onThemeChange(!darkMode) }
                    .semantics {
                        contentDescription = "Switch calculator theme"
                    }
                    .padding(12.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(displayColor)
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = message,
                color = textColor.copy(alpha = 0.65f),
                fontSize = 20.sp,
                textAlign = TextAlign.End
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = display,
                color = textColor,
                fontSize = if (display.length > 10) 30.sp else 44.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                maxLines = 2
            )
        }

        val rows = listOf(
            listOf("C", "%", "⌫", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { label ->
                    val buttonColor = when (label) {
                        "C", "⌫", "%" -> specialButton
                        "+", "−", "×", "÷", "=" -> orange
                        else -> numberButton
                    }

                    CalculatorButton(
                        label = label,
                        modifier = Modifier
                            .weight(if (label == "0" && row.size == 3) 2f else 1f)
                            .fillMaxSize(),
                        buttonColor = buttonColor,
                        textColor = if (buttonColor == orange) {
                            Color.Black
                        } else {
                            textColor
                        },
                        onClick = { press(label) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    label: String,
    modifier: Modifier = Modifier,
    buttonColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = buttonColor,
        label = "buttonColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(animatedColor)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = when (label) {
                    "⌫" -> "Backspace"
                    "C" -> "Clear"
                    "÷" -> "Divide"
                    "×" -> "Multiply"
                    "−" -> "Subtract"
                    "=" -> "Equals"
                    else -> label
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 25.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CalculatorApp()
}
