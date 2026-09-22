package com.xcloak.spacexpert

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.xcloak.spacexpert.ui.CleanScreen
import com.xcloak.spacexpert.ui.FilesScreen
import com.xcloak.spacexpert.ui.HomeScreen
import com.xcloak.spacexpert.ui.RecoverScreen
import com.xcloak.spacexpert.ui.SettingsScreen
import com.xcloak.spacexpert.ui.VaultScreen
import com.xcloak.spacexpert.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import com.xcloak.spacexpert.ui.theme.*
import androidx.compose.foundation.background

enum class AppTab(val label: String) {
    HOME("Home"), CLEAN("Clean"), FILES("Files"), RECOVER("Recover"), VAULT("Vault")
}

@AndroidEntryPoint
class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpaceXpertTheme {
                Surface {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val prefs = remember { context.getSharedPreferences("spacexpert_prefs", android.content.Context.MODE_PRIVATE) }
                    var onboardingCompleted by remember { mutableStateOf(prefs.getBoolean("onboarding_completed", false)) }

                    if (!onboardingCompleted) {
                        com.xcloak.spacexpert.ui.OnboardingScreen(
                            onFinished = {
                                prefs.edit().putBoolean("onboarding_completed", true).apply()
                                onboardingCompleted = true
                            }
                        )
                    } else {
                        var granted by remember { mutableStateOf(PermissionUtils.hasAllPermissions(this@MainActivity)) }

                        val launcher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestMultiplePermissions()
                        ) { result -> granted = result.values.all { it } }

                        LaunchedEffect(Unit) {
                            if (!granted) launcher.launch(PermissionUtils.requiredPermissions())
                        }

                        if (granted) {
                            AppScaffold()
                        } else {
                            PermissionRationaleScreen(
                                onRequestAgain = { launcher.launch(PermissionUtils.requiredPermissions()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppScaffold() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var isSettingsVisible by remember { mutableStateOf(false) }
    var hasAllFilesAccess by remember { mutableStateOf(PermissionUtils.hasAllFilesAccess()) }

    val allFilesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasAllFilesAccess = PermissionUtils.hasAllFilesAccess()
    }

    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp > 600
    val colors = LocalSpaceXpertColors.current

    Row(Modifier.fillMaxSize().background(colors.backgroundGradient)) {
        if (isWideScreen) {
            NavigationRail(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationRailItem(
                    selected = selectedTab == AppTab.HOME && !isSettingsVisible,
                    onClick = { selectedTab = AppTab.HOME; isSettingsVisible = false },
                    icon = { Icon(Icons.Filled.Home, contentDescription = AppTab.HOME.label) },
                    label = { Text(AppTab.HOME.label) }
                )
                NavigationRailItem(
                    selected = selectedTab == AppTab.CLEAN,
                    onClick = { selectedTab = AppTab.CLEAN; isSettingsVisible = false },
                    icon = { Icon(Icons.Filled.CleaningServices, contentDescription = AppTab.CLEAN.label) },
                    label = { Text(AppTab.CLEAN.label) }
                )
                NavigationRailItem(
                    selected = selectedTab == AppTab.FILES,
                    onClick = { selectedTab = AppTab.FILES; isSettingsVisible = false },
                    icon = { Icon(Icons.Filled.Folder, contentDescription = AppTab.FILES.label) },
                    label = { Text(AppTab.FILES.label) }
                )
                NavigationRailItem(
                    selected = selectedTab == AppTab.RECOVER,
                    onClick = { selectedTab = AppTab.RECOVER; isSettingsVisible = false },
                    icon = { Icon(Icons.Filled.Restore, contentDescription = AppTab.RECOVER.label) },
                    label = { Text(AppTab.RECOVER.label) }
                )
                NavigationRailItem(
                    selected = selectedTab == AppTab.VAULT,
                    onClick = { selectedTab = AppTab.VAULT; isSettingsVisible = false },
                    icon = { Icon(Icons.Filled.Lock, contentDescription = AppTab.VAULT.label) },
                    label = { Text(AppTab.VAULT.label) }
                )
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (!isWideScreen && !isSettingsVisible) {
                    NavigationBar(
                        containerColor = colors.glassBackground,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == AppTab.HOME,
                            onClick = { selectedTab = AppTab.HOME },
                            icon = { Icon(Icons.Filled.Home, contentDescription = AppTab.HOME.label) },
                            label = { Text(AppTab.HOME.label) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppTab.CLEAN,
                            onClick = { selectedTab = AppTab.CLEAN },
                            icon = { Icon(Icons.Filled.CleaningServices, contentDescription = AppTab.CLEAN.label) },
                            label = { Text(AppTab.CLEAN.label) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppTab.FILES,
                            onClick = { selectedTab = AppTab.FILES },
                            icon = { Icon(Icons.Filled.Folder, contentDescription = AppTab.FILES.label) },
                            label = { Text(AppTab.FILES.label) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppTab.RECOVER,
                            onClick = { selectedTab = AppTab.RECOVER },
                            icon = { Icon(Icons.Filled.Restore, contentDescription = AppTab.RECOVER.label) },
                            label = { Text(AppTab.RECOVER.label) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppTab.VAULT,
                            onClick = { selectedTab = AppTab.VAULT },
                            icon = { Icon(Icons.Filled.Lock, contentDescription = AppTab.VAULT.label) },
                            label = { Text(AppTab.VAULT.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                if (isSettingsVisible) {
                    SettingsScreen(onBack = { isSettingsVisible = false })
                } else {
                    when (selectedTab) {
                        AppTab.HOME -> HomeScreen(onNavigateToSettings = { isSettingsVisible = true })
                        AppTab.CLEAN -> CleanScreen()
                        AppTab.FILES -> {
                            if (hasAllFilesAccess) {
                                FilesScreen()
                            } else {
                                AllFilesAccessRationale(
                                    onGrantClick = { allFilesLauncher.launch(PermissionUtils.allFilesAccessIntent(context)) }
                                )
                            }
                        }
                        AppTab.RECOVER -> RecoverScreen()
                        AppTab.VAULT -> VaultScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun AllFilesAccessRationale(onGrantClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Browsing files needs full storage access.")
        Spacer(Modifier.height(8.dp))
        Text(
            "You'll be taken to system settings — enable \"Allow access to manage all files\" for SpaceXpert.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGrantClick) { Text("Grant Full Access") }
    }
}

@Composable
fun PermissionRationaleScreen(onRequestAgain: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("SpaceXpert needs media access to analyze your storage.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequestAgain) { Text("Grant Access") }
    }
}