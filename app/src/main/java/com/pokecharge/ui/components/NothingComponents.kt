package com.pokecharge.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pokecharge.ui.theme.PcBorder
import com.pokecharge.ui.theme.PcMutedSurface
import com.pokecharge.ui.theme.PcSecondaryText

/** Small uppercase mono label, used throughout for technical/status copy. */
@Composable
fun DotMatrixLabel(text: String, modifier: Modifier = Modifier, color: Color = PcSecondaryText) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier,
    )
}

/** A minimal bordered surface in the Nothing style: near-black fill, thin hairline border, small corner radius. */
@Composable
fun NothingCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PcMutedSurface)
            .border(BorderStroke(1.dp, PcBorder), RoundedCornerShape(6.dp))
            .padding(16.dp),
    ) {
        content()
    }
}

/** A small filled circle tinted with a type's accent colour. */
@Composable
fun TypeColorDot(color: Color, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 8.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}

/** A tiny row of dots - the "dot-matrix" decorative detail called out in the brief. */
@Composable
fun DotMatrixDecoration(modifier: Modifier = Modifier, dotCount: Int = 5, color: Color = PcBorder) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(dotCount) {
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

fun parseHexColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) {
    Color.White
}
