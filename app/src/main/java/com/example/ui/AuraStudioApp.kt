package com.example.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.StudioScreen
import com.example.viewmodel.ExportStatus
import com.example.viewmodel.StudioViewModel

object StudioRoutes {
    const val STUDIO = "studio"
    const val EXPLORE = "explore"
    const val SAVED = "saved"
}

private data class NavDestinationItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navItems = listOf(
    NavDestinationItem(
        route = StudioRoutes.STUDIO,
        labelRes = R.string.nav_studio,
        selectedIcon = Icons.Rounded.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_studio"
    ),
    NavDestinationItem(
        route = StudioRoutes.EXPLORE,
        labelRes = R.string.nav_explore,
        selectedIcon = Icons.Rounded.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTag = "nav_explore"
    ),
    NavDestinationItem(
        route = StudioRoutes.SAVED,
        labelRes = R.string.nav_saved,
        selectedIcon = Icons.Rounded.PhotoLibrary,
        unselectedIcon = Icons.Outlined.PhotoLibrary,
        testTag = "nav_saved"
    )
)

@Composable
fun AuraStudioApp(
    viewModel: StudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current

    // Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.loadImageFromUri(it) }
    }

    // SAF CreateDocument fallback for Android < 10 (API 24-28)
    val safExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToSafUri(uri)
        } else {
            viewModel.dismissExportStatus()
        }
    }

    LaunchedEffect(uiState.exportStatus) {
        val status = uiState.exportStatus
        if (status is ExportStatus.NeedsSafDestination) {
            safExportLauncher.launch(status.suggestedFilename)
        }
    }

    LaunchedEffect(uiState.shareUri) {
        val shareUri = uiState.shareUri
        if (shareUri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share HD Photo"))
            viewModel.consumeShareUri()
        }
    }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == item.route
                            } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = {
                                    NavItemIcon(
                                        item = item,
                                        selected = selected,
                                        savedCount = uiState.savedCreations.size
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        navItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == item.route
                            } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = {
                                    NavItemIcon(
                                        item = item,
                                        selected = selected,
                                        savedCount = uiState.savedCreations.size
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = StudioRoutes.STUDIO,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(StudioRoutes.STUDIO) {
                        StudioScreen(
                            uiState = uiState,
                            onPickPhoto = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onLoadSamplePortrait = {
                                viewModel.loadSamplePortrait(R.drawable.img_studio_sample)
                            },
                            onSelectCategory = viewModel::selectCategory,
                            onPromptChange = viewModel::updatePrompt,
                            onSelectPreset = viewModel::applyPreset,
                            onEnhancePrompt = viewModel::enhanceCurrentPrompt,
                            onApplyEdit = viewModel::generateAiEdit,
                            onSelectQuality = viewModel::setQuality,
                            onSelectAspect = viewModel::setAspectPreset,
                            onToggleMatchLighting = viewModel::setMatchLighting,
                            onSelectBokeh = viewModel::setBokehLevel,
                            onCompareModeChange = viewModel::setCompareMode,
                            onSelectHistoryIndex = viewModel::selectHistoryIndex,
                            onUndo = viewModel::undo,
                            onRedo = viewModel::redo,
                            onDownloadHd = viewModel::downloadHdPhoto,
                            onShare = { viewModel.prepareShareBitmap() },
                            onDismissGenerationStatus = viewModel::dismissGenerationStatus,
                            onDismissExportStatus = viewModel::dismissExportStatus
                        )
                    }

                    composable(StudioRoutes.EXPLORE) {
                        ExploreScreen(
                            onSelectPresetForStudio = { preset ->
                                viewModel.applyPreset(preset)
                                if (uiState.activeBitmap == null) {
                                    viewModel.loadSamplePortrait(R.drawable.img_studio_sample)
                                }
                                navigateToTab(StudioRoutes.STUDIO)
                            }
                        )
                    }

                    composable(StudioRoutes.SAVED) {
                        SavedScreen(
                            creations = uiState.savedCreations,
                            onReEditInStudio = { creation ->
                                viewModel.loadSavedCreationIntoStudio(creation)
                                navigateToTab(StudioRoutes.STUDIO)
                            },
                            onShareBitmap = { bitmap ->
                                viewModel.prepareShareBitmap(bitmap)
                            },
                            onDeleteCreation = viewModel::deleteCreation,
                            onGoToStudio = { navigateToTab(StudioRoutes.STUDIO) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItemIcon(
    item: NavDestinationItem,
    selected: Boolean,
    savedCount: Int
) {
    val icon = if (selected) item.selectedIcon else item.unselectedIcon
    if (item.route == StudioRoutes.SAVED && savedCount > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Text(savedCount.toString())
                }
            }
        ) {
            Icon(imageVector = icon, contentDescription = stringResource(item.labelRes))
        }
    } else {
        Icon(imageVector = icon, contentDescription = stringResource(item.labelRes))
    }
}
