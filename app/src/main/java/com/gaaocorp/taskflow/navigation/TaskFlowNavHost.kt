package com.gaaocorp.taskflow.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gaaocorp.taskflow.profile.ui.ProfileScreen
import com.gaaocorp.taskflow.tasks.ui.CreateTaskScreen
import com.gaaocorp.taskflow.tasks.ui.TaskDetailScreen
import com.gaaocorp.taskflow.tasks.ui.TaskListScreen

sealed class Screen(val route: String) {
    object TaskList : Screen("task_list")
    object CreateTask : Screen("create_task")
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    object Profile : Screen("profile")
}

@Composable
fun TaskFlowNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TaskList.route
    ) {
        composable(Screen.TaskList.route) {
            TaskListScreen(
                onNavigateToCreate = { navController.navigate(Screen.CreateTask.route) },
                onNavigateToDetail = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
        composable(Screen.CreateTask.route) {
            CreateTaskScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TaskDetail.route) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
