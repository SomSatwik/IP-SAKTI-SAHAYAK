package com.ipsakti.sahayak.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.withStyle

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    val annotatedString = remember(text) {
        parseMarkdown(text)
    }

    val finalStyle = if (lineHeight != TextUnit.Unspecified) {
        style.copy(lineHeight = lineHeight)
    } else {
        style
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = finalStyle,
        color = if (color != Color.Unspecified) color else style.color
    )
}

fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("""(\*\*(.*?)\*\*|\*(.*?)\*)""", RegexOption.DOT_MATCHES_ALL)
        var currentIndex = 0

        regex.findAll(text).forEach { matchResult ->
            val range = matchResult.range
            if (range.first > currentIndex) {
                append(text.substring(currentIndex, range.first))
            }

            val fullMatch = matchResult.value
            if (fullMatch.startsWith("**") && fullMatch.endsWith("**") && fullMatch.length >= 4) {
                val boldContent = fullMatch.substring(2, fullMatch.length - 2)
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(boldContent)
                }
            } else if (fullMatch.startsWith("*") && fullMatch.endsWith("*") && fullMatch.length >= 2) {
                val italicContent = fullMatch.substring(1, fullMatch.length - 1)
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(italicContent)
                }
            } else {
                append(fullMatch)
            }

            currentIndex = range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
