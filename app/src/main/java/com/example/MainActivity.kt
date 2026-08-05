package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.AdminAnalyticsScreen
import com.example.ui.screens.AuthProfileScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DownloaderScreen
import com.example.ui.screens.FileManagerScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.theme.MediaManagerTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MediaViewModel

class MainActivity : ComponentActivity() {
    private val mediaViewModel: MediaViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaManagerTheme {
                MainAppStructure(
                    mediaViewModel = mediaViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Dashboard", Icons.Default.Home, "nav_tab_dashboard"),
    DOWNLOADER("Downloader", Icons.Default.Download, "nav_tab_downloader"),
    FILES("Files", Icons.Default.Folder, "nav_tab_files"),
    VAULT("Vault", Icons.Default.Lock, "nav_tab_vault"),
    ADMIN("Admin", Icons.Default.AdminPanelSettings, "nav_tab_admin"),
    PROFILE("Profile", Icons.Default.Person, "nav_tab_profile")
}

@Composable
fun MainAppStructure(
    mediaViewModel: MediaViewModel,
    authViewModel: AuthViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = NavigationTab.entries.toTypedArray()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (selectedTab) {
            0 -> DashboardScreen(
                viewModel = mediaViewModel,
                onNavigateToDownloader = { selectedTab = 1 },
                onNavigateToFileManager = { selectedTab = 2 },
                onNavigateToVault = { selectedTab = 3 },
                modifier = modifier
            )
            1 -> DownloaderScreen(
                viewModel = mediaViewModel,
                modifier = modifier
            )
            2 -> FileManagerScreen(
                viewModel = mediaViewModel,
                modifier = modifier
            )
            3 -> VaultScreen(
                mediaViewModel = mediaViewModel,
                authViewModel = authViewModel,
                modifier = modifier
            )
            4 -> AdminAnalyticsScreen(
                mediaViewModel = mediaViewModel,
                authViewModel = authViewModel,
                modifier = modifier
            )
            5 -> AuthProfileScreen(
                authViewModel = authViewModel,
                modifier = modifier
            )
        }
    }
}
