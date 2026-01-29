package com.kanishk.goldscanner.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.kanishk.goldscanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf(
        BottomNavItem("Gold Rate", ImageVector.vectorResource(R.drawable.gold_bar)),
        BottomNavItem("Article List", ImageVector.vectorResource(R.drawable.article_list)),
        BottomNavItem("Article Detail", ImageVector.vectorResource(R.drawable.article_detail)),
        BottomNavItem("More", Icons.Default.Menu)
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> GoldRateScreen()
                1 -> ArticlesTabScreen()
                2 -> ScannerTabScreen()
                3 -> MoreTabScreen()
            }
        }
    }
}

@Composable
fun ArticlesTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Articles",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Gold news and articles will be displayed here")
    }
}

@Composable
fun ScannerTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Scanner",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Scanner functionality will be implemented here")
    }
}

@Composable
fun MoreTabScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "More",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Settings and more options")
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector
)