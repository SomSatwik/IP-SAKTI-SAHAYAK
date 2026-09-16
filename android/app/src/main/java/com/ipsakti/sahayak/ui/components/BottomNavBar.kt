package com.ipsakti.sahayak.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipsakti.sahayak.data.manager.LanguageManager
import com.ipsakti.sahayak.ui.navigation.Screen
import com.ipsakti.sahayak.ui.navigation.bottomNavItems
import com.ipsakti.sahayak.ui.theme.*

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()

    NavigationBar(
        containerColor = Navy900,
        contentColor = Slate400,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val localizedTitle = when (screen) {
                Screen.Home -> LanguageManager.getString("nav_home")
                Screen.Investigate -> LanguageManager.getString("nav_investigate")
                Screen.EvidenceList -> LanguageManager.getString("nav_evidence")
                Screen.Saved -> LanguageManager.getString("nav_saved")
                Screen.Settings -> LanguageManager.getString("nav_settings")
                else -> screen.title
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen.route) },
                icon = {
                    screen.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = localizedTitle
                        )
                    }
                },
                label = {
                    Text(
                        text = localizedTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = RoyalBlue800,
                    unselectedIconColor = Slate400,
                    unselectedTextColor = Slate400
                )
            )
        }
    }
}
