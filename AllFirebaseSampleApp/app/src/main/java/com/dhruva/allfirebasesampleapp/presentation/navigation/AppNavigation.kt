package com.dhruva.allfirebasesampleapp.presentation.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dhruva.allfirebasesampleapp.R
import com.dhruva.allfirebasesampleapp.presentation.auth.AuthScreen

@Composable
fun TodosScreen(onNavigateToAddTodo: () -> Unit, onNavigateToDetail: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Todos Screen")
            Button(onClick = onNavigateToAddTodo) { Text("Add Todo") }
            Button(onClick = { onNavigateToDetail("123") }) { Text("View Detail (ID: 123)") }
        }
    }
}

@Composable
fun PriorityScreen(onNavigateToDetail: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Priority Screen")
            Button(onClick = { onNavigateToDetail("456") }) { Text("View Detail (ID: 456)") }
        }
    }
}

@Composable
fun DoneScreen(onNavigateToDetail: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Done Screen")
            Button(onClick = { onNavigateToDetail("789") }) { Text("View Detail (ID: 789)") }
        }
    }
}

@Composable
fun StatisticsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Statistics Screen")
    }
}

@Composable
fun AddTodoScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Add Todo Bottom Sheet / Dialog")
    }
}

@Composable
fun TodoDetailScreen(documentId: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Todo Detail Screen for ID: $documentId")
    }
}

object Routes {
    const val AUTH = "auth"
    const val TODO_DETAIL = "todoDetail/{documentId}"
    const val ADD_TODO = "addTodo"
}

enum class Destination(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    @StringRes val contentDescription: Int
) {
    TODOS("todos", R.string.title_todos, R.drawable.ic_todos, R.string.title_todos),
    PRIORITY("priority", R.string.title_priority, R.drawable.ic_priority, R.string.title_priority),
    DONE("done", R.string.title_done, R.drawable.ic_done, R.string.title_done),
    STATISTICS("statistics", R.string.title_statistics, R.drawable.ic_statistics, R.string.title_statistics)
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH,
        modifier = modifier
    ) {
        composable(Routes.AUTH) {
            AuthScreen(onNavigateToMain = { destination ->
                navController.navigate(destination.route) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
            })
        }
        composable(Destination.TODOS.route) {
            TodosScreen(
                onNavigateToAddTodo = { navController.navigate(Routes.ADD_TODO) },
                onNavigateToDetail = { id -> navController.navigate("todoDetail/$id") }
            )
        }
        composable(Destination.PRIORITY.route) {
            PriorityScreen(onNavigateToDetail = { id -> navController.navigate("todoDetail/$id") })
        }
        composable(Destination.DONE.route) {
            DoneScreen(onNavigateToDetail = { id -> navController.navigate("todoDetail/$id") })
        }
        composable(Destination.STATISTICS.route) {
            StatisticsScreen()
        }
        dialog(Routes.ADD_TODO) {
            AddTodoScreen()
        }
        dialog(
            route = Routes.TODO_DETAIL,
            arguments = listOf(navArgument("documentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
            TodoDetailScreen(documentId = documentId)
        }
    }
}

@Preview
@Composable
fun BottomNavbar(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = Destination.entries
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    bottomNavItems.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = destination.icon),
                                    contentDescription = stringResource(id = destination.contentDescription)
                                )
                            },
                            label = { Text(text = stringResource(id = destination.label)) }
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        AppNavHost(navController = navController, modifier = Modifier.padding(contentPadding))
    }
}
