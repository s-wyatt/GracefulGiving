package com.gracechurch.gracefulgiving.ui.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gracechurch.gracefulgiving.app.navigation.Routes

@Composable
fun AppDrawerContent(navController: NavController, closeDrawer: () -> Unit) {
    Text(
        "Menu",
        modifier = Modifier.padding(16.dp),
        style = MaterialTheme.typography.headlineSmall.copy(
            fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.7f
        )
    )
    Spacer(modifier = Modifier.height(16.dp))

    val itemTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = MaterialTheme.typography.labelLarge.fontSize * 1.7f
    )

    NavigationDrawerItem(
        label = { Text("Dashboard", style = itemTextStyle) },
        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
        selected = false,
        onClick = {
            navController.navigate(Routes.DASHBOARD)
            closeDrawer()
        }
    )

    NavigationDrawerItem(
        label = { Text("Batches", style = itemTextStyle) },
        icon = { Icon(Icons.Default.List, contentDescription = "Batches") },
        selected = false,
        onClick = {
            navController.navigate(Routes.BATCH_SELECTION)
            closeDrawer()
        }
    )

    NavigationDrawerItem(
        label = { Text("Donors", style = itemTextStyle) },
        icon = { Icon(Icons.Default.Person, contentDescription = "Donors") },
        selected = false,
        onClick = {
            navController.navigate(Routes.DONORS_DONATIONS)
            closeDrawer()
        }
    )

    NavigationDrawerItem(
        label = { Text("Funds", style = itemTextStyle) },
        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Funds") },
        selected = false,
        onClick = {
            navController.navigate(Routes.FUND_MANAGEMENT)
            closeDrawer()
        }
    )

    NavigationDrawerItem(
        label = { Text("Giving", style = itemTextStyle) },
        icon = { Icon(Icons.Default.CreditCard, contentDescription = "Giving") },
        selected = false,
        onClick = {
            navController.navigate(Routes.YEARLY_STATEMENTS)
            closeDrawer()
        }
    )
}