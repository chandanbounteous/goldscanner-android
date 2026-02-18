package com.kanishk.goldscanner.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.kanishk.goldscanner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    initialSelectedTab: Int = 0,
    onNavigateToArticleDetail: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialSelectedTab) }
    
    val tabs = listOf(
        BottomNavItem("Gold Rate", ImageVector.vectorResource(R.drawable.gold_bar)),
        BottomNavItem("Articles", ImageVector.vectorResource(R.drawable.article_list)),
        BottomNavItem("Customers", ImageVector.vectorResource(R.drawable.customer)),
        BottomNavItem("Basket", ImageVector.vectorResource(R.drawable.gold_basket)),
        BottomNavItem("More", Icons.Default.Menu)
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
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
                1 -> ArticlesTabScreen(onNavigateToArticleDetail)
                2 -> CustomerTabScreen(onNavigateToCustomers)
                3 -> BasketScreen(
                    onNavigateToArticleListing = {
                        selectedTab = 1 // Navigate to Articles tab
                    },
                    onNavigateToArticleDetail = onNavigateToArticleDetail,
                    onNavigateAway = {
                        // This will be called when navigating away from billed basket
                        // The actual clearing is handled in the DisposableEffect
                    }
                )
                4 -> MoreTabScreen()
            }
        }
    }
}

@Composable
fun ArticlesTabScreen(onNavigateToArticleDetail: () -> Unit = {}) {
    ArticleListScreen(onNavigateToArticleDetail = onNavigateToArticleDetail)
}

@Composable
fun CustomerTabScreen(
    onNavigateToCustomers: () -> Unit = {}
) {
    CustomerScreen()
}

@Composable
fun MoreTabScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "More",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Additional options coming soon...",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector
)