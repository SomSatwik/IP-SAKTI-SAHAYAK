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

import com.ipsakti.sahayak.data.manager.PersonaManager
import com.ipsakti.sahayak.data.manager.PersonaType

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val currentLang by LanguageManager.currentLanguage.collectAsState()
    val currentPersona by PersonaManager.currentPersona.collectAsState()

    NavigationBar(
        containerColor = Navy900,
        contentColor = Slate400,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val localizedTitle = when (screen) {
                Screen.Home -> LanguageManager.getString("nav_home")
                Screen.Investigate -> when (currentPersona) {
                    PersonaType.PRACTITIONER -> "Formulations"
                    PersonaType.RESEARCHER -> "Novelty R&D"
                    PersonaType.AYUSH_STARTUP -> "Fast-Track"
                    PersonaType.MSME -> "Licensing"
                    PersonaType.CULTIVATOR -> "Biodiversity"
                }
                Screen.EvidenceList -> when (currentPersona) {
                    PersonaType.PRACTITIONER -> "API Standards"
                    PersonaType.RESEARCHER -> "Citations"
                    PersonaType.AYUSH_STARTUP -> "Clearances"
                    PersonaType.MSME -> "GMP & Rules"
                    PersonaType.CULTIVATOR -> "ABS Rules"
                }
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
