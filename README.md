# Android + OpenCV + OpenGL Assessment

This project contains an Android app that captures camera frames, processes them with OpenCV (C++), and renders them using OpenGL ES. It also includes a TypeScript web viewer.

## Project Structure

- `android/`: Android Studio project
    - `app/`: Main application module
    - `app/src/main/cpp/`: Native C++ code (OpenCV, JNI)
- `web/`: TypeScript web viewer

## Setup Instructions

### 1. OpenCV Android SDK
You need to download the OpenCV Android SDK to build the native part of this project.

1.  Download the **OpenCV Android SDK** from the [official website](https://opencv.org/releases/).
2.  Extract the SDK to a location on your computer (e.g., `C:\Android\OpenCV-android-sdk`).
3.  Create a file named `local.properties` in the `android/` directory (if it doesn't exist).
4.  Add the following line to `local.properties`, replacing the path with your actual path:

```properties
sdk.dir=C:\\Users\\YourUser\\AppData\\Local\\Android\\Sdk
opencv.dir=C:\\path\\to\\OpenCV-android-sdk
```

**Note:** Use double backslashes `\\` or single forward slashes `/` for paths on Windows.

### 2. Android Build
1.  **Open Android Studio.**
2.  Select **Open** and navigate to the `android/` directory within this project. **Important:** Do not open the root `flamapp` folder; open the `android` subdirectory.
3.  Wait for Gradle to sync. If prompted, install any missing SDK components or NDK versions.
4.  Connect your Android device or start an emulator.
5.  Click the **Run** button (green play icon) to build and install the app.

### 3. Web Viewer
1.  Navigate to the `web/` directory.
2.  Run `npm install`.
3.  Run `npm run dev` to start the local server.

## Features
- [ ] Camera Feed (Android)
- [ ] OpenCV Edge Detection (Native C++)
- [ ] OpenGL Rendering
- [ ] Web Viewer
