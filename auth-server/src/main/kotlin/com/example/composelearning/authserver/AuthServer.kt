package com.example.composelearning.authserver

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

/**
 * Access-token / refresh-token demo server.
 *
 *   http://<host>:8090
 *
 * Routes (all JSON):
 *   POST /login    {username,password}  -> 200 TokenResponse | 401 invalid_credentials
 *   GET  /profile  Authorization: Bearer <access>
 *                                       -> 200 ProfileResponse
 *                                       | 401 missing/invalid/expired access token
 *   POST /refresh  {refreshToken}       -> 200 TokenResponse (rotated) | 401 invalid/expired refresh
 *   POST /logout   {refreshToken}       -> 204 (refresh token revoked)
 *
 * Try it from a terminal:
 *   curl -s localhost:8090/login -H 'Content-Type: application/json' \
 *        -d '{"username":"demo","password":"password"}'
 *
 * The Android client reaches this via 10.0.2.2:8090 (emulator alias for host localhost).
 */
private const val PORT = 8090
private const val BEARER_PREFIX = "Bearer "

fun main() {
    val tokenService = TokenService()

    println("Auth demo server listening on http://0.0.0.0:$PORT")
    println("Demo credentials -> username: demo | password: password")
    println("Access-token TTL: ${TokenService.ACCESS_TTL_SECONDS}s  •  Refresh-token TTL: ${TokenService.REFRESH_TTL_SECONDS}s")

    embeddedServer(CIO, port = PORT, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true; ignoreUnknownKeys = true })
        }
        // Any AuthException thrown in a route becomes a clean 401 + ErrorResponse envelope.
        install(StatusPages) {
            exception<AuthException> { call, cause ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("unauthorized", cause.reason))
            }
            exception<Throwable> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("bad_request", cause.message ?: "malformed request"))
            }
        }

        routing {
            post("/login") {
                val body = call.receive<LoginRequest>()
                val tokens = tokenService.login(body.username, body.password)
                println("✅ /login  user=${body.username} -> issued access+refresh")
                call.respond(tokens)
            }

            get("/profile") {
                val access = call.request.header("Authorization")?.removePrefix(BEARER_PREFIX)?.trim()
                val username = tokenService.authenticate(access)
                println("🔓 /profile user=$username (token OK)")
                call.respond(tokenService.profileOf(username))
            }

            post("/refresh") {
                val body = call.receive<RefreshRequest>()
                val tokens = tokenService.refresh(body.refreshToken)
                println("♻️  /refresh -> rotated to new access+refresh")
                call.respond(tokens)
            }

            post("/logout") {
                val body = call.receive<LogoutRequest>()
                tokenService.logout(body.refreshToken)
                println("👋 /logout -> refresh token revoked")
                call.respond(HttpStatusCode.NoContent)
            }

            // Demo-only: force the presented access token to expire immediately so the client's
            // auto-refresh path can be shown on demand instead of waiting out the TTL.
            post("/debug/expire") {
                val access = call.request.header("Authorization")?.removePrefix(BEARER_PREFIX)?.trim()
                tokenService.forceExpire(access)
                println("🧪 /debug/expire -> access token back-dated to expired")
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }.start(wait = true)
}
