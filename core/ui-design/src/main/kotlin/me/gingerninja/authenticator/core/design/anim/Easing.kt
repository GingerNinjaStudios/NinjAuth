package me.gingerninja.authenticator.core.design.anim

import androidx.compose.animation.core.PathEasing
import androidx.compose.ui.graphics.Path

val motionEasingEmphasizedInterpolator = PathEasing(
    Path().apply {
        moveTo(0f, 0f)
        cubicTo(0.05f, 0f, 0.133333f, 0.06f, 0.166666f, 0.4f)
        cubicTo(0.208333f, 0.82f, 0.25f, 1f, 1f, 1f)
    }
)