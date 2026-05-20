package me.gingerninja.authenticator.core.design.anim

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

fun motionEnterTransition(
    animOffset: Int
) = fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = motionEasingEmphasizedInterpolator
    )
) +
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = motionEasingEmphasizedInterpolator
            )
        ) {
            // it / 2
            animOffset
        }

fun motionExitTransition(
    animOffset: Int
) = fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = motionEasingEmphasizedInterpolator
    )
) + slideOutHorizontally(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = motionEasingEmphasizedInterpolator
    )
) {
    //-it / 2
    -animOffset
}


fun motionPopEnterTransition(
    animOffset: Int
) = fadeIn(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = motionEasingEmphasizedInterpolator
    )
) +
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = motionEasingEmphasizedInterpolator
            )
        ) {
            //-it / 2
            -animOffset
        }

fun motionPopExitTransition(
    animOffset: Int
) = fadeOut(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = motionEasingEmphasizedInterpolator
    )
) + slideOutHorizontally(
    animationSpec = tween(
        durationMillis = durationMillis,
        easing = motionEasingEmphasizedInterpolator
    )
) {
    //it / 2
    animOffset
}

val DefaultMotionAnimOffset
    @Composable get() = with(LocalDensity.current) {
        30.dp.roundToPx()
    }

private const val durationMillis = 450