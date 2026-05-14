# Security policy

## Project status

Stack Casino is an academic deliverable for the course Desarrollo de
Aplicaciones I (UADE, 2026). It is not maintained as a production
product. Security patches are not promised for any version.

## Supported versions

| Version | Supported              |
| ------- | ---------------------- |
| 0.x     | No (pre-release)       |
| Older   | No                     |

## Reporting a vulnerability

If you discover a vulnerability in the codebase, please follow the
GitHub private disclosure process:

1. Open a private security advisory under the repository's Security tab:
   <https://github.com/nullnullnullnullnullnullnullnullnullnul/stack/security/advisories>
2. Do not disclose the issue publicly until it has been triaged and
   acknowledged.
3. Expect an initial response within 30 days.

Public issues for vulnerabilities are not the correct channel and will
be closed without comment.

## Secrets and sensitive data

The repository must not contain credentials, API keys, signing keys,
or other secrets. The following files are gitignored and must be
provided per developer or per environment:

- `local.properties` (Android SDK path, optional API keys).
- `secrets.properties` (third-party API keys, when present).
- `app/google-services.json` (Firebase project configuration).
- `*.jks`, `*.keystore` (signing keystores for release builds).

If a secret is committed accidentally, treat the secret as compromised:
rotate it immediately and force-push only after rotation. Notify the
project owner.

## On-device security model

- The house wallet private key is stored encrypted in Android Keystore
  via EncryptedSharedPreferences. It never leaves the device and is
  never transmitted over the network.
- Every operation that touches the private key requires biometric
  authentication via BiometricPrompt.
- If the device has no system unlock method configured, house wallet
  operations are blocked.

## Cryptographic primitives

- HMAC-SHA256 (RFC 2104) is the only cryptographic primitive used by
  the provably fair engine. It is implemented from FIPS 180-4 and
  RFC 2104 in `cpp_engine/include/Sha256.h`, without external
  dependencies.
- TLS for network calls is provided by the Android system and by
  OkHttp (Retrofit's default client).
