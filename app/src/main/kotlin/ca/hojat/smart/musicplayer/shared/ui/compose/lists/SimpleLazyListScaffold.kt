package ca.hojat.smart.musicplayer.shared.ui.compose.lists

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ca.hojat.smart.musicplayer.shared.ui.compose.extensions.AdjustNavigationBarColors
import ca.hojat.smart.musicplayer.shared.ui.compose.extensions.MyDevices
import ca.hojat.smart.musicplayer.shared.ui.compose.extensions.plus
import ca.hojat.smart.musicplayer.shared.ui.compose.extensions.rememberMutableInteractionSource
import ca.hojat.smart.musicplayer.shared.ui.compose.theme.AppThemeSurface

@Composable
fun SimpleLazyListScaffold(
    title: String,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    state: LazyListState = rememberLazyListState(),
    lazyContent: LazyListScope.(PaddingValues) -> Unit
) {
    val context = LocalContext.current

    val (statusBarColor, contrastColor) = statusBarAndContrastColor(
        context
    )
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val (colorTransitionFraction, scrolledColor) = transitionFractionAndScrolledColor(
        scrollBehavior,
        contrastColor
    )
    SystemUISettingsScaffoldStatusBarColor(
        scrolledColor
    )
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    AdjustNavigationBarColors()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SimpleScaffoldTopBar(
                title = title,
                scrolledColor = scrolledColor,
                navigationIconInteractionSource = navigationIconInteractionSource,
                goBack = goBack,
                scrollBehavior = scrollBehavior,
                statusBarColor = statusBarColor,
                colorTransitionFraction = colorTransitionFraction,
                contrastColor = contrastColor
            )
        }
    ) { paddingValues ->
        ScreenBoxSettingsScaffold(paddingValues) {
            LazyColumn(
                modifier = Modifier
                    .matchParentSize(),
                state = state,
                contentPadding = contentPadding.plus(PaddingValues(bottom = paddingValues.calculateBottomPadding())),
                reverseLayout = reverseLayout,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled
            ) {
                lazyContent(paddingValues)
            }
        }
    }
}


@Composable
fun SimpleLazyListScaffold(
    modifier: Modifier = Modifier,
    title: @Composable (scrolledColor: Color) -> Unit,
    goBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    state: LazyListState = rememberLazyListState(),
    lazyContent: LazyListScope.(PaddingValues) -> Unit
) {
    val context = LocalContext.current

    val (statusBarColor, contrastColor) = statusBarAndContrastColor(
        context
    )
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val (colorTransitionFraction, scrolledColor) = transitionFractionAndScrolledColor(
        scrollBehavior,
        contrastColor
    )
    SystemUISettingsScaffoldStatusBarColor(
        scrolledColor
    )
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    AdjustNavigationBarColors()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SimpleScaffoldTopBar(
                title = title,
                scrolledColor = scrolledColor,
                navigationIconInteractionSource = navigationIconInteractionSource,
                goBack = goBack,
                scrollBehavior = scrollBehavior,
                statusBarColor = statusBarColor,
                colorTransitionFraction = colorTransitionFraction,
                contrastColor = contrastColor
            )
        }
    ) { paddingValues ->
        ScreenBoxSettingsScaffold(paddingValues) {
            LazyColumn(
                modifier = Modifier
                    .matchParentSize(),
                state = state,
                contentPadding = contentPadding.plus(PaddingValues(bottom = paddingValues.calculateBottomPadding())),
                reverseLayout = reverseLayout,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled,
            ) {
                lazyContent(paddingValues)
            }
        }
    }
}


@Composable
fun SimpleScaffold(
    modifier: Modifier = Modifier,
    darkStatusBarIcons: Boolean = true,
    customTopBar: @Composable (scrolledColor: Color, navigationInteractionSource: MutableInteractionSource, scrollBehavior: TopAppBarScrollBehavior, statusBarColor: Int, colorTransitionFraction: Float, contrastColor: Color) -> Unit,
    customContent: @Composable (BoxScope.(PaddingValues) -> Unit)
) {
    val context = LocalContext.current

    val (statusBarColor, contrastColor) = statusBarAndContrastColor(
        context
    )
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val (colorTransitionFraction, scrolledColor) = transitionFractionAndScrolledColor(
        scrollBehavior,
        contrastColor,
        darkStatusBarIcons
    )
    SystemUISettingsScaffoldStatusBarColor(
        scrolledColor
    )
    val navigationIconInteractionSource = rememberMutableInteractionSource()
    AdjustNavigationBarColors()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            customTopBar(
                scrolledColor,
                navigationIconInteractionSource,
                scrollBehavior,
                statusBarColor,
                colorTransitionFraction,
                contrastColor
            )
        }
    ) { paddingValues ->
        ScreenBoxSettingsScaffold(paddingValues) {
            customContent(paddingValues)
        }
    }
}


@MyDevices
@Composable
private fun SimpleLazyListScaffoldPreview() {
    AppThemeSurface {
        SimpleLazyListScaffold(title = "About", goBack = {}) {
            item {
                ListItem(headlineContent = { Text(text = "Some text") },
                    leadingContent = {
                        Icon(imageVector = Icons.Filled.AccessTime, contentDescription = null)
                    })
            }
        }
    }
}
