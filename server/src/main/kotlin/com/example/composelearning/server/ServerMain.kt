package com.example.composelearning.server

import com.example.composelearning.proto.Contact
import com.example.composelearning.proto.ContactList
import com.example.composelearning.proto.Role
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

/**
 * A tiny desktop server with ZERO web-framework dependencies — it uses
 * [HttpServer], which ships inside the JDK (the `jdk.httpserver` module).
 *
 * It exposes the same list of contacts in two formats so you can literally see
 * the size difference protobuf gives you:
 *
 *   GET /contacts       -> protobuf-encoded bytes   (application/x-protobuf)
 *   GET /contacts.json  -> a JSON rendering           (application/json)
 *   GET /               -> a plain-text help page
 *
 * The Android client calls /contacts, gets back a compact byte array, and
 * reconstructs the list with ContactList.parseFrom(bytes).
 */
private const val PORT = 8080

fun main() {
    val contacts = sampleContacts()

    // Build the protobuf message once. This is the canonical builder pattern
    // that protoc generates for every message type.
    val contactList = ContactList.newBuilder()
        .addAllContacts(contacts)
        .build()

    val protoBytes = contactList.toByteArray()
    val jsonBytes = contactsAsJson(contacts).toByteArray(Charsets.UTF_8)

    val server = HttpServer.create(InetSocketAddress("0.0.0.0", PORT), 0)

    server.createContext("/contacts") { exchange ->
        respond(
            exchange,
            status = 200,
            contentType = "application/x-protobuf",
            body = protoBytes
        )
    }

    server.createContext("/contacts.json") { exchange ->
        respond(
            exchange,
            status = 200,
            contentType = "application/json",
            body = jsonBytes
        )
    }

    // RemoteCompose: build a UI document on the fly and serve its bytes.
    // GET /remoteui?variant=N  -> a RemoteCompose document (binary)
    // Rebuilt per request (the clock + variant change the bytes), so the
    // Android player renders whatever this server decides — server-driven UI.
    server.createContext("/remoteui") { exchange ->
        val variant = exchange.requestURI.query
            ?.split('&')
            ?.firstOrNull { it.startsWith("variant=") }
            ?.substringAfter('=')
            ?.toIntOrNull()
            ?: 0
        val doc = RemoteUiDocument.build(variant)
        respond(exchange, 200, "application/octet-stream", doc)
    }

    server.createContext("/") { exchange ->
        // Only the exact root path gets the help page; everything else is 404.
        if (exchange.requestURI.path != "/") {
            respond(exchange, 404, "text/plain", "Not found\n".toByteArray())
            return@createContext
        }
        val help = buildString {
            appendLine("ComposeLearning protobuf demo server")
            appendLine("------------------------------------")
            appendLine("GET /contacts        protobuf bytes  (${protoBytes.size} bytes)")
            appendLine("GET /contacts.json   JSON            (${jsonBytes.size} bytes)")
            appendLine("GET /remoteui?variant=0..${RemoteUiDocument.variantCount() - 1}  RemoteCompose document (binary)")
            appendLine()
            appendLine("Serving ${contacts.size} contacts.")
            appendLine("Protobuf is ${percentSmaller(protoBytes.size, jsonBytes.size)}% smaller than the JSON here.")
        }
        respond(exchange, 200, "text/plain", help.toByteArray())
    }

    server.executor = null // use the default executor
    server.start()

    println("Protobuf demo server running at http://localhost:$PORT")
    println("  /contacts       -> ${protoBytes.size} bytes (protobuf)")
    println("  /contacts.json  -> ${jsonBytes.size} bytes (json)")
    println("From the Android emulator reach it at http://10.0.2.2:$PORT")
    println("Press Ctrl+C to stop.")
}

/** Writes a response and closes the exchange. */
private fun respond(exchange: HttpExchange, status: Int, contentType: String, body: ByteArray) {
    exchange.responseHeaders.add("Content-Type", contentType)
    exchange.sendResponseHeaders(status, body.size.toLong())
    exchange.responseBody.use { it.write(body) }
}

private fun sampleContacts(): List<Contact> = listOf(
    contact(1, "Ada Lovelace", "ada@example.com", "+1-202-555-0143", Role.ENGINEER, true),
    contact(2, "Grace Hopper", "grace@example.com", "+1-202-555-0179", Role.ENGINEER, true),
    contact(3, "Dieter Rams", "dieter@example.com", "+49-30-555-0112", Role.DESIGNER, true),
    contact(4, "Susan Kare", "susan@example.com", "+1-415-555-0190", Role.DESIGNER, false),
    contact(5, "Andy Grove", "andy@example.com", "+1-408-555-0188", Role.MANAGER, true),
    contact(6, "Margaret Hamilton", "margaret@example.com", "+1-617-555-0166", Role.ENGINEER, true),
    contact(7, "Don Norman", "don@example.com", "+1-858-555-0123", Role.DESIGNER, true),
    contact(8, "Katherine Johnson", "katherine@example.com", "+1-757-555-0154", Role.ENGINEER, false)
)

private fun contact(
    id: Int,
    name: String,
    email: String,
    phone: String,
    role: Role,
    active: Boolean
): Contact = Contact.newBuilder()
    .setId(id)
    .setName(name)
    .setEmail(email)
    .setPhone(phone)
    .setRole(role)
    .setActive(active)
    .build()

/** Minimal hand-rolled JSON so we can compare payload sizes without extra deps. */
private fun contactsAsJson(contacts: List<Contact>): String = buildString {
    append("{\"contacts\":[")
    contacts.forEachIndexed { index, c ->
        if (index > 0) append(",")
        append("{")
        append("\"id\":${c.id},")
        append("\"name\":\"${c.name}\",")
        append("\"email\":\"${c.email}\",")
        append("\"phone\":\"${c.phone}\",")
        append("\"role\":\"${c.role.name}\",")
        append("\"active\":${c.active}")
        append("}")
    }
    append("]}")
}

private fun percentSmaller(protoSize: Int, jsonSize: Int): Int = ((jsonSize - protoSize) * 100.0 / jsonSize).toInt()
