package com.gracechurch.gracefulgiving.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gracechurch.gracefulgiving.app.navigation.Routes
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import com.gracechurch.gracefulgiving.domain.usecase.ImportDonationsUseCase
import com.gracechurch.gracefulgiving.ui.batch.BatchEntryScreen
import com.gracechurch.gracefulgiving.ui.batch.BatchManagementScreen
import com.gracechurch.gracefulgiving.ui.batch.BatchSelectionScreen
import com.gracechurch.gracefulgiving.ui.common.MainScaffold
import com.gracechurch.gracefulgiving.ui.dashboard.DashboardScreen
import com.gracechurch.gracefulgiving.ui.donation.DonationScreen
import com.gracechurch.gracefulgiving.ui.donor.DonorDetailScreen
import com.gracechurch.gracefulgiving.ui.donor.DonorListScreen
import com.gracechurch.gracefulgiving.ui.fund.FundManagementScreen
import com.gracechurch.gracefulgiving.ui.import.ImportDonationsScreen
import com.gracechurch.gracefulgiving.ui.login.ForgotPasswordScreen
import com.gracechurch.gracefulgiving.ui.login.LoginScreen
import com.gracechurch.gracefulgiving.ui.profile.ChangePasswordScreen
import com.gracechurch.gracefulgiving.ui.profile.EditProfileScreen
import com.gracechurch.gracefulgiving.ui.statements.YearlyStatementsScreen
import com.gracechurch.gracefulgiving.ui.usermanagement.EditUserScreen
import com.gracechurch.gracefulgiving.ui.usermanagement.UserManagementScreen
import com.gracefulgiving.gracefulgiving.theme.GracefulGivingTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userSessionRepository: UserSessionRepository
    @Inject lateinit var importDonationsUseCase: ImportDonationsUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GracefulGivingTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.LOGIN
                ) {

                    composable(Routes.LOGIN) {
                        LoginScreen(
                            navController = navController,
                            onLoginSuccess = {
                                navController.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.DASHBOARD) {
                        MainScaffold(navController) {
                            DashboardScreen(navController)
                        }
                    }

                    composable(Routes.IMPORT_DONATIONS) {
                        MainScaffold(navController) {
                            ImportDonationsScreen(
                                importDonationsUseCase = importDonationsUseCase,
                                userSessionRepository = userSessionRepository
                            )
                        }
                    }

                    composable(Routes.BATCH_SELECTION) {
                        MainScaffold(navController) {
                            val userId = userSessionRepository.currentUser?.id ?: 0L
                            BatchSelectionScreen(
                                userId = userId,
                                onNavigateToBatchEntry = { batchId ->
                                    navController.navigate("${Routes.BATCH_ENTRY}/$batchId")
                                },
                                onNavigateUp = {
                                    // Normally handled by scaffold, but if needed:
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    composable(
                        route = "${Routes.BATCH_ENTRY}/{batchId}",
                        arguments = listOf(navArgument("batchId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val batchId = backStackEntry.arguments?.getLong("batchId") ?: 0L
                        MainScaffold(navController) {
                            BatchEntryScreen(
                                batchId = batchId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    
                    composable(Routes.DONORS_DONATIONS) {
                        MainScaffold(navController) {
                            DonorListScreen(navController)
                        }
                    }

                    composable(Routes.FUND_MANAGEMENT) {
                        MainScaffold(navController) {
                            FundManagementScreen(
                                onNavigateUp = { navController.popBackStack() }
                            )
                        }
                    }

                    composable(Routes.YEARLY_STATEMENTS) {
                        MainScaffold(navController) {
                            YearlyStatementsScreen(
                                navController = navController
                            )
                        }
                    }
                    
                    composable(Routes.USER_MANAGEMENT) {
                         MainScaffold(navController) {
                            UserManagementScreen(
                                navController = navController
                            )
                         }
                    }
                    
                    composable(Routes.EDIT_PROFILE) {
                        MainScaffold(navController) {
                            EditProfileScreen(
                                onProfileUpdated = { navController.popBackStack() }
                            )
                        }
                    }

                    composable(Routes.CHANGE_PASSWORD) {
                        MainScaffold(navController) {
                            ChangePasswordScreen(
                                onPasswordChanged = { navController.popBackStack() }
                            )
                        }
                    }

                    composable(
                        route = "${Routes.DONOR_DETAIL}/{donorId}",
                        arguments = listOf(navArgument("donorId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val donorId = backStackEntry.arguments?.getLong("donorId") ?: 0L
                        MainScaffold(navController) {
                            DonorDetailScreen(donorId = donorId, navController = navController)
                        }
                    }
                    
                    composable(
                        route = "${Routes.DONATION}/{donationId}",
                        arguments = listOf(navArgument("donationId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val donationId = backStackEntry.arguments?.getLong("donationId") ?: 0L
                        MainScaffold(navController) {
                            DonationScreen(donationId = donationId, navController = navController)
                        }
                    }
                    
                    composable(
                        route = "${Routes.EDIT_USER}/{userId}",
                        arguments = listOf(navArgument("userId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                        MainScaffold(navController) {
                            EditUserScreen(onUserUpdated = { navController.popBackStack() })
                        }
                    }

                    // Re-adding potentially missing routes just in case
                     composable(Routes.FORGOT_PASSWORD) {
                        ForgotPasswordScreen(
                            onPasswordReset = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
