# Access & Refresh Token demo

An end-to-end, runnable illustration of the **access-token / refresh-token** pattern used by
almost every real mobile app — with a Ktor backend you run locally and a Clean-Architecture
Android client that refreshes tokens **silently** when they expire.

## What it demonstrates

1. **Login** exchanges credentials for a *short-lived* access token (30s) + a longer-lived
   refresh token.
2. Every call to the protected `GET /profile` carries `Authorization: Bearer <accessToken>`,
   injected by an OkHttp **interceptor** — the app code never touches the header.
3. When the access token expires, the server answers `401`. An OkHttp **`Authenticator`**
   ([`TokenAuthenticator`](data/TokenAuthenticator.kt)) transparently calls `/refresh`, stores the
   new pair, and retries the original request. The suspend `api.profile()` call never sees the 401.
4. **Refresh-token rotation**: each `/refresh` invalidates the old refresh token and issues a new
   one. Replaying an old refresh token is rejected (replay defense).
5. **Logout** revokes the refresh token server-side and clears the local session.

A live **flow log** on screen traces every step, including the silent `401 → refresh → retry`.

## Run it

1. Start the backend (in-memory, restartable):

   ```
   ./gradlew :auth-server:run
   ```

   It listens on `http://0.0.0.0:8090`. Demo credentials: **demo / password**.

2. Run the app, open **App Clones & Real-world → Access & Refresh Tokens**.
   - The client reaches the server by the **host's LAN IP** (`BASE_URL` in
     [`di/AuthModule.kt`](di/AuthModule.kt), also whitelisted for cleartext in
     `res/xml/network_security_config.xml`). Set both to your machine's IP —
     `ipconfig getifaddr en0` (macOS) / `hostname -I` (Linux).
   - Why not `10.0.2.2`? Newer Wi-Fi AVDs expose two `10.0.2.0/24` interfaces (eth0 + wlan0) and
     only one forwards `10.0.2.2` to the host, so connections intermittently time out. The LAN IP
     (reached over Wi-Fi) has no such ambiguity. Keep the emulator's Wi-Fi **on**.
   - Diagnose any network error via Logcat: `adb logcat -s okhttp.OkHttpClient`.

3. Try the flow: **Log in** → **Call protected /profile** (works). Then either wait ~30s for the
   access-token countdown to hit *EXPIRED*, or tap **Force-expire access token (demo)** to do it
   instantly. Now tap **Call protected /profile** again — the log shows the automatic
   `401 → refresh → retry` and the call still succeeds.

   > **Force-expire** calls a demo-only `POST /debug/expire` on the server, which back-dates the
   > current access token so it is rejected on the next use — the honest way to trigger the refresh
   > path on demand without weakening the real flow.

## Try it with curl

```
# login
curl -s localhost:8090/login -H 'Content-Type: application/json' \
     -d '{"username":"demo","password":"password"}'

# protected call (paste the accessToken)
curl -s localhost:8090/profile -H 'Authorization: Bearer <accessToken>'

# rotate (paste the refreshToken)
curl -s localhost:8090/refresh -H 'Content-Type: application/json' \
     -d '{"refreshToken":"<refreshToken>"}'
```

## Layout (Clean Architecture)

```
auth/
├── domain/                     # pure Kotlin, no Android/network types
│   ├── model/                  # AuthTokens, UserProfile
│   ├── AuthRepository.kt       # the boundary the UI depends on
│   ├── TokenStorage.kt         # sync + reactive token access
│   ├── AuthEventLog.kt         # shared observable timeline (what makes refresh visible)
│   └── usecase/                # Login / GetProfile / RefreshTokens / Logout
├── data/
│   ├── remote/                 # Retrofit APIs + DTOs + mappers
│   ├── AccessTokenInterceptor  # attaches the bearer
│   ├── TokenAuthenticator      # ★ silent refresh on 401 (bare, loop-proof)
│   ├── InMemoryTokenStorage
│   └── AuthRepositoryImpl      # happy-path only — token machinery lives in OkHttp
├── di/AuthModule.kt            # Koin: two Retrofit instances (authed + bare-refresh)
└── presentation/               # AuthViewModel + AuthScreen (Route → Screen → content)
```

The server lives in the `:auth-server` Gradle module
([`AuthServer.kt`](../../../../../../../../auth-server/src/main/kotlin/com/example/composelearning/authserver/AuthServer.kt)).
