# Kread Terminal Emulator

**Kread** is a powerful terminal emulator for Android, inspired by Termux. It provides a full-featured Linux terminal experience on your Android device.

## Features

- ✅ **Full Terminal Emulation**: VT100/ANSI escape sequence support
- ✅ **Multiple Sessions**: Run multiple terminal sessions simultaneously
- ✅ **Background Service**: Keep sessions running in the background
- ✅ **Custom Environment**: Isolated file system with HOME and PREFIX directories
- ✅ **Keyboard Support**: Full keyboard input with special keys (arrows, tab, etc.)
- ✅ **Native Android**: Built with Kotlin for optimal performance

## Architecture

### Core Components

1. **TerminalEmulator** (`emulator/TerminalEmulator.kt`)
   - Handles VT100/ANSI escape sequences
   - Manages terminal screen buffer
   - Processes cursor movements and text rendering

2. **TerminalSession** (`session/TerminalSession.kt`)
   - Manages shell process lifecycle
   - Handles I/O between terminal and shell
   - Provides environment configuration

3. **TerminalView** (`view/TerminalView.kt`)
   - Custom Android View for rendering terminal
   - Handles touch input and keyboard
   - Displays terminal output with cursor

4. **TerminalService** (`session/TerminalService.kt`)
   - Foreground service to keep sessions alive
   - Manages multiple terminal sessions
   - Provides persistent notification

5. **MainActivity** (`activity/MainActivity.kt`)
   - Main UI controller
   - Manages session lifecycle
   - Handles menu actions

## Directory Structure

```
kread/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/kread/terminal/
│   │       │   ├── activity/
│   │       │   │   └── MainActivity.kt
│   │       │   ├── emulator/
│   │       │   │   └── TerminalEmulator.kt
│   │       │   ├── session/
│   │       │   │   ├── TerminalSession.kt
│   │       │   │   └── TerminalService.kt
│   │       │   ├── view/
│   │       │   │   ├── TerminalView.kt
│   │       │   │   └── TerminalInputConnection.kt
│   │       │   └── KreadApplication.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   ├── values/
│   │       │   │   ├── strings.xml
│   │       │   │   ├── colors.xml
│   │       │   │   └── themes.xml
│   │       │   ├── menu/
│   │       │   │   └── main_menu.xml
│   │       │   ├── drawable/
│   │       │   │   ├── ic_launcher.xml
│   │       │   │   └── ic_launcher_round.xml
│   │       │   └── xml/
│   │       │       └── file_paths.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Building the App

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK (API 24+)
- JDK 17
- Gradle 8.1+

### Build Steps

1. **Clone or open the project in Android Studio**

2. **Update SDK path** in `local.properties`:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   ```

3. **Sync Gradle**:
   - Click "Sync Project with Gradle Files" in Android Studio

4. **Build the APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   
   Or use Android Studio: `Build > Build Bundle(s) / APK(s) > Build APK(s)`

5. **Install on device**:
   ```bash
   ./gradlew installDebug
   ```

## Installation

### From Source

1. Build the APK using the steps above
2. Transfer the APK to your Android device
3. Enable "Install from Unknown Sources" in Android settings
4. Install the APK

### APK Location

After building, find the APK at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Usage

### Basic Commands

Once installed, launch Kread and you'll have access to standard Android shell commands:

```bash
# List files
ls -la

# Check current directory
pwd

# View environment variables
env

# Run shell commands
echo "Hello from Kread!"

# Navigate directories
cd /sdcard
```

### Keyboard Shortcuts

- **Enter**: Execute command
- **Backspace**: Delete character
- **Arrow Keys**: Navigate command history and cursor
- **Tab**: Auto-complete (if supported by shell)

### Menu Options

- **New Session**: Create a new terminal session
- **Keyboard**: Show/focus keyboard
- **About**: Display app information

## Environment

Kread sets up the following environment:

- **HOME**: `/data/data/com.kread.terminal/files/home`
- **PREFIX**: `/data/data/com.kread.terminal/files/usr`
- **PATH**: `$PREFIX/bin:/system/bin:/system/xbin`
- **TERM**: `xterm-256color`
- **SHELL**: `/system/bin/sh`

## Permissions

Kread requires the following permissions:

- **INTERNET**: For network operations
- **WRITE_EXTERNAL_STORAGE**: For file access (Android 9 and below)
- **READ_EXTERNAL_STORAGE**: For file access (Android 9 and below)
- **FOREGROUND_SERVICE**: To keep sessions running
- **WAKE_LOCK**: To prevent device sleep during operations

## Comparison with Termux

| Feature | Kread | Termux |
|---------|-------|--------|
| Terminal Emulation | ✅ VT100/ANSI | ✅ Full VT100 |
| Multiple Sessions | ✅ Yes | ✅ Yes |
| Package Manager | ❌ Not included | ✅ pkg/apt |
| Background Service | ✅ Yes | ✅ Yes |
| Custom Environment | ✅ Yes | ✅ Yes |
| Shell Access | ✅ Android Shell | ✅ Full Linux |

## Extending Kread

### Adding Package Management

To make Kread more like Termux, you can add:

1. **Bootstrap script** to download and extract a Linux environment
2. **Package manager** (pkg/apt) integration
3. **Additional binaries** in the PREFIX directory

### Custom Commands

Add custom commands by:

1. Creating shell scripts in `$PREFIX/bin`
2. Making them executable
3. Adding to PATH

## Troubleshooting

### App crashes on startup
- Check Android version (requires API 24+)
- Verify permissions are granted
- Check logcat for errors

### Keyboard not showing
- Tap the terminal view
- Use the keyboard menu option
- Check input method settings

### Commands not found
- Kread uses Android's built-in shell
- Not all Linux commands are available
- Consider adding a bootstrap for full Linux environment

## Contributing

Contributions are welcome! Areas for improvement:

- Enhanced terminal emulation
- Package management system
- Additional keyboard layouts
- Gesture support
- Split-screen sessions
- Customizable themes

## License

This project is created for educational purposes. Feel free to modify and distribute.

## Credits

Inspired by:
- **Termux**: The original Android terminal emulator
- **Android Terminal Emulator**: Early terminal apps for Android

## Version History

### v1.0.0 (Current)
- Initial release
- Basic terminal emulation
- Multiple session support
- Background service
- VT100/ANSI escape sequences

## Contact

For issues, suggestions, or contributions, please create an issue in the project repository.

---

**Kread** - A Termux-inspired terminal emulator for Android 🚀
