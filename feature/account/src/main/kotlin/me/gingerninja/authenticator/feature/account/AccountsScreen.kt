package me.gingerninja.authenticator.feature.account

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarScrollBehavior
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberSearchBarWithGapState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.gingerninja.authenticator.core.codegen.CodeGeneratorState
import me.gingerninja.authenticator.core.codegen.rememberCodeGeneratorState
import me.gingerninja.authenticator.core.design.component.NinjaModalBottomSheet
import me.gingerninja.authenticator.core.model.Account
import me.gingerninja.authenticator.feature.account.component.AccountCard

@Composable
fun AccountsScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val codeGenerator = rememberCodeGeneratorState(viewModel.codeGenerator)

    AccountsScreen(
        modifier = modifier,
        state = state,
        codeGeneratorState = codeGenerator,
        onAccountClick = {
            // TODO
        },
        onSearch = viewModel::search,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AccountsScreen(
    state: AccountsUiState,
    codeGeneratorState: CodeGeneratorState,
    onAccountClick: (account: Account) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || !listState.canScrollForward || listState.lastScrolledBackward
        }
    }

    fun copyCode(code: String) {
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("", code)))
        }
    }

    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                onSearch = {
                    onSearch(it.toString())
                },
            )
        },
        /*bottomBar = {
            var selectedIndex by remember { mutableIntStateOf(0) }

            val options = listOf("Accounts", "Labels")
            val unCheckedIcons =
                listOf(
                    me.gingerninja.authenticator.core.ui.design.R.drawable.ic_key_filled,
                    me.gingerninja.authenticator.core.ui.design.R.drawable.ic_label,
                )
            val checkedIcons =
                listOf(
                    me.gingerninja.authenticator.core.ui.design.R.drawable.ic_key_filled,
                    me.gingerninja.authenticator.core.ui.design.R.drawable.ic_label,
                )

            Box(
                modifier = Modifier.fillMaxWidth()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                FlowRow(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    options.forEachIndexed { index, label ->
                        ToggleButton(
                            checked = selectedIndex == index,
                            onCheckedChange = { selectedIndex = index },
                            shapes =
                                when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                            modifier = Modifier.semantics { role = Role.RadioButton },
                        ) {
                            Icon(
                                painterResource(
                                    if (selectedIndex == index) checkedIcons[index] else unCheckedIcons[index]
                                ),
                                contentDescription = "Localized description",
                            )
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                            Text(label)
                        }
                    }
                }
                /*ButtonGroup(
                    modifier = Modifier.fillMaxWidth(),
                    overflowIndicator = {},
                    horizontalArrangement = Arrangement.Center,
                ) {
                    toggleableItem(
                        checked = selected == 0,
                        onCheckedChange = {
                            selected = 0
                        },
                        label = "Accounts",
                        icon = {
                            Icon(
                                painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_key_filled),
                                null,
                            )
                        },
                    )

                    toggleableItem(
                        checked = selected == 1,
                        onCheckedChange = {
                            selected = 1
                        },
                        label = "Labels",
                        icon = {
                            Icon(
                                painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_label),
                                null,
                            )
                        },
                    )
                }*/
            }


            /*ShortNavigationBar(
                arrangement = ShortNavigationBarArrangement.Centered
            ) {
                ShortNavigationBarItem(
                    selected = selected == 0,
                    onClick = {
                        selected = 0
                    },
                    icon = {
                        Icon(
                            painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_key_filled),
                            null,
                        )
                    },
                    iconPosition = NavigationItemIconPosition.Start,
                    label = {
                        if(selected == 0){
                            Text("Accounts")
                        }
                    },
                )

                ShortNavigationBarItem(
                    selected = selected == 1,
                    onClick = {
                        selected = 1
                    },
                    icon = {
                        Icon(
                            painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_label),
                            null,
                        )
                    },
                    iconPosition = NavigationItemIconPosition.Start,
                    label = {
                        if(selected == 1){
                            Text("Labels")
                        }
                    },
                )
            }*/
        },*/
        floatingActionButton = {
            val [fabMenuOpen, setFabMenuOpen] = remember { mutableStateOf(false) }

            TooltipBox(
                positionProvider =
                    TooltipDefaults.rememberTooltipPositionProvider(
                        TooltipAnchorPosition.Above
                    ),
                tooltip = { PlainTooltip { Text(stringResource(R.string.account_add_new_title)) } },
                state = rememberTooltipState(),
            ) {
                FloatingActionButton(
                    modifier = Modifier.animateFloatingActionButton(
                        visible = fabVisible || fabMenuOpen,
                        alignment = Alignment.Center,
                    ),
                    onClick = { setFabMenuOpen(true) },
                ) {
                    Icon(
                        painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_add),
                        contentDescription = null,
                    )
                }
            }

            if (fabMenuOpen) {
                NinjaModalBottomSheet(
                    onDismissRequest = {
                        setFabMenuOpen(false)
                    }
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        text = stringResource(R.string.account_add_new_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )

                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ListItem(
                            onClick = {},
                            leadingContent = {
                                Icon(
                                    painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_camera),
                                    contentDescription = null,
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent,
                            )
                        ) {
                            Text(stringResource(R.string.account_add_scan_camera))
                        }
                        ListItem(
                            onClick = {},
                            leadingContent = {
                                Icon(
                                    painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_image),
                                    contentDescription = null,
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent,
                            )
                        ) {
                            Text(stringResource(R.string.account_add_scan_image))
                        }
                        ListItem(
                            onClick = {},
                            leadingContent = {
                                Icon(
                                    painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_keyboard),
                                    contentDescription = null,
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent,
                            )
                        ) {
                            Text(stringResource(R.string.account_add_manual))
                        }
                    }
                }
            }

            /*FloatingActionButtonMenu(
                expanded = fabMenuOpen,
                button = {
                    ToggleFloatingActionButton(
                        modifier = Modifier.animateFloatingActionButton(
                            visible = fabVisible || fabMenuOpen,
                            alignment = Alignment.Center,
                        ),
                        checked = fabMenuOpen,
                        onCheckedChange = setFabMenuOpen,
                    ) {
                        val iconRes by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) {
                                    me.gingerninja.authenticator.core.ui.design.R.drawable.ic_close
                                } else {
                                    me.gingerninja.authenticator.core.ui.design.R.drawable.ic_add
                                }
                            }
                        }

                        Icon(
                            modifier = Modifier.animateIcon({ checkedProgress }),
                            painter = painterResource(iconRes),
                            contentDescription = null, // TODO
                        )
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = {},
                    text = {
                        Text("Scan QR code with camera")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_fingerprint),
                            contentDescription = null, // TODO
                        )
                    }
                )

                FloatingActionButtonMenuItem(
                    onClick = {},
                    text = {
                        Text("Scan QR code from image")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_fingerprint),
                            contentDescription = null, // TODO
                        )
                    }
                )

                FloatingActionButtonMenuItem(
                    onClick = {},
                    text = {
                        Text("Enter details")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_fingerprint),
                            contentDescription = null, // TODO
                        )
                    }
                )
            }*/
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = padding.plus(
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = ToggleFloatingActionButtonDefaults.containerSize()(1f) * 2,
                )
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                items = state.accounts,
                key = { it.id },
            ) { account ->
                AccountCard(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .animateItem(),
                    account = account,
                    codeGeneratorState = codeGeneratorState,
                    onClick = {
                        onAccountClick(account)
                    },
                    onCopyClick = ::copyCode,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(
    scrollBehavior: SearchBarScrollBehavior,
    onSearch: (CharSequence) -> Unit,
) {
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarWithGapState()
    val scope = rememberCoroutineScope()

    SideEffect(textFieldState.text) {
        onSearch(textFieldState.text)
    }

    val appBarWithSearchColors = SearchBarDefaults.appBarWithSearchColors(
        searchBarColors = SearchBarDefaults.containedColors(searchBarState)
    )

    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                colors = appBarWithSearchColors.searchBarColors.inputFieldColors,
                onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                placeholder = {
                    Text(modifier = Modifier.clearAndSetSemantics {}, text = "Search") // TODO translation
                },
                leadingIcon = {
                    if (searchBarState.currentValue == SearchBarValue.Expanded) {
                        IconButton(
                            onClick = {
                                scope.launch { searchBarState.animateToCollapsed() }
                            },
                        ) {
                            Icon(
                                painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_back),
                                contentDescription = null,
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_search),
                            contentDescription = null,
                        )
                    }
                },
                trailingIcon = {
                    Row {
                        if (textFieldState.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    textFieldState.clearText()
                                },
                            ) {
                                Icon(
                                    painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_close),
                                    contentDescription = null,
                                )
                            }
                        }

                        /*AnimatedVisibility(
                            visible = searchBarState.currentValue == SearchBarValue.Collapsed,
                            enter =
                                slideIn(
                                    animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                                    initialOffset = { IntOffset(it.width, 0) },
                                ),
                            exit =
                                slideOut(
                                    animationSpec = tween(durationMillis = 150, delayMillis = 0),
                                    targetOffset = { IntOffset(it.width, 0) },
                                ),
                        )*/
                        if (searchBarState.currentValue == SearchBarValue.Collapsed || searchBarState.targetValue == SearchBarValue.Collapsed) {
                            var isMenuVisible by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = {
                                        isMenuVisible = true
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_more_vertical),
                                        contentDescription = null,
                                    )
                                }

                                DropdownMenu(
                                    expanded = isMenuVisible,
                                    onDismissRequest = {
                                        isMenuVisible = false
                                    },
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text("Settings") // TODO translation
                                        },
                                        onClick = {
                                            // TODO
                                        },
                                    )
                                }
                            }
                        }
                    }
                },
            )
        }

    AppBarWithSearch(
        scrollBehavior = scrollBehavior,
        state = searchBarState,
        colors = appBarWithSearchColors,
        inputField = inputField,
        /*navigationIcon = {
            //SampleNavigationIcon(searchBarState)
        },*/
        /*actions = {
            IconButton(
                onClick = {},
            ) {
                Icon(
                    painter = painterResource(me.gingerninja.authenticator.core.ui.design.R.drawable.ic_filters),
                    contentDescription = null,
                )
            }
        },*/
        contentPadding = WindowInsets.statusBars
            .asPaddingValues()
            .plus(PaddingValues(bottom = 16.dp)),
        windowInsets = WindowInsets(0),
    )

    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = inputField,
    ) {
        /*LazyColumn(
            modifier = Modifier.background(
                ListItemDefaults.colors().containerColor,
            )
        ) {
            items(15) {
                ListItem(
                    onClick = {},
                ) {
                    Text("ncore #$it")
                }
            }
        }*/
        /*SampleSearchResults(
            onResultClick = { result ->
                textFieldState.setTextAndPlaceCursorAtEnd(result)
                scope.launch { searchBarState.animateToCollapsed() }
            }
        )*/
    }
}