# Smart Grocery Organizer 🛒

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A powerful Android application designed to help you manage your grocery inventory efficiently, track expiration dates, receive timely notifications, and discover recipes based on your available ingredients.

## 📋 Table of Contents

- [Features](#-features)
- [Screenshots](#-screenshots)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#-usage)
- [Architecture](#-architecture)
- [Dependencies](#-dependencies)
- [API Integration](#-api-integration)
- [Contributing](#-contributing)
- [License](#-license)
- [Changelog](#-changelog)
- [Contact](#-contact)
- [Acknowledgments](#-acknowledgments)

## ✨ Features

### Core Functionality
- **📦 Inventory Management**: Add, edit, and delete grocery items with detailed information
- **🗂️ Category Organization**: Organize items by categories (Fruits, Vegetables, Dairy, Meat, etc.)
- **📅 Expiry Tracking**: Automatic calculation of days until expiration
- **⚠️ Smart Alerts**: Customizable expiry warning system (1-7 days configurable)
- **⭐ Urgent Items**: Mark items as urgent for priority attention
- **🔍 Advanced Sorting**: Multiple sorting options (by expiry, name, category, urgency)

### Smart Features
- **🔔 Push Notifications**: Daily reminders for expiring items at 12 PM
- **🤖 Auto-Delete**: Optional automatic removal of expired items
- **🍳 Recipe Suggestions**: AI-powered recipe recommendations based on available ingredients
- **📊 Analytics Dashboard**: Visual insights with charts and category breakdowns
- **🎨 Dark Mode**: Full theme support for comfortable viewing

### User Experience
- **🎯 Intuitive Interface**: Material Design 3 with smooth animations
- **📱 Responsive Design**: Optimized for various screen sizes
- **🔄 Pull to Refresh**: Quick data synchronization
- **💾 Offline Support**: Works seamlessly without internet connection
- **🌐 Multi-language Ready**: Internationalization support

## 📱 Screenshots

> **Note**: Add screenshots to a `/screenshots` folder in your repository and update the paths below.

| Home Screen | Add Item | Recipe Suggestions |
|------------|----------|-------------------|
| ![Home](screenshots/home.png) | ![Add](screenshots/add_item.png) | ![Recipes](screenshots/recipes.png) |

| Analytics | Categories | Settings |
|-----------|------------|----------|
| ![Analytics](screenshots/analytics.png) | ![Categories](screenshots/categories.png) | ![Settings](screenshots/settings.png) |

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Arctic Fox (2020.3.1) or newer (recommended: Hedgehog or later)
- **JDK**: Java Development Kit 17 or higher
- **Android SDK**: API Level 24 (Android 7.0) minimum, API Level 34 target
- **Gradle**: 8.7.3 (included via wrapper)
- **Kotlin**: 2.0.21 (configured in build files)

### Installation

#### Method 1: Clone the Repository

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/smart-grocery-organizer.git
   cd smart-grocery-organizer
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory and select it

3. **Configure API Keys**
   
   The app uses Spoonacular API for recipe suggestions. To enable this feature:
   
   a. Get your free API key from [Spoonacular](https://spoonacular.com/food-api)
   
   b. Open `app/src/main/java/com/example/smartgroceryorganizer/api/RecipeRepository.kt`
   
   c. Replace the API key:
   ```kotlin
   companion object {
       const val API_KEY = "YOUR_API_KEY_HERE"
   }
   ```

4. **Set up Firebase (Optional)**
   
   For crashlytics support:
   
   a. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   
   b. Add your Android app to the Firebase project
   
   c. Download `google-services.json` and place it in the `app/` directory
   
   d. If you don't want Firebase, remove these lines from `app/build.gradle`:
   ```groovy
   id 'com.google.gms.google-services'
   id 'com.google.firebase.crashlytics'
   ```

5. **Sync Gradle**
   - Click "Sync Now" in the banner at the top of Android Studio
   - Wait for Gradle sync to complete

6. **Build and Run**
   - Connect your Android device via USB or start an emulator
   - Click the "Run" button (▶️) in Android Studio
   - Select your device and click "OK"

#### Method 2: Download APK

1. Go to the [Releases](https://github.com/yourusername/smart-grocery-organizer/releases) page
2. Download the latest APK file
3. Transfer to your Android device
4. Enable "Install from Unknown Sources" in your device settings
5. Install the APK

## 📖 Usage

### Adding Items

1. **Open the app** and tap the **floating action button** (➕) on the home screen
2. Fill in the item details:
   - **Name**: Enter the item name (e.g., "Milk", "Eggs")
   - **Category**: Select from predefined categories
   - **Quantity**: Specify quantity (e.g., "2 liters", "1 dozen")
   - **Expiry Date**: Pick a date from the calendar
3. Tap **Save** to add the item

### Managing Items

- **View Details**: Tap on any item to see full details
- **Edit Item**: In detail view, tap the edit icon
- **Delete Item**: In detail view, tap the delete icon and confirm
- **Mark as Urgent**: Tap the star icon on any item
- **Sort Items**: Tap the sort button and choose your preferred sorting method

### Setting Up Notifications

1. Navigate to **Settings** (⚙️ icon in bottom navigation)
2. Toggle **Enable Notifications**
3. Adjust **Expiry Warning Days** (1-7 days) using the slider
4. Configure **Notification Time** (default: 12:00 PM)
5. Enable **Auto-Delete Expired Items** if desired

### Discovering Recipes

1. Tap **Recipes** in the bottom navigation
2. Click **Search Recipes** button
3. The app will analyze your inventory and suggest recipes
4. Tap any recipe to view:
   - Full ingredient list
   - Step-by-step instructions
   - Cooking time and servings
5. Use the **Use Recipe** button to automatically deduct used ingredients

### Viewing Analytics

1. Tap **Analytics** in the bottom navigation
2. View comprehensive statistics:
   - Total items count
   - Items expiring soon
   - Expired items count
3. Explore the **category breakdown** chart
4. See percentage distribution across categories

### Customizing Settings

Navigate to **Settings** to configure:
- **Theme**: Switch between Light and Dark mode
- **Notifications**: Enable/disable and set timing
- **Auto-Delete**: Automatically remove expired items
- **Expiry Warning**: Set warning threshold (1-7 days)
- **Account**: Update username and email

## 🏗️ Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture pattern with **Repository** pattern for clean separation of concerns.

```
app/
├── data/
│   ├── models/         # Data models (GroceryItem)
│   ├── dao/            # Room Database Access Objects
│   ├── database/       # Room Database configuration
│   └── repository/     # Data repositories
├── ui/
│   ├── activities/     # Activity classes
│   ├── adapters/       # RecyclerView adapters
│   └── viewmodels/     # ViewModel classes
├── api/
│   ├── services/       # Retrofit API interfaces
│   ├── models/         # API response models
│   └── repositories/   # API data repositories
├── utils/
│   ├── notifications/  # Notification helpers
│   ├── workers/        # WorkManager workers
│   └── receivers/      # Broadcast receivers
└── res/
    ├── layout/         # XML layouts
    ├── values/         # Strings, colors, themes
    ├── drawable/       # Images and icons
    └── anim/           # Animation resources
```

### Key Components

- **Room Database**: Local SQLite database for persistent storage
- **LiveData**: Observable data holder for lifecycle-aware updates
- **ViewModel**: Manages UI-related data in lifecycle-conscious way
- **WorkManager**: Schedules background tasks for notifications
- **Retrofit**: REST API client for recipe suggestions
- **Material Design 3**: Modern UI components and theming

## 📦 Dependencies

### Core Android Libraries
```groovy
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
androidx.constraintlayout:constraintlayout:2.1.4
```

### Lifecycle Components
```groovy
androidx.lifecycle:lifecycle-livedata-ktx:2.7.0
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
```

### Room Database
```groovy
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1
androidx.room:room-compiler:2.6.1 (KSP)
```

### Navigation
```groovy
androidx.navigation:navigation-fragment-ktx:2.7.6
androidx.navigation:navigation-ui-ktx:2.7.6
```

### Background Processing
```groovy
androidx.work:work-runtime-ktx:2.9.0
```

### Networking
```groovy
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0
com.squareup.okhttp3:logging-interceptor:4.11.0
com.google.code.gson:gson:2.10.1
```

### Image Loading
```groovy
com.github.bumptech.glide:glide:4.16.0
```

### Charts & Visualization
```groovy
com.github.PhilJay:MPAndroidChart:v3.1.0
```

### Firebase (Optional)
```groovy
com.google.firebase:firebase-bom:32.7.0
com.google.firebase:firebase-crashlytics
```

## 🌐 API Integration

### Spoonacular API

This app integrates with the [Spoonacular Food API](https://spoonacular.com/food-api) to provide recipe suggestions.

**Features Used:**
- **Find Recipes by Ingredients**: Searches for recipes based on available grocery items
- **Get Recipe Information**: Fetches detailed recipe instructions and nutritional data

**API Endpoints:**
- `GET /recipes/findByIngredients`: Find recipes matching available ingredients
- `GET /recipes/{id}/information`: Get complete recipe details

**Rate Limits:**
- Free tier: 150 requests/day
- Upgrade for higher limits

**Configuration:**
Update the API key in `RecipeRepository.kt`:
```kotlin
const val API_KEY = "your_spoonacular_api_key"
```

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### Reporting Bugs

1. Check if the bug has already been reported in [Issues](https://github.com/yourusername/smart-grocery-organizer/issues)
2. If not, create a new issue with:
   - Clear, descriptive title
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
   - Device and Android version

### Suggesting Features

1. Open a new issue with the `enhancement` label
2. Describe the feature and its benefits
3. Provide examples or mockups if possible

### Pull Requests

1. **Fork** the repository
2. **Create a branch** for your feature
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit** your changes
   ```bash
   git commit -m "Add some AmazingFeature"
   ```
4. **Push** to the branch
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request** with:
   - Clear description of changes
   - Reference to related issues
   - Screenshots for UI changes

### Code Style Guidelines

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Write clean, maintainable code
- Test your changes thoroughly

### Development Setup

1. Fork and clone the repository
2. Create a feature branch
3. Make your changes
4. Run tests (if available)
5. Submit a pull request

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 Mustahsan Atif

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 📝 Changelog

### Version 1.0.0 (November 2025)

#### Added
- ✅ Complete inventory management system
- ✅ Expiry date tracking with customizable warnings
- ✅ Push notifications for expiring items
- ✅ Recipe suggestions via Spoonacular API
- ✅ Analytics dashboard with visual charts
- ✅ Category-based organization
- ✅ Dark mode support
- ✅ Multiple sorting options
- ✅ Auto-delete expired items feature
- ✅ Recipe ingredient deduction
- ✅ Urgent item marking system

#### Technical Improvements
- ✅ MVVM architecture implementation
- ✅ Room database integration
- ✅ WorkManager for background tasks
- ✅ Retrofit API integration
- ✅ Material Design 3 components
- ✅ Comprehensive error handling

## 📞 Contact

**Developer**: Mustahsan Atif

- **Email**: support@smartgrocery.com
- **GitHub**: [@yourusername](https://github.com/yourusername)
- **LinkedIn**: [Your LinkedIn](https://linkedin.com/in/yourprofile)
- **Twitter**: [@yourhandle](https://twitter.com/yourhandle)

### Support

For support, please:
1. Check the [FAQ section](https://github.com/yourusername/smart-grocery-organizer/wiki/FAQ)
2. Search existing [Issues](https://github.com/yourusername/smart-grocery-organizer/issues)
3. Create a new issue with detailed information
4. Email support@smartgrocery.com for urgent matters

## 🙏 Acknowledgments

- **[Spoonacular API](https://spoonacular.com/)** - Recipe data and suggestions
- **[MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)** - Beautiful chart library
- **[Glide](https://github.com/bumptech/glide)** - Efficient image loading
- **[Material Design](https://material.io/)** - Design guidelines and components
- **Android Jetpack** - Modern Android development tools

### Special Thanks

- All contributors who have helped improve this project
- The Android development community for invaluable resources
- Beta testers for their feedback and bug reports

---

## 🌟 Star Us!

If you find this project helpful, please consider giving it a ⭐ on GitHub. It helps others discover the project!

## 📊 Project Stats

![GitHub stars](https://img.shields.io/github/stars/yourusername/smart-grocery-organizer?style=social)
![GitHub forks](https://img.shields.io/github/forks/yourusername/smart-grocery-organizer?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/yourusername/smart-grocery-organizer?style=social)

---

**Made with ❤️ by Mustahsan Atif**

*Last Updated: November 4, 2025*

