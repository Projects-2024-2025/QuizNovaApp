package com.mindostech.quiznova.ui.screen.category.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.mindostech.quiznova.R
import com.mindostech.quiznova.data.local.entity.CategoryEntity
import com.mindostech.quiznova.ui.components.common.HorizontalPagerIndicator
import com.mindostech.quiznova.ui.components.getIconForCategory
import com.mindostech.quiznova.ui.components.vibrantIconColors
import kotlin.math.absoluteValue

@Composable
fun CategoryCarousel(
    categories: List<CategoryEntity>,
    onCategorySelect: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Kategoriler yükleniyor veya bulunamadı.")
        }
        return
    }
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val currentCategory by remember { derivedStateOf { categories.getOrNull(pagerState.currentPage) } }

    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp
    val screenHeightDp: Dp = configuration.screenHeightDp.dp

    val isCompactWidth = screenWidthDp < 380.dp
    val isCompactHeight = screenHeightDp < 650.dp

    val topSpacerHeight = if (isCompactHeight) 12.dp else 20.dp

    val pagerContentHorizontalPadding = when {
        screenWidthDp < 360.dp -> 40.dp
        screenWidthDp < 420.dp -> 48.dp
        else -> 56.dp
    }
    val pagerPageSpacing = if (isCompactWidth) 12.dp else 16.dp

    val indicatorVerticalPadding = if (isCompactHeight) 16.dp else 24.dp
    val indicatorHorizontalPadding = if (isCompactWidth) 24.dp else 32.dp
    val indicatorDotSize = if (isCompactWidth) 6.dp else 8.dp
    val indicatorSpacing = if (isCompactWidth) 6.dp else 8.dp

    val buttonFillMaxWidthFraction = if (isCompactWidth) 0.85f else 0.75f
    val buttonVerticalPaddingTop = if (isCompactHeight) 12.dp else 16.dp
    val buttonVerticalPaddingBottom = if (isCompactHeight) 32.dp else 48.dp
    val buttonHeight = if (isCompactHeight) 50.dp else 56.dp
    val buttonFontSize = if (isCompactWidth) 14.sp else 16.sp

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(topSpacerHeight))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = pagerContentHorizontalPadding),
            pageSpacing = pagerPageSpacing
        ) { pageIndex ->
            categories.getOrNull(pageIndex)?.let { category ->
                val icon = getIconForCategory(category.name)
                val accentColor = vibrantIconColors[pageIndex % vibrantIconColors.size]

                CategoryPageItem(
                    category = category,
                    icon = icon,
                    accentColor = accentColor,
                    isCompactWidth = isCompactWidth,
                    isCompactHeight = isCompactHeight,
                    modifier = Modifier
                        .graphicsLayer {
                            val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                            val scale = lerp(start = 0.80f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(start = 0.4f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                        }
                        .clip(MaterialTheme.shapes.extraLarge)
                )
            } ?: Spacer(Modifier.fillMaxSize(0.8f))
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .padding(vertical = indicatorVerticalPadding)
                .padding(horizontal = indicatorHorizontalPadding),
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            indicatorWidth = indicatorDotSize,
            indicatorHeight = indicatorDotSize,
            spacing = indicatorSpacing,
            indicatorShape = CircleShape,
            activeIndicatorWidthMultiplier = 2.5f
        )

        val buttonEnabled = currentCategory != null
        Button(
            onClick = { currentCategory?.let { onCategorySelect(it) } },
            enabled = buttonEnabled,
            modifier = Modifier
                .fillMaxWidth(buttonFillMaxWidthFraction)
                .padding(top = buttonVerticalPaddingTop, bottom = buttonVerticalPaddingBottom)
                .height(buttonHeight),
            shape = MaterialTheme.shapes.medium,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (buttonEnabled) 6.dp else 0.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            AnimatedContent(
                targetState = currentCategory?.name ?: "",
                label = "ButtonTextAnim",
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }
            ) { name ->
                val displayName = name.substringAfterLast(':', name).trim()
                Text(
                    text = if (displayName.isNotEmpty()) stringResource(R.string.start_quiz_button_short, displayName)
                    else stringResource(R.string.select_category_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = buttonFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}