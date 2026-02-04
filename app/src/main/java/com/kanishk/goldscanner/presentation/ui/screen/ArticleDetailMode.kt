package com.kanishk.goldscanner.presentation.ui.screen

enum class ArticleDetailMode {
    CREATE_NEW,           // Creating a new article from scratch
    UPDATE_INDEPENDENT,   // Updating an existing article not part of any basket
    UPDATE_BASKET_ITEM    // Updating an article that is part of a basket
}