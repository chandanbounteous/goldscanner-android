## Article List Screen Implementation

This implementation provides a complete Article List screen with the following features:

### Components Created

1. **GetGoldArticlesUseCase** - Domain layer use case for retrieving articles
2. **ArticleListViewModel** - Manages UI state with pagination and search functionality
3. **ArticleListScreen** - Main screen component with all required features
4. **ArticleListItem** - Individual article display component
5. **ArticleSearchBar** - Search input with scanner button

### Features Implemented

✅ **CompactGoldRateCard** - Displays current gold rates at the top
✅ **Search Functionality** - Text input with 500ms debounce for smooth searching  
✅ **Pagination** - Automatic loading when scrolling near end of list
✅ **Scanner Button** - Clickable scanner icon (navigation to be implemented later)
✅ **Article Display** - Shows articleCode, karat, netWeight, totalCostAfterTax
✅ **Clickable Rows** - Tappable items for navigation (to be implemented later)
✅ **Loading States** - Initial loading, load more, and empty states
✅ **Error Handling** - Error display with retry functionality

### API Integration

- **Initial Load**: `GET /gold/articles?code=&offset=0&limit=25`
- **Load More**: `GET /gold/articles?code=&offset=1&limit=25` (incremental offset)
- **Search**: `GET /gold/articles?code=<search_query>&offset=0&limit=25`
- **Results**: Appended to local list for smooth scrolling experience

### UI Design Features

- Modern Material 3 design
- Responsive loading indicators
- Smooth scroll-triggered pagination
- Professional article cards with pricing display
- Intuitive search experience with clear button
- Error handling with retry mechanism

### Dependencies Updated

- Added `GetGoldArticlesUseCase` to `UseCaseModule`
- Added `ArticleListViewModel` to `AppModule`
- Updated `MainScreen` to use new `ArticleListScreen`

The implementation is ready for use and testing. Scanner navigation and article detail navigation can be implemented when those screens are created.
