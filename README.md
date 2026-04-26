# SquadPlay 🎮

[![LinkedIn](https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555)](https://linkedin.com/in/evgenysamarin)

**SquadPlay** is an Android application designed to help you and your friends organize game sessions easily. No more endless chat scrolls to figure out who's playing and when.

## 🚀 Features

- **Session Organization:** Create and manage game events.
- **Friend Management:** Keep track of your squad.
- **Calendar Integration:** See upcoming games at a glance.
- **Real-time Updates:** Stay notified with Firebase Cloud Messaging.
- **Seamless Auth:** Easy login with Google Sign-In and Firebase Auth.

## 🛠 Built With

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture:** Clean Architecture with Multi-module setup
- **Dependency Injection:** [Koin](https://insert-koin.io/)
- **Backend:** [Firebase](https://firebase.google.com/) (Firestore, Auth, Messaging, Crashlytics)
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
- **Design:** Material 3

## 📁 Project Structure

The project is divided into several modules to ensure separation of concerns and maintainability:

- **`:app`**: The main Android module containing UI (Compose), ViewModels, and Android-specific logic.
- **`:domain`**: Business logic and Use Cases. Pure Kotlin module.
- **`:data`**: Implementation of repositories, handling data from Firebase and other sources.
- **`:contract`**: Interfaces and abstractions for communication between modules.
- **`:models`**: Shared data models used throughout the application.

## 🚦 Getting Started

### Prerequisites

- Android Studio Jellyfish or newer.
- JDK 17.
- A Firebase project (you'll need to add your own `google-services.json`).

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/EvgenySamarin/SquadPlay.git
   ```
2. Open the project in Android Studio.
3. Connect your Firebase project and add `google-services.json` to the `app/` directory.
4. Build and Run!

## 🗺 Roadmap

See the [open issues](https://github.com/EvgenySamarin/SquadPlay/issues) for a full list of proposed features and known issues.

## 📬 Contact

Evgeny Samarin - [ey.samarin@gmail.com](mailto:ey.samarin@gmail.com)
Twitter: [@EvgenySamarin](https://twitter.com/EvgenySamarin)

Project Link: [https://github.com/EvgenySamarin/SquadPlay](https://github.com/EvgenySamarin/SquadPlay)
