# GeminiApiComposeStarter - Lab Assignment 1

This repository contains the complete implementation for Mobile Application Development Lab Assignment 1. It features a fully functional chat application powered by the Google Gemini API, with a robust Jetpack Compose UI, local data persistence, user preferences, and advanced security practices for handling API keys.

## Features Implemented

1. **Jetpack Compose Chat UI**: 
   - A modern, responsive chat interface using `LazyColumn` for rendering messages.
   - Distinct, aesthetically pleasing chat bubbles for user messages and Gemini responses.
   - Automatically scrolls to the latest message.
   - Responsive layouts optimized for both portrait and landscape on phones and tablets.
   
2. **Gemini API Integration**: 
   - Configured with `gemini-pro` model.
   - Fully stateful UI management handling Loading, Success, and Error states cleanly via `StateFlow` and `collectAsStateWithLifecycle()`.
   
3. **Voice Input (Speech Recognition)**: 
   - Uses `RecognizerIntent` and `rememberLauncherForActivityResult` to seamlessly convert speech to text and append it to the prompt box without breaking normal keyboard input.

4. **Robust Persistence (Room DB)**: 
   - All conversations are stored in a local Room database (`ChatDatabase`).
   - Chat history survives app restarts.
   - Background I/O execution handled via Coroutine Dispatchers.

5. **User Preferences (DataStore)**: 
   - Theme selection (System, Light, Dark) saved in Preferences DataStore.
   - Secure encrypted API key ciphertext stored securely in DataStore.

6. **Advanced Security (AES-256-GCM & Android KeyStore)**: 
   - The application does **not** store plaintext API keys anywhere in shared preferences or databases.
   - Instead, on first launch, an AES-256-GCM symmetric key is generated inside the hardware-backed **Android KeyStore** using `KeyGenParameterSpec`.
   - The raw Gemini API key (from `BuildConfig`) is encrypted, and only the ciphertext is saved to DataStore.
   - The decrypted API key exists only temporarily in memory when initializing the Gemini `GenerativeModel`.

## Submission Checklist

### Branch and Pull Request
To submit this assignment:
1. Create a branch starting with your roll number:
   ```bash
   git checkout -b N170_Manassingh_Assignment
   ```
2. Commit your code and push:
   ```bash
   git add .
   git commit -m "Complete Assignment 1 Features"
   git push -u origin N170_Manassingh_Assignment
   ```
3. Open a Pull Request on GitHub. Make sure your PR description lists the implemented features and includes screenshots of the UI.

### No Secrets in History
- The `local.properties` file has been added to `.gitignore` and is explicitly excluded from being tracked by git.
- Verify that your PR diff and git log contain NO API key.

## API Key Configuration

**IMPORTANT**: Never hardcode your API key in source files or commit it to version control!

1. Locate or create a `local.properties` file in the root of the project. (This file is ignored by git).
2. Look at `local.properties.example` for the required format.
3. Add your key to `local.properties`:
   ```
   GEMINI_API_KEY=AIzaSyYourRealKeyGoesHere
   ```
4. *Alternatively for CI/CD environments*, the build script will automatically fall back to reading the `GEMINI_API_KEY` system environment variable.

### Production Security Notice
While this application demonstrates advanced client-side encryption (Android KeyStore + AES-256-GCM) to prevent basic extraction of the API key from a rooted device's XML/DataStore files, **client-side encryption cannot completely hide an API key**. An attacker could theoretically reverse-engineer the app, call your decryption function, or hook into the network requests. 

For a true production application:
- Implement a **backend proxy server**. Your app authenticates to your server, and your server holds the Gemini API key and makes requests to Google.
- Use **Firebase App Check** to ensure only genuine instances of your app can communicate with your backend.
- Use API Key restrictions (e.g., restrict by Android package name and SHA-1 fingerprint) in the Google Cloud Console.

## Building and Running

### Build the App
```bash
./gradlew assembleDebug
```

### Release Build (with Minification)
The release build uses R8 minification (`isMinifyEnabled = true`) with custom ProGuard rules to optimize size and obfuscate code while preserving Room and Generative AI SDK reflection points.
```bash
./gradlew assembleRelease
```

### Testing
The project includes thorough testing, as requested:
- **Unit Testing**: `ChatViewModelTest` extensively tests the View Model using `kotlinx-coroutines-test` and a fake `GeminiRepository`.
- **UI Testing**: `ChatScreenTest` validates the Compose screen logic using `createComposeRule()`.

```bash
# Run Unit tests
./gradlew testDebugUnitTest

# Build (or run) UI Tests
./gradlew assembleAndroidTest
```
