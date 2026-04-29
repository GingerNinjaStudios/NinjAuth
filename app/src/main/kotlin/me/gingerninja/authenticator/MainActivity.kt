package me.gingerninja.authenticator

import android.annotation.SuppressLint
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass
import dagger.hilt.android.AndroidEntryPoint
import me.gingerninja.authenticator.core.design.anim.motionEnterTransition
import me.gingerninja.authenticator.core.design.anim.motionExitTransition
import me.gingerninja.authenticator.core.design.anim.motionPopEnterTransition
import me.gingerninja.authenticator.core.design.anim.motionPopExitTransition
import me.gingerninja.authenticator.core.design.theme.NinjAuthTheme
import me.gingerninja.authenticator.core.design.theme.isAppInDarkTheme
import me.gingerninja.authenticator.feature.auth.authScreen
import me.gingerninja.authenticator.feature.auth.navigateToAuth
import me.gingerninja.authenticator.core.ui.design.R as commonR

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

            LaunchedEffect(Unit) {
                Measure(1, MeasureUnit.PINT)
            }

            NinjAuthTheme {
                SystemBars()
                val ctx = LocalContext.current

                val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
                    rememberTopAppBarState()
                )

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(text = "NinjAuth")
                            },
                            scrollBehavior = scrollBehavior
                        )
                    },
                    bottomBar = {
                        val isMedium =
                            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

                        val iconPosition = if (isMedium) {
                            NavigationItemIconPosition.Start
                        } else {
                            NavigationItemIconPosition.Top
                        }

                        val arrangement = if (isMedium) {
                            ShortNavigationBarArrangement.Centered
                        } else {
                            ShortNavigationBarArrangement.EqualWeight
                        }

                        ShortNavigationBar(
                            arrangement = arrangement,
                        ) {
                            ShortNavigationBarItem(
                                selected = true,
                                onClick = { /*TODO*/ },
                                icon = {
                                    Icon(
                                        painter = painterResource(commonR.drawable.ic_key_filled),
                                        contentDescription = "Accounts"
                                    )
                                },
                                label = {
                                    Text("Accounts")
                                },
                                iconPosition = iconPosition,
                            )

                            ShortNavigationBarItem(
                                selected = false,
                                onClick = { /*TODO*/ },
                                icon = {
                                    Icon(
                                        painter = painterResource(commonR.drawable.ic_label),
                                        contentDescription = "Labels"
                                    )
                                },
                                label = {
                                    Text("Labels")
                                },
                                iconPosition = iconPosition,
                            )

                            ShortNavigationBarItem(
                                selected = false,
                                onClick = { /*TODO*/ },
                                icon = {
                                    Icon(
                                        painter = painterResource(commonR.drawable.ic_more_horizontal),
                                        contentDescription = "More",
                                    )
                                },
                                label = {
                                    Text("More")
                                },
                                iconPosition = iconPosition,
                            )
                        }
                        /*BottomAppBar(
                            actions = {

                                IconButton(onClick = { /* doSomething() */ }) {
                                    Icon(
                                        Icons.Filled.Menu,
                                        contentDescription = "Localized description"
                                    )
                                }

                                PlainTooltipBox(tooltip = { Text(text = "Search") }) {
                                    IconButton(
                                        modifier = Modifier.tooltipTrigger(),
                                        onClick = { /* doSomething() */ }
                                    ) {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = "Search"
                                        )
                                    }
                                }
                            }, floatingActionButton = {
                                FloatingActionButton(
                                    onClick = { /*TODO*/ },
                                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "")
                                }
                            }
                        )*/
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                // TODO
                                navController.navigateToAuth(true)
                            },
                        ) {
                            Icon(
                                painter = painterResource(commonR.drawable.ic_add),
                                contentDescription = ""
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { padding ->
                    val density = LocalDensity.current

                    val animOffset = remember(density) {
                        with(density) {
                            30.dp.roundToPx()
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "auth",
                        enterTransition = { motionEnterTransition(animOffset) },
                        exitTransition = { motionExitTransition(animOffset) },
                        popEnterTransition = { motionPopEnterTransition(animOffset) },
                        popExitTransition = { motionPopExitTransition(animOffset) }
                    ) {
                        authScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            onAuthComplete = {
                                // TODO
                            }
                        )
                    }

                    /*LazyColumn(contentPadding = padding) {
                        items(20) {
                            Card(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                onClick = {
                                    Toast.makeText(
                                        ctx,
                                        "${LocalDateTime.now()}",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            ) {
                                Box(modifier = Modifier.padding(20.dp)) {
                                    Greeting("Android #$it")
                                }
                            }
                        }
                    }*/
                }
            }
        }
    }
}

@Composable
private fun SystemBars() {
    val activity = LocalActivity.current as? ComponentActivity
    val isDark = isAppInDarkTheme()

    SideEffect {
        val navbar = if (isDark) {
            SystemBarStyle.dark(
                scrim = Color.Transparent.toArgb(),
            )
        } else {
            SystemBarStyle.light(
                scrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb(),
            )
        }

        activity?.enableEdgeToEdge(
            navigationBarStyle = navbar,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NinjAuthTheme {
        //Greeting("Android")
    }
}