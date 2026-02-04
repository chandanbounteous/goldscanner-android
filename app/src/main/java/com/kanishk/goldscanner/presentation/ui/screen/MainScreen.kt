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
    onNavigateToArticleDetail: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialSelectedTab) }
    
    val tabs = listOf(
        BottomNavItem("Gold Rate", ImageVector.vectorResource(R.drawable.gold_bar)),
        BottomNavItem("Articles", ImageVector.vectorResource(R.drawable.article_list)),
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
                2 -> BasketTabScreen()
                3 -> MoreTabScreen()
            }
        }
    }
}

@Composable
fun ArticlesTabScreen(onNavigateToArticleDetail: () -> Unit = {}) {
    ArticleListScreen(onNavigateToArticleDetail = onNavigateToArticleDetail)
}

@Composable
fun BasketTabScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Basket",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your gold basket items will be displayed here",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
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
            text = "Settings and more options",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector
)