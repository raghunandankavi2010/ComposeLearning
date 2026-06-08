
# Protocol Buffers — A Hands-On Guide (with a runnable sample)

This document explains **what Protocol Buffers (protobuf) are, how they work on
the wire, and why they are usually a better choice than JSON** — using the
working desktop-server + Android-client sample in this repo as the running
example.

---

## 1. What is Protobuf?

**Protocol Buffers** is a language-neutral, platform-neutral way of serializing
structured data, created by Google. You:

1. Describe your data **once** in a `.proto` schema file.
2. Run the **`protoc`** compiler, which generates type-safe classes in your
   language (Java/Kotlin, Swift, Go, C++, Python, …).
3. Use those generated classes to **encode** objects to a compact binary byte
   array, and **decode** bytes back into objects.

The key idea: the **schema is the contract**. Both ends of a connection compile
against the same `.proto`, so they can never disagree about the shape of the
data. There is no hand-written parsing, no "what does this JSON field mean"
guesswork, and no string keys travelling over the network.

This is different from JSON, where the "schema" lives only in documentation, in
your head, or in a separate validation layer — and the field names are shipped
in **every single message**.

---

## 2. The schema in this sample

See [`proto-models/src/main/proto/contacts.proto`](proto-models/src/main/proto/contacts.proto):

```proto
syntax = "proto3";

package composelearning;

option java_package = "com.example.composelearning.proto";
option java_multiple_files = true;

enum Role {
  ROLE_UNSPECIFIED = 0;   // proto3 requires a 0 default
  ENGINEER = 1;
  DESIGNER = 2;
  MANAGER = 3;
}

message Contact {
  int32  id    = 1;       // the "= 1" is a FIELD NUMBER (a tag), not a value
  string name  = 2;
  string email = 3;
  string phone = 4;
  Role   role  = 5;
  bool   active = 6;
}

message ContactList {
  repeated Contact contacts = 1;   // "repeated" == a list
}
```

Things to notice:

- **Field numbers (`= 1`, `= 2`, …)** are the most important concept. They — not
  the field names — are what gets written to the bytes. This is why you can
  freely **rename** `name` to `fullName` without breaking anyone, but you must
  **never reuse or change a number** for an existing field.
- **Field numbers 1–15 cost one byte** for their tag; 16–2047 cost two bytes.
  Put your hot/most-frequent fields in 1–15.
- **`repeated`** is how you express a list/array.
- **proto3 enums must have a `0` value** that acts as "unset/default".

---

## 3. How the wire format works (the part that makes it small & fast)

Protobuf encodes each field as a sequence of **`(tag, value)`** pairs, where the
`tag` packs together the field number and a 3-bit *wire type* that tells the
decoder how to read the next bytes:

```
tag byte = (field_number << 3) | wire_type
```

Common wire types:

| Wire type | Meaning              | Used by                         |
|-----------|----------------------|---------------------------------|
| 0         | varint               | int32, int64, bool, enum        |
| 1         | 64-bit               | fixed64, double                 |
| 2         | length-delimited     | string, bytes, embedded messages, repeated |
| 5         | 32-bit               | fixed32, float                  |

Two techniques keep it compact:

- **Varints**: small integers take fewer bytes. The number `1` is a single byte.
  A boolean is one byte. There are no quotes, colons, braces, or field names.
- **Length-delimited**: strings and nested messages are written as
  `[tag][length][raw bytes]` — no escaping, no delimiters to scan for.

### See it for real

When you hit `GET /contacts` on the sample server, the first bytes are:

```
0a 36 08 01 12 0c 41 64 61 20 4c 6f 76 65 6c 61 63 65  1a 0f 61 64 61 40 ...
└┬┘ └┬┘ └─┬─┘ └──┬──┘ └──────────┬──────────────────┘
 │   │    │      │               └ "Ada Lovelace" (12 bytes of UTF-8)
 │   │    │      └ field 2 (name), wire type 2, length 0x0c = 12
 │   │    └ field 1 (id) = 1   (08 = field1+varint, 01 = the value)
 │   └ length of this Contact message = 0x36 = 54 bytes
 └ field 1 (contacts) of ContactList, wire type 2 (length-delimited)
```

Notice what is **absent**: the strings `"id"`, `"name"`, `"email"`. The decoder
already knows field `2` is the name because both sides share the schema. JSON,
by contrast, must ship `"name":` with every record.

> In this sample the same 8 contacts serialize to **466 bytes as protobuf vs 957
> bytes as JSON — roughly 51% smaller.** The gap widens as records repeat the
> same field names, which real payloads do constantly. Start the server and open
> `http://localhost:8080/` to see the live numbers, and compare `/contacts` with
> `/contacts.json`.

---

## 4. How the sample is wired together

```
┌─────────────────────┐        GET /contacts            ┌──────────────────────┐
│  :server (desktop)  │  ───────────────────────────▶   │  :app (Android)      │
│  JDK HttpServer     │   application/x-protobuf bytes   │  OkHttp + Compose    │
│  ContactList        │                                  │  ContactList         │
│    .toByteArray()   │                                  │    .parseFrom(bytes) │
└──────────┬──────────┘                                  └───────────┬──────────┘
           │                                                          │
           └───────────────► :proto-models ◄─────────────────────────┘
                         (shared .proto + generated
                          Contact / ContactList classes)
```

- **`:proto-models`** is a plain `java-library` module. It owns `contacts.proto`
  and applies the `com.google.protobuf` Gradle plugin, which runs `protoc` and
  generates the `Contact` / `ContactList` / `Role` Java classes. It is kept
  separate from the Android module on purpose, so the protobuf plugin never has
  to interoperate with the Android Gradle Plugin.
- **`:server`** ([`ServerMain.kt`](server/src/main/kotlin/com/example/composelearning/server/ServerMain.kt))
  is a Kotlin JVM app using the JDK's built-in `com.sun.net.httpserver.HttpServer`
  — **zero web-framework dependencies**. It builds a `ContactList`, calls
  `.toByteArray()`, and serves the bytes.
- **`:app`** decodes them. The only protobuf-specific line on the client is:

  ```kotlin
  val contactList = ContactList.parseFrom(bytes)   // bytes -> typed object
  ```

  See [`ProtobufContactsViewModel.kt`](app/src/main/java/com/example/composelearning/protobufdemo/ProtobufContactsViewModel.kt)
  and [`ProtobufDemoScreen.kt`](app/src/main/java/com/example/composelearning/protobufdemo/ProtobufDemoScreen.kt).

---

## 5. Running it

### Start the desktop server

```bash
./gradlew :server:run
```

You should see:

```
Protobuf demo server running at http://localhost:8080
  /contacts       -> 466 bytes (protobuf)
  /contacts.json  -> 957 bytes (json)
From the Android emulator reach it at http://10.0.2.2:8080
```

Sanity-check it from a terminal:

```bash
curl -s http://localhost:8080/ | cat                 # help + size comparison
curl -s http://localhost:8080/contacts | xxd | head  # raw protobuf bytes
```

### Open the Android client

1. Run the app on an emulator.
2. Open **"Protobuf over HTTP"** from the home list.
3. The server URL defaults to `http://10.0.2.2:8080` (the emulator's alias for
   your machine's `localhost`). Tap **Fetch contacts**.

**Physical device?** The emulator alias won't work. Use your computer's LAN IP
(e.g. `http://192.168.1.20:8080`) and add that IP to the `<domain-config>` in
[`app/src/main/res/xml/network_security_config.xml`](app/src/main/res/xml/network_security_config.xml)
(plain HTTP is otherwise blocked on Android).

---

## 6. Why protobuf is often better than JSON

| Dimension | JSON | Protobuf |
|---|---|---|
| **Size** | Field names + punctuation shipped in every message | Numeric tags + binary values; ~30–60% smaller here |
| **Parse speed** | Text tokenizing, string→number conversions | Direct binary reads; no tokenizing |
| **Schema** | Implicit / in docs | Explicit `.proto` is the single source of truth |
| **Type safety** | Stringly-typed; you cast/validate at runtime | Generated classes; wrong field = compile error |
| **Evolution** | Easy to drift; no enforced rules | Add fields safely via new numbers; old clients ignore unknowns |
| **Cross-language** | Manual model duplication per language | `protoc` generates models for every language |

### The compatibility story (this is the big one)

Because fields are identified by **number**, schemas evolve safely:

- **Add a field** → give it a new, never-before-used number. Old clients simply
  ignore the bytes they don't recognize; new clients see a default if it's
  absent. No coordinated deploy required.
- **Rename a field** → free. The name isn't on the wire.
- **Remove a field** → stop using it, but *reserve* its number so nobody reuses
  it: `reserved 4;`.

This forward/backward compatibility is what lets large systems with many
independently-deployed services keep talking to each other for years.

### When JSON is still the right call

Protobuf is not a silver bullet. Prefer JSON when:

- **Humans read the payload directly** (config files, log lines, debugging by eye).
- **Browser/`fetch` consumers** want zero tooling (though protobuf-ES exists).
- **Ad-hoc / schemaless data** where the shape genuinely isn't known up front.
- **Public REST APIs** where universal, dependency-free consumption matters more
  than bytes.

A common rule of thumb: **JSON at the human-facing edges, protobuf for
internal/service-to-service and mobile traffic where size and speed matter.**

---

## 7. Cheat sheet

```bash
# Regenerate Java classes after editing the .proto:
./gradlew :proto-models:generateProto

# Run the server:
./gradlew :server:run

# Inspect raw bytes vs JSON:
curl -s http://localhost:8080/contacts | xxd | head
curl -s http://localhost:8080/contacts.json
```

- `.toByteArray()` — object → bytes (encode)
- `Message.parseFrom(bytes)` — bytes → object (decode)
- Field **numbers** are the contract; names are cosmetic.
- Never reuse a field number; `reserved` the ones you retire.
- Put frequently-used fields in numbers **1–15** (1-byte tags).
