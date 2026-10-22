# Auth service

AUTH handles account registration and login for the ABL services. It stores passwords as BCrypt hashes, assigns the `pelanggan` role server-side, issues short-lived JWTs, and publishes `UserRegisteredEvent` to RabbitMQ for the Pelanggan service to consume.

## Local configuration

Set these environment variables before starting the service:

- `APP_JWT_SECRET`: random signing secret of at least 32 UTF-8 bytes. The same value will be configured for services that validate these tokens.
- `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`, `APP_ADMIN_EMAIL`: configure all three to bootstrap an admin account. If all are empty, no admin account is created. Admin passwords must be supplied through the environment, never committed to source.
- `DB_USERNAME`, `DB_PASSWORD`: optional H2 credentials (defaults are for local development).
- `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`: RabbitMQ credentials (local defaults are for local development).

PowerShell example (replace placeholders locally; do not commit the values):

```powershell
$env:APP_JWT_SECRET = '<random secret, at least 32 UTF-8 bytes>'
$env:APP_ADMIN_USERNAME = 'admin'
$env:APP_ADMIN_PASSWORD = '<admin password, at least 12 characters>'
$env:APP_ADMIN_EMAIL = 'admin@example.invalid'
.\auth\mvnw.cmd -f auth\pom.xml spring-boot:run
```

AUTH uses H2 in-memory on port `8084`; its data is lost on restart. The service registers with Eureka as `AUTH` and is reachable through Gateway at `/api/auth/**`.

This class-project slice does not yet include distributed rate limiting for login/register or HTTPS termination. Keep it on the local demo network; do not expose the endpoints directly to the public internet until those controls are added.

## Endpoints

`POST /api/auth/register` accepts `username`, `password`, `email`, `nama`, `alamat`, and optional `jenisKelamin` (`LAKI_LAKI` or `PEREMPUAN`). Public registration always creates a `pelanggan` role; clients cannot set their own role.

`POST /api/auth/login` accepts `username` and `password`. Both endpoints return a Bearer JWT response. Registration publishes a durable `user_registered_queue` message; the Pelanggan service consumes it and creates a profile linked by `authUserId`.
