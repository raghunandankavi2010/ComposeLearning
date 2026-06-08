# Protobuf demo server (Python)

A standalone local server that serves a contact list as Protocol Buffers, for
the **"Protobuf over HTTP"** screen in the ComposeLearning Android app.

Everything it needs is already set up in this folder (a `venv` with the
`protobuf` package and the generated `contacts_pb2.py`).

## Start it

```bash
cd ~/Desktop/protobuf-server
./start.sh
```

You should see:

```
Protobuf demo server running at http://localhost:8080
  /contacts       -> 466 bytes (protobuf)
  /contacts.json  -> 957 bytes (json)
```

Leave this terminal open. Press **Ctrl+C** to stop.

## Connect the Android emulator

The app reaches the server through an `adb reverse` tunnel (most reliable):

```bash
~/Library/Android/sdk/platform-tools/adb reverse tcp:8080 tcp:8080
```

Then in the app set the **Server URL** to `http://localhost:8080` and tap
**Fetch contacts**. (Re-run the `adb reverse` command if you reboot the emulator.)

Alternatively, without adb, use `http://10.0.2.2:8080` in the app.

## Quick checks from a terminal

```bash
curl -s http://localhost:8080/                 # help + size comparison
curl -s http://localhost:8080/contacts | xxd   # raw protobuf bytes
curl -s http://localhost:8080/contacts.json    # same data as JSON
```

## Files

| File | What it is |
|------|------------|
| `contacts.proto`  | The schema (a copy of the one in the Android project) |
| `contacts_pb2.py` | Python classes generated from the schema by `protoc` |
| `server.py`       | The HTTP server (Python stdlib `http.server`) |
| `start.sh`        | Convenience launcher using the bundled `venv` |
| `venv/`           | Virtual env with the `protobuf` runtime preinstalled |

## Regenerating after editing contacts.proto

If you change `contacts.proto`, regenerate the Python class (needs `protoc`):

```bash
protoc --proto_path=. --python_out=. contacts.proto
```
