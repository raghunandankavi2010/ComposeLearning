#!/usr/bin/env python3
"""
Tiny desktop server that serves a list of contacts as Protocol Buffers.

It mirrors the Kotlin :server module in the ComposeLearning project, using the
SAME contacts.proto schema, so the Android client decodes its bytes identically.

Endpoints:
    GET /contacts       -> protobuf bytes      (application/x-protobuf)
    GET /contacts.json  -> JSON rendering        (application/json)
    GET /               -> plain-text help + size comparison

Run it:  ./start.sh        (or:  venv/bin/python server.py)
"""
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

import contacts_pb2  # generated from contacts.proto by protoc

PORT = 8080

# ---- Build the list once at startup ---------------------------------------
_PEOPLE = [
    (1, "Ada Lovelace", "ada@example.com", "+1-202-555-0143", contacts_pb2.ENGINEER, True),
    (2, "Grace Hopper", "grace@example.com", "+1-202-555-0179", contacts_pb2.ENGINEER, True),
    (3, "Dieter Rams", "dieter@example.com", "+49-30-555-0112", contacts_pb2.DESIGNER, True),
    (4, "Susan Kare", "susan@example.com", "+1-415-555-0190", contacts_pb2.DESIGNER, False),
    (5, "Andy Grove", "andy@example.com", "+1-408-555-0188", contacts_pb2.MANAGER, True),
    (6, "Margaret Hamilton", "margaret@example.com", "+1-617-555-0166", contacts_pb2.ENGINEER, True),
    (7, "Don Norman", "don@example.com", "+1-858-555-0123", contacts_pb2.DESIGNER, True),
    (8, "Katherine Johnson", "katherine@example.com", "+1-757-555-0154", contacts_pb2.ENGINEER, False),
]


def build_contact_list():
    contact_list = contacts_pb2.ContactList()
    for cid, name, email, phone, role, active in _PEOPLE:
        c = contact_list.contacts.add()
        c.id = cid
        c.name = name
        c.email = email
        c.phone = phone
        c.role = role
        c.active = active
    return contact_list


_CONTACT_LIST = build_contact_list()
_PROTO_BYTES = _CONTACT_LIST.SerializeToString()  # <-- object -> protobuf bytes


def _json_bytes():
    role_name = contacts_pb2.Role.Name
    payload = {
        "contacts": [
            {
                "id": c.id,
                "name": c.name,
                "email": c.email,
                "phone": c.phone,
                "role": role_name(c.role),
                "active": c.active,
            }
            for c in _CONTACT_LIST.contacts
        ]
    }
    # separators without spaces == the most compact JSON, a fair comparison
    return json.dumps(payload, separators=(",", ":")).encode("utf-8")


_JSON_BYTES = _json_bytes()


class Handler(BaseHTTPRequestHandler):
    def _send(self, status, content_type, body):
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path == "/contacts":
            self._send(200, "application/x-protobuf", _PROTO_BYTES)
        elif self.path == "/contacts.json":
            self._send(200, "application/json", _JSON_BYTES)
        elif self.path == "/":
            pct = int((len(_JSON_BYTES) - len(_PROTO_BYTES)) * 100 / len(_JSON_BYTES))
            help_text = (
                "ComposeLearning protobuf demo server (Python)\n"
                "---------------------------------------------\n"
                f"GET /contacts       protobuf bytes  ({len(_PROTO_BYTES)} bytes)\n"
                f"GET /contacts.json  JSON            ({len(_JSON_BYTES)} bytes)\n\n"
                f"Serving {len(_PEOPLE)} contacts.\n"
                f"Protobuf is {pct}% smaller than the JSON here.\n"
            )
            self._send(200, "text/plain", help_text.encode("utf-8"))
        else:
            self._send(404, "text/plain", b"Not found\n")

    # quieter logging
    def log_message(self, fmt, *args):
        print("%s - %s" % (self.address_string(), fmt % args))


def main():
    server = ThreadingHTTPServer(("0.0.0.0", PORT), Handler)
    print(f"Protobuf demo server running at http://localhost:{PORT}")
    print(f"  /contacts       -> {len(_PROTO_BYTES)} bytes (protobuf)")
    print(f"  /contacts.json  -> {len(_JSON_BYTES)} bytes (json)")
    print("If using adb reverse, the Android app URL is http://localhost:8080")
    print("Press Ctrl+C to stop.")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping server.")
        server.shutdown()


if __name__ == "__main__":
    main()
