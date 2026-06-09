# Passkeys: The Future of Authentication

Passkeys are a passwordless authentication standard based on FIDO2 and WebAuthn. They allow users to sign in to apps and websites using the same biometrics or screen lock they use to unlock their devices.

## 🚀 Getting Started

### 1. Run the Local Server
I have provided a local Python server to handle the FIDO2/WebAuthn handshake.
- **File**: `passkey_server.py`
- **Command**: `python passkey_server.py`
- **Dependencies**: `pip install flask pywebauthn flask-cors`

### 2. Run the Android App
1. Open the **Compose Learning** app on your device.
2. Navigate to **Passkeys Demo** from the Home screen.
3. Tap **Register a Passkey** to create a new credential.
4. Use **Sign in with Passkey** to authenticate using your fingerprint/face.

---

## 🧠 How it Works (The Math & Logic)

Passkeys are built on **Asymmetric Cryptography**. Unlike passwords, which are shared secrets, passkeys use a Public/Private key pair.

### Phase 1: Registration
1. **Device Side**: Your Android device generates a unique cryptographic key pair for the specific app (localhost in this demo).
2. **Private Key**: Stays securely on your device (in the Hardware Security Module/Secure Element). It is never sent to the internet.
3. **Public Key**: Sent to our Python server. The server stores this in its "database".

### Phase 2: Authentication (The Challenge)
When you try to log in, the "handshake" happens:
1. **Server**: Sends a random string of data called a **Challenge**.
2. **Device**: Asks for your biometric (fingerprint). Once verified, the device uses its **Private Key** to "sign" the challenge.
3. **Verification**: The device sends the signature back to the server.
4. **Result**: The server uses your **Public Key** to verify the signature. If it matches, you are logged in.

### Why is this Secure?
- **Phishing Proof**: Passkeys are bound to the domain (RP ID). A fake website cannot request a signature for the real app.
- **No Shared Secret**: Even if the server is hacked, the hackers only get the Public Keys, which cannot be used to log in.
- **Privacy**: Your biometric data never leaves your device. The OS only tells the app "The user is verified."

---

## 🛠 Developer Integration (Android)

In Compose, we use the `Credential Manager` API:

```kotlin
val credentialManager = CredentialManager.create(context)

// Registration
val createRequest = CreatePublicKeyCredentialRequest(requestJsonFromServer)
val result = credentialManager.createCredential(context, createRequest)

// Authentication
val getRequest = GetCredentialRequest(listOf(GetPublicKeyCredentialOption(requestJson)))
val result = credentialManager.getCredential(context, getRequest)
```

## 📝 Resources
- [Google Passkey Documentation](https://developer.android.com/identity/sign-in/passkeys)
- [FIDO Alliance](https://fidoalliance.org/)
- [WebAuthn Guide](https://webauthn.guide/)
