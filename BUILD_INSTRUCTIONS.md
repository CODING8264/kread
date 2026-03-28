# Build Instructions for Kread Terminal

## Quick Start

### Option 1: Using Android Studio (Recommended)

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install Android SDK (API 24 or higher)

2. **Open Project**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `kread` folder and open it

3. **Configure SDK Path**
   - Update `local.properties` with your Android SDK path:
     ```properties
     sdk.dir=/path/to/your/android/sdk
     ```
   - On Windows: `C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`
   - On Mac: `/Users/YourName/Library/Android/sdk`
   - On Linux: `/home/YourName/Android/Sdk`

4. **Sync Gradle**
   - Click "Sync Project with Gradle Files" button (elephant icon)
   - Wait for dependencies to download

5. **Build APK**
   - Go to `Build > Build Bundle(s) / APK(s) > Build APK(s)`
   - Wait for build to complete
   - APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

6. **Install on Device**
   - Connect Android device via USB
   - Enable USB debugging on device
   - Click "Run" button (green play icon) in Android Studio
   - Or use: `Build > Build Bundle(s) / APK(s) > Build APK(s)` then manually install

### Option 2: Using Command Line

1. **Prerequisites**
   - Install JDK 17
   - Install Android SDK
   - Set ANDROID_HOME environment variable

2. **Set Environment Variables**
   ```bash
   export ANDROID_HOME=/path/to/android/sdk
   export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
   ```

3. **Update local.properties**
   ```bash
   echo "sdk.dir=$ANDROID_HOME" > local.properties
   ```

4. **Build Debug APK**
   ```bash
   # On Linux/Mac
   ./gradlew assembleDebug
   
   # On Windows
   gradlew.bat assembleDebug
   ```

5. **Install on Device**
   ```bash
   # On Linux/Mac
   ./gradlew installDebug
   
   # On Windows
   gradlew.bat installDebug
   ```

6. **Build Release APK (Signed)**
   ```bash
   # Generate keystore first
   keytool -genkey -v -keystore kread-release.keystore -alias kread -keyalg RSA -keysize 2048 -validity 10000
   
   # Build release
   ./gradlew assembleRelease
   ```

## Gradle Wrapper Setup

If you don't have Gradle wrapper files, create them:

```bash
gradle wrapper --gradle-version 8.1
```

## Troubleshooting

### Build Fails - SDK Not Found
```
Solution: Update local.properties with correct SDK path
```

### Build Fails - Gradle Version
```
Solution: Update gradle/wrapper/gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.1-bin.zip
```

### Build Fails - Kotlin Version
```
Solution: Check build.gradle has correct Kotlin version (1.9.0)
```

### Build Fails - Dependencies
```
Solution: Run ./gradlew clean build --refresh-dependencies
```

### APK Not Installing
```
Solution: Enable "Install from Unknown Sources" in Android settings
```

## Build Variants

### Debug Build
- Includes debugging symbols
- Not optimized
- Larger APK size
```bash
./gradlew assembleDebug
```

### Release Build
- Optimized and minified
- Requires signing
- Smaller APK size
```bash
./gradlew assembleRelease
```

## Signing Configuration

For release builds, add to `app/build.gradle`:

```gradle
android {
    signingConfigs {
        release {
            storeFile file("kread-release.keystore")
            storePassword "your-password"
            keyAlias "kread"
            keyPassword "your-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run on Emulator
1. Create AVD in Android Studio
2. Start emulator
3. Run: `./gradlew installDebug`

### Run on Physical Device
1. Enable Developer Options on device
2. Enable USB Debugging
3. Connect via USB
4. Run: `adb devices` to verify connection
5. Run: `./gradlew installDebug`

## Build Output

After successful build, find files at:

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Build Reports**: `app/build/reports/`
- **Logs**: `app/build/outputs/logs/`

## Clean Build

To clean all build artifacts:
```bash
./gradlew clean
```

## Build with Specific ABI

To build for specific architecture:
```bash
./gradlew assembleDebug -Pandroid.injected.abi=arm64-v8a
```

Available ABIs:
- `armeabi-v7a` (32-bit ARM)
- `arm64-v8a` (64-bit ARM)
- `x86` (32-bit Intel)
- `x86_64` (64-bit Intel)

## Continuous Integration

Example GitHub Actions workflow:

```yaml
name: Build APK
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Build with Gradle
        run: ./gradlew assembleDebug
      - name: Upload APK
        uses: actions/upload-artifact@v2
        with:
          name: app-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

## Next Steps

After building:
1. Test the APK on various Android versions
2. Test on different screen sizes
3. Optimize performance
4. Add more features
5. Publish to Play Store (optional)

## Support

For build issues:
- Check Android Studio logs
- Run with `--stacktrace` flag: `./gradlew assembleDebug --stacktrace`
- Check Gradle console output
- Verify all dependencies are downloaded
