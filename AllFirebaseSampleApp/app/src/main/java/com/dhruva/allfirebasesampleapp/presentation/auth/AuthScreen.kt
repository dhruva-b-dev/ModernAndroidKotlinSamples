package com.dhruva.allfirebasesampleapp.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dhruva.allfirebasesampleapp.presentation.auth.additionalprovider.AdditionalProvidersScreen
import com.dhruva.allfirebasesampleapp.presentation.auth.nativeprovider.NativeProvidersScreen
import com.dhruva.allfirebasesampleapp.presentation.navigation.Destination

@Preview
@Composable
fun AuthScreen(modifier: Modifier = Modifier, onNavigateToMain: (Destination) -> Unit = {}) {
    val navController = rememberNavController()
    val startDestination = AuthScreenRoutes.ADDITIONAL_PROVIDERS
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(modifier = modifier.background(MaterialTheme.colorScheme.primary)) { contentPadding ->
        SecondaryTabRow(
            selectedTabIndex = selectedDestination,
            modifier = Modifier.padding(contentPadding)
        ) {
            AuthScreenRoutes.entries.forEachIndexed { index, destination ->
                Tab(
                    selected = selectedDestination == index,
                    onClick = {
                        navController.navigate(route = destination.route)
                        selectedDestination = index
                    },
                    text = {
                        Text(
                            text = destination.label,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        AuthNavHost(navController, onNavigateToMain)
    }
}

@Composable
fun AuthNavHost(
    navController: NavHostController,
    onNavigateToMain: (Destination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AuthScreenRoutes.ADDITIONAL_PROVIDERS.route,
        modifier = modifier
    ) {
        composable(AuthScreenRoutes.ADDITIONAL_PROVIDERS.route) {
            AdditionalProvidersScreen(onSignInSuccess = { onNavigateToMain(Destination.TODOS) })
        }
        composable(AuthScreenRoutes.NATIVE_PROVIDERS.route) {
            NativeProvidersScreen()
        }
    }
}

enum class AuthScreenRoutes(val route: String, val label: String) {
    ADDITIONAL_PROVIDERS(route = "additional_providers", label = "Additional Providers"),
    NATIVE_PROVIDERS(route = "native_providers", label = "Native Providers")
}
