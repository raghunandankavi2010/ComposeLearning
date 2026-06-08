#!/usr/bin/env bash
# Start the protobuf demo server. Double-clickable / run with ./start.sh
cd "$(dirname "$0")" || exit 1
exec ./venv/bin/python server.py
