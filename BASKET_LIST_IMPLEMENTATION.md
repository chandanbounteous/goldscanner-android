# Basket List/Search Screen Implementation

## Overview

Implemented a comprehensive Basket List/Search Screen for the Gold Scanner Android app following CLEAN architecture principles. The implementation handles both the initial requirement to check for an active basket and provides a full-featured search interface for customer baskets.

## Features Implemented

### 1. Navigation Logic

- **Active Basket Check**: When basket tab is clicked, the app checks for an active basket in local storage
- **Conditional Navigation**:
  - If active basket exists → Navigate to Active Basket Screen (placeholder for future implementation)
  - If no active basket → Show Basket List/Search Screen

### 2. Search Functionality

- **API Integration**: Uses `/api/v1/customer/basket/search` endpoint
- **Search Filters**:
  - Customer Name (text field)
  - Phone Number (number field)
  - Start Date (dd-mm-yyyy format)
  - End Date (dd-mm-yyyy format)
  - Include Billed (checkbox)
  - Include Discarded (checkbox)
- **Search Actions**:
  - Search button (triggers filtered search)
  - Clear button (resets filters and reloads initial data)

### 3. List Display

- **Infinite Scroll**: Automatic pagination when user reaches bottom of list
- **Card Layout**: Each basket displayed as a clickable card showing:
  - Nepali Date (formatted as "26 Poush 2082")
  - Customer Name (firstName + lastName combined)
  - Phone Number
  - Item Count (total articles in basket)
  - Status Icons:
    - Billed icon (when isBilled = true)
    - Discarded icon (when isDiscarded = true)
- **Empty State**: Friendly message when no baskets found

### 4. Pagination

- **Initial Load**: Starts with offset=0, limit=10
- **Load More**: Automatically fetches next page when scrolling near bottom
- **hasMore Logic**: Uses API's hasMore field to determine if more data is available

## Architecture Implementation

### Data Layer

1. **Models**:
   - `BasketSearchRequest.kt` - API request model
   - `BasketSearchResponse.kt` - API response models (BasketItem, PaginationInfo, etc.)
   - `Basket.kt` - Domain models (Basket, BasketSearchFilter, ActiveBasket)

2. **Network Service**:
   - Extended `CustomerApiService.kt` with `searchBaskets()` method
   - Handles authentication, error cases, and response parsing

3. **Repository**:
   - `BasketRepository.kt` - Interface defining basket operations
   - `BasketRepositoryImpl.kt` - Implementation with:
     - Search baskets with filters and pagination
     - Active basket management (get/set/clear)
     - Date format conversion (UI dd-mm-yyyy ↔ API yyyy-mm-dd)
     - Nepali date formatting for display

### Domain Layer

4. **Use Cases**:
   - `SearchBasketsUseCase.kt` - Handles basket search business logic
   - `GetActiveBasketUseCase.kt` - Manages active basket retrieval

### Presentation Layer

5. **ViewModel**:
   - `BasketListViewModel.kt` - Manages UI state and business logic:
     - Checks for active basket on initialization
     - Handles search filtering
     - Manages pagination state (loading, hasMore, etc.)
     - Error handling and state management

6. **UI**:
   - `BasketListScreen.kt` - Complete Compose UI with:
     - Collapsible search filters section
     - Infinite scroll list with proper loading states
     - Error handling
     - Empty state display
     - Card-based basket items with status icons

### Dependency Injection

7. **DI Updates**:
   - Added BasketRepository to RepositoryModule
   - Added basket use cases to UseCaseModule
   - Added BasketListViewModel to AppModule

### Utilities

8. **LocalStorage Enhancement**:
   - Added public methods for string storage (`getString`, `putString`, `remove`)
   - Added ACTIVE_BASKET key to StorageKey enum

## Integration Points

### Replaced BasketTabScreen

- Updated `MainScreen.kt` to use new `BasketListScreen` instead of placeholder
- Removed old `BasketTabScreen` implementation
- Added basket click handler (placeholder for future navigation to basket detail)

### Future Integration Ready

- Active basket detection works with local storage
- Basket click handler prepared for navigation to basket detail screen
- Repository pattern supports easy extension for basket operations

## Date Handling

- **UI Format**: dd-mm-yyyy (user-friendly)
- **API Format**: yyyy-mm-dd (Nepali calendar)
- **Display Format**: "26 Poush 2082" (localized month names)
- Automatic conversion between formats in repository layer

## Error Handling

- Network errors with user-friendly messages
- Authentication handling with automatic token refresh
- Validation for date formats and input fields
- Graceful handling of empty states and loading states

## Performance Optimizations

- Lazy loading with pagination
- Efficient state management with StateFlow
- Minimal recomposition with proper state handling
- Background loading indicators for better UX

## Testing Considerations

- Repository layer is easily testable with mocked API service
- Use cases can be tested independently
- ViewModel state changes can be verified
- UI components are composable and testable

This implementation provides a solid foundation for the basket management feature and can be easily extended when the Active Basket Screen and Basket Detail Screen are implemented.
