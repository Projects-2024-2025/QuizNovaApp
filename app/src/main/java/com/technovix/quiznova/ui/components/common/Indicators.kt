package com.technovix.quiznova.ui.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    indicatorWidth: Dp = 8.dp,
    indicatorHeight: Dp = indicatorWidth,
    spacing: Dp = indicatorWidth,
    indicatorShape: Shape = CircleShape,
    activeIndicatorWidthMultiplier: Float = 2.0f
) {
    val activeIndicatorWidth = indicatorWidth * activeIndicatorWidthMultiplier
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val currentPage = pagerState.currentPage
        if (pagerState.pageCount > 0) { // Sayfa sayısı 0'dan büyükse göster
            repeat(pagerState.pageCount) { iteration ->
                val width by animateDpAsState(
                    targetValue = if (currentPage == iteration) activeIndicatorWidth else indicatorWidth,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    label = "IndicatorWidthAnim"
                )
                val color = if (currentPage == iteration) activeColor else inactiveColor
                Box(
                    modifier = Modifier
                        .clip(indicatorShape)
                        .background(color)
                        .height(indicatorHeight)
                        .width(width)
                )
            }
        }
    }
}