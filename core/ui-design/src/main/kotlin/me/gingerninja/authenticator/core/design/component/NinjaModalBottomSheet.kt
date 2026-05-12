package me.gingerninja.authenticator.core.design.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NinjaModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    dragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        modifier = Modifier
            .padding(SheetPadding)
            .then(modifier),
        shape = SheetShape,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        contentWindowInsets = {
            BottomSheetDefaults.modalWindowInsets//.exclude(WindowInsets.navigationBars)
        },
        dragHandle = null,
        containerColor = Color.Transparent,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = .6f),
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = false,
            isAppearanceLightNavigationBars = false,
        ),
    ) {
        val view = LocalView.current

        val window = remember(view) {
            var parent = view.parent
            while (parent != null && parent !is DialogWindowProvider) {
                parent = parent.parent
            }

            (parent as? DialogWindowProvider)?.window
        }

        SideEffect {
            window?.let {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    it.navigationBarColor = android.graphics.Color.TRANSPARENT
                    it.statusBarColor = android.graphics.Color.TRANSPARENT
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    it.isNavigationBarContrastEnforced = false
                }
            }
        }

        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeContent)
                .background(
                    color = BottomSheetDefaults.ContainerColor,
                    shape = SheetShape,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            dragHandle?.invoke()

            content()
        }
    }
}

private val SheetPadding = 8.dp
private val SheetShape = RoundedCornerShape(16.dp)