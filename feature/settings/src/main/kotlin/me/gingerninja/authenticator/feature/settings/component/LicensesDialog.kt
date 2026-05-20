package me.gingerninja.authenticator.feature.settings.component

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.m3.style.m3VariantColors
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesDensity
import com.mikepenz.aboutlibraries.ui.compose.variant.LibrariesVariant
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryBadges
import com.mikepenz.aboutlibraries.ui.compose.variant.LibraryDetailMode
import me.gingerninja.authenticator.core.design.component.NinjaModalBottomSheet
import me.gingerninja.authenticator.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LicensesDialog(
    show: Boolean,
    onDismiss: () -> Unit,
) {
    if (show) {
        val licenseListState = rememberLazyListState()

        NinjaModalBottomSheet(
            onDismissRequest = onDismiss,
        ) {
            val libs by produceLibraries(R.raw.aboutlibraries)

            LibrariesContainer(
                lazyListState = licenseListState,
                variantColors = LibraryDefaults.m3VariantColors(
                    rowBackground = Color.Transparent,
                ),
                libraries = libs,
                colors = LibraryDefaults.libraryColors(
                    libraryBackgroundColor = Color.Transparent,
                ),
                variant = LibrariesVariant.Traditional,
                badges = LibraryBadges(
                    author = false,
                    description = false,
                    license = true,
                    funding = false,
                    version = false,
                ),
                density = LibrariesDensity.Cozy,
                detailMode = LibraryDetailMode.Inline,
            )
        }
    }
}