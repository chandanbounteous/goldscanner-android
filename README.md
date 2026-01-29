# Gold Scanner Android App

An Android application for scanning and valuing gold items built with **Clean Architecture** principles.

## 🏗️ Architecture

The app follows **Clean Architecture** with clear separation of concerns:

### 📂 Package Structure

```
com.kanishk.goldscanner/
├── data/                          # Data Layer
│   ├── model/
│   │   ├── request/              # API request models
│   │   └── response/             # API response models
│   ├── network/                  # Network configuration
│   │   └── service/             # API services
│   └── repository/              # Repository implementations
├── domain/                       # Domain Layer
│   ├── repository/              # Repository interfaces
│   └── usecase/                 # Business logic use cases
│       └── auth/               # Authentication use cases
├── presentation/                 # Presentation Layer
│   ├── ui/
│   │   └── screen/             # Compose UI screens
│   ├── viewmodel/              # ViewModels
│   └── navigation/             # Navigation configuration
├── di/                          # Dependency Injection modules
└── utils/                       # Utility classes
```

## 🛠️ Tech Stack

- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Koin 4.0.0
- **Navigation**: Navigation Compose
- **Networking**: Ktor Client 3.0.1
- **Serialization**: Kotlinx Serialization
- **Authentication**: JWT with refresh token
- **Local Storage**: SharedPreferences
- **Language**: Kotlin

## ✨ Features

### 🔐 Authentication

- JWT-based authentication with automatic token refresh
- Secure local token storage
- Login state persistence
- Automatic logout on token expiration

### 🗂️ Local Storage

- Secure credential storage using SharedPreferences
- Rate per tola caching
- User session management
- App configuration persistence

### 🌐 API Integration

- RESTful API communication using Ktor
- Automatic request/response logging
- Error handling with proper user feedback
- Bearer token authentication

### 🎨 UI/UX

- Modern Material3 design
- Responsive layout for different screen sizes
- Loading states and error handling
- Bottom navigation with 4 tabs:
  - 🏠 **Home**: Dashboard and quick actions
  - 📷 **Scanner**: Gold item scanning functionality
  - 📋 **History**: Scan history and records
  - 👤 **Profile**: User profile and settings

## 🚦 App Flow

1. **Splash Screen**:
   - Displays app logo and checks authentication status
   - Routes to Login or Main based on session

2. **Authentication**:
   - Login with email/password
   - JWT token storage and management
   - Automatic navigation to main app

3. **Main Application**:
   - Bottom tab navigation
   - Protected routes requiring authentication
   - Real-time gold rate updates

## 🔧 Setup & Installation

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 30+
- Kotlin 1.9+

### Installation

1. Clone the repository
2. Open project in Android Studio
3. Sync project with Gradle files
4. Update API base URL in `NetworkConfig.kt`
5. Run the app

### Configuration

Update the base URL in `NetworkConfig.kt`:

```kotlin
companion object {
    private const val BASE_URL = "http://localhost:3000/api/v1"
}
```

## 📱 Screens

### 🚀 Splash Screen

- App branding and initialization
- Authentication state check
- Automatic navigation

### 🔑 Login Screen

- Email/password input with validation
- Password visibility toggle
- Loading states and error handling
- "Remember me" functionality

### 🏠 Main Screen

- Bottom navigation with 4 tabs
- Real-time data updates
- User-friendly interface

## 🔄 State Management

The app uses a combination of:

- **ViewModel**: UI-related data holder
- **StateFlow**: Reactive state management
- **Compose State**: Local UI state

## 🧪 Testing Strategy

- **Unit Tests**: Business logic and use cases
- **Integration Tests**: Repository and API layers
- **UI Tests**: Compose UI components

## 🚀 Build & Deploy

### Debug Build

```bash
./gradlew assembleDebug
```

### Release Build

```bash
./gradlew assembleRelease
```

## 🔐 Security Features

- JWT token storage in encrypted SharedPreferences
- Automatic token refresh
- Secure API communication
- Session timeout handling

## 🎯 Future Enhancements

- [ ] Camera integration for gold scanning
- [ ] Offline data caching
- [ ] Push notifications
- [ ] Dark/Light theme support
- [ ] Multi-language support
- [ ] Advanced gold rate analytics

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ using Clean Architecture principles**
