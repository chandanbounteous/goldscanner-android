## CompactGoldRateCard Component

### Overview

A reusable, compact component that displays the current 24K gold rate and effective date. Designed to be used across multiple screens without taking up much space.

### Features

- **Compact Design**: Minimal space usage while displaying essential information
- **Real-time Data**: Fetches current gold rates using GET /api/v1/gold/currentrate
- **State Handling**: Loading, error, and success states with appropriate UI
- **24K Focus**: Shows only the highest purity gold rate for quick reference
- **Nepali Date**: Displays effective date in Bikram Samvat format

### Usage

```kotlin
// Basic usage
CompactGoldRateCard()

// With custom modifier and error handling
CompactGoldRateCard(
    modifier = Modifier.padding(16.dp),
    onError = {
        // Handle authentication errors
        navigateToLogin()
    }
)
```

### Component Structure

- **Left Side**: 24K gold indicator with star icon and current rate
- **Right Side**: Effective date in Nepali format with B.S. indicator
- **Loading State**: Compact progress indicator with loading text
- **Error State**: Error message with retry button

### Integration

The component automatically uses the existing GoldRateViewModel and follows the same data flow as the main Gold Rate screen. It can be placed anywhere in the app where a quick gold rate reference is needed.

### Preview States

The component includes comprehensive previews showing:

- Normal state with data
- Loading state
- Error state with retry option
