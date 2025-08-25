# AndrApp Application

This project is a demonstration of integrating modern Android
development practices into a simulated legacy Java-based application. It
showcases a modular architecture, the use of contemporary libraries like
Jetpack Compose and Coroutines, and interaction with device hardware
like Bluetooth LE.

## 🚀 Setup Instructions

Follow these steps to build and run the project on your local machine.

### 1. Clone the Repository

Clone this repository to your local machine using your preferred Git
client.

```bash
git clone https://github.com/nsilyov/AndrApp.git
```

### 2. Configure Google Maps API Key

The project requires a Google Maps API key to display the map.

1.  **Get an API Key**: Follow the official Google Cloud [documentation](https://developers.google.com/maps/documentation/android-sdk/get-api-key) to create a project and get an API key for the **\"Maps SDK for Android\"**.

2.  **Create local.properties**: In the root directory of the project the same level as build.gradle.kts), create a file named ocal.properties.

3.  **Add Your Key**: Add your API key to the local.properties file in he following format:  MAPS_API_KEY=YOUR_API_KEY_HERE  
     **Note**: This file is included in .gitignore and should **not** be committed to version control.

### 3. Build and Run

1.  Open the project in the latest version of Android Studio.

2.  Allow Gradle to sync and download all the required dependencies.

3.  Connect an Android device or start an emulator.

4.  Click the \"Run\" button in Android Studio.

## 🧪 Testing the Bluetooth Module

Since this application scans for Bluetooth Low Energy (BLE) devices, you
need a way to simulate a BLE peripheral for testing, if you don't have a
BLE device. The recommended method is to use a second Android phone with
the **nRF Connect for Mobile** app.

1.  **Install nRF Connect**: On a second Android phone, install [nRF Connect for Mobile](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp) from the Google Play Store.

2.  **Configure a GATT Server**:

    - Open nRF Connect, tap the hamburger menu (☰), and select
       **Configure GATT server**.

    - You can add a custom service and characteristic. For example, you
       can create a \"Battery Service\" with a \"Battery Level\"
       characteristic.

3.  **Start Advertising**:

    - Switch to the **Advertiser** tab.

    - Create a new advertising packet, give it a **Local Name** (e.g.,
      > \"My Test Device\"), and include your newly created service.

    - Start advertising.

4.  **Scan with this App**: The device you configured in nRF Connect
     will now appear in the scan list of this application, allowing you
     to test the connection and service discovery features.

## 💡 Architectural Design

The application is intentionally designed as a hybrid of legacy and
modern code to simulate a real-world refactoring scenario.

- **Core Principle**: A central, unified **Dependency Container**
   (DependencyContainer.kt) manages all major dependencies for the
   application, providing repositories and ViewModel factories to the
   presentation layer.

- **Clean Architecture**: The modern modules (Maps & Bluetooth) follow a
   clean, multi-layered architecture to ensure separation of concerns,
   testability, and maintainability.

  - **Presentation Layer**: Responsible for the UI. It consists of
     Activities, Composables (like MapScreen), and ViewModels. The UI
     observes state from the ViewModel and sends user events to it.

  - **Domain Layer**: Contains the core business logic and rules of the
     application. It is represented by UseCases (e.g., GetPinsUseCase)
     and pure data models (Pin.kt). This layer is completely
     independent of Android frameworks.

  - **Data Layer**: Manages all data operations. It is represented by
     Repositories (MapRepositoryImpl, BleManager) which abstract the
     data sources (Room database, BLE hardware).

- **MVVM (Model-View-ViewModel)**: The presentation layer is built using
   the MVVM pattern, where the ViewModel holds and manages UI-related
   state, surviving configuration changes.

- **State Management**:

  - **Kotlin Flows (StateFlow)**: Used to expose streams of data and
     state from the ViewModel to the UI in a reactive way.

  - **Resource Wrapper (Resource.kt)**: A sealed class is used to wrap
     data streams, explicitly representing Loading, Success, and Error
     states in the UI.

## 🛠️ Technology Stack {#technology-stack}

- **Languages**: **Kotlin** (for all modern modules) and **Java** (for
   the legacy core module).

- **UI**:

  - **Jetpack Compose**: The Google Maps module is built entirely with
   Compose for a modern, declarative UI.

  - **XML Layouts**: Used for the legacy core, WebView, and Bluetooth
     modules.

- **Asynchronous Programming**: **Kotlin Coroutines** are used
   throughout the app for managing background threads and asynchronous
   tasks.

- **Dependency Injection**: A simple, manual DI container
   (DependencyContainer.kt) is used to provide dependencies.

- **Jetpack Libraries**:

  - **ViewModel**: For managing UI-related state.

  - **Room**: For local database persistence of map pins.

  - **Jetpack Compose Libraries**: compose-ui, viewmodel-compose.

- **Networking & Hardware**:

  - **Google Maps SDK for Android**: Integrated with the maps-compose
     library.

  - **Android Bluetooth LE API**: For scanning, connecting, and
     discovering services of BLE devices.

## 📦 Module Breakdown {#module-breakdown}

1.  **Core Application (Java)**: Simulates a legacy application entry
     point (MainActivity.java). It includes basic business logic -
     state management using SharedPreferences
     (UserSessionManager.java). **On the first app start, a default
     user is created and saved to demonstrate session persistence. The
     app does not have a login/logout system; this user remains the
     same across sessions until the app\'s data is cleared.**

2.  **Google Maps Module (Kotlin & Compose)**: A modern, feature-rich
     map screen. Users can long-press to save pins (name, description,
     location) to a local Room database. The UI is fully reactive and
     handles loading, success, and error states.

3.  **Local WebView Module (Kotlin & XML)**: Demonstrates loading local
     HTML, CSS, and JavaScript content from the app\'s assets folder
     into a WebView. It includes a custom WebChromeClient to display
     native dialogs for JavaScript alerts.

4.  **Bluetooth Module (Kotlin & XML)**: A screen for scanning and
     interacting with Bluetooth Low Energy (BLE) devices. It handles
     runtime permissions, scanning with a timeout, displaying results,
     and connecting to a device to discover its services and
     characteristics.

## 🚧 Known Limitations & Future Improvements

- **Basic UI/UX**: The user interface is functional but minimalist. It
   lacks a polished visual design and advanced user experience
   considerations.

- **Single BLE Connection**: The Bluetooth module is designed to manage
   only one connection at a time. It could be refactored to handle
   multiple simultaneous connections.

- **No Characteristic Interaction**: The app discovers BLE services and
   characteristics but does not implement reading or writing to them.
   This would be the next logical step.

- **UI Consistency**: The UI is a mix of XML and Jetpack Compose. A
   future improvement would be to migrate all screens to Compose for a
   consistent design and development experience.

- **Error Handling**: While the Maps module uses a Resource wrapper,
   error handling could be made more robust and user-friendly across
   the entire application.
