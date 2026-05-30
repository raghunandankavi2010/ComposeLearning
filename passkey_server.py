"""
Passkey Server (Local)
Run this on Windows to test FIDO2/WebAuthn flow.

Requirements:
pip install flask pywebauthn flask-cors
"""

import os
import json
from flask import Flask, request, jsonify, session
from flask_cors import CORS
from webauthn import (
    generate_registration_options,
    verify_registration_response,
    generate_authentication_options,
    verify_authentication_response,
    options_to_json,
    base64url_to_bytes
)
from webauthn.helpers.structs import (
    AttestationPreference,
    AuthenticatorSelectionCriteria,
    UserVerificationRequirement,
    RegistrationCredential,
    AuthenticationCredential
)

app = Flask(__name__)
app.secret_key = os.urandom(24)
CORS(app)  # Enable CORS for the Android app to connect

# Mock Database
RP_ID = "localhost"
RP_NAME = "Compose Learning Passkey Demo"
ORIGIN = "android:apk-key-hash:pY9...your_key_hash_here..." # This would normally be your app's hash

users = {} # {username: {user_id, credentials: []}}

@app.route('/register/options', methods=['POST'])
def register_options():
    username = request.json.get('username')
    if not username:
        return "Username required", 400

    user_id = os.urandom(16)

    options = generate_registration_options(
        rp_id=RP_ID,
        rp_name=RP_NAME,
        user_id=user_id,
        user_name=username,
        attestation=AttestationPreference.NONE,
        authenticator_selection=AuthenticatorSelectionCriteria(
            user_verification=UserVerificationRequirement.PREFERRED
        ),
    )

    # Store user_id and challenge in session
    session['register_user'] = {
        'id': user_id.hex(),
        'name': username
    }
    session['challenge'] = options.challenge.hex()

    return options_to_json(options)

@app.route('/register/verify', methods=['POST'])
def register_verify():
    registration_response = request.json
    expected_challenge = bytes.fromhex(session.get('challenge'))

    try:
        verification = verify_registration_response(
            credential=RegistrationCredential.parse_obj(registration_response),
            expected_challenge=expected_challenge,
            expected_origin=ORIGIN,
            expected_rp_id=RP_ID
        )

        # Save credential to "database"
        username = session['register_user']['name']
        if username not in users:
            users[username] = {'id': session['register_user']['id'], 'credentials': []}

        users[username]['credentials'].append({
            'id': verification.credential_id.hex(),
            'public_key': verification.public_key.hex(),
            'sign_count': verification.sign_count
        })

        return jsonify({"status": "success"})
    except Exception as e:
        return str(e), 400

@app.route('/login/options', methods=['POST'])
def login_options():
    username = request.json.get('username')
    user = users.get(username)
    if not user:
        return "User not found", 404

    options = generate_authentication_options(
        rp_id=RP_ID,
        allow_credentials=[base64url_to_bytes(c['id']) for c in user['credentials']]
    )

    session['challenge'] = options.challenge.hex()
    session['login_user'] = username

    return options_to_json(options)

@app.route('/login/verify', methods=['POST'])
def login_verify():
    auth_response = request.json
    username = session.get('login_user')
    user = users.get(username)

    # Find matching credential
    cred_id = auth_response.get('id')
    db_cred = next((c for c in user['credentials'] if c['id'] == cred_id), None)

    try:
        verify_authentication_response(
            credential=AuthenticationCredential.parse_obj(auth_response),
            expected_challenge=bytes.fromhex(session.get('challenge')),
            expected_origin=ORIGIN,
            expected_rp_id=RP_ID,
            credential_public_key=bytes.fromhex(db_cred['public_key']),
            credential_current_sign_count=db_cred['sign_count']
        )
        return jsonify({"status": "authenticated"})
    except Exception as e:
        return str(e), 400

if __name__ == '__main__':
    # On Windows, run with: python passkey_server.py
    # Use 0.0.0.0 to allow connections from your Android device
    app.run(host='0.0.0.0', port=5000, debug=True)
