# Kotlin Ktor demo

Ktor + Netty + kotlinx.serialization, with no database. Requires JDK 26.

## Run (PowerShell)

```powershell
.\gradlew.bat run
```

If Java is not on PATH, set `JAVA_HOME` to your JDK directory first:

```powershell
$env:JAVA_HOME = 'C:\Users\Administrator\.jdks\openjdk-26.0.2.1'
.\gradlew.bat run
```

Open http://localhost:8080/health. Stop with Ctrl+C.
The default bind address is `127.0.0.1`; override with `HOST` and `PORT` environment variables.
In IntelliJ IDEA, reload the Gradle project and run the Gradle `application > run` task.

## Endpoints

| Method | Path | Behavior |
| --- | --- | --- |
| GET | `/health` | `{"status":"UP"}` |
| GET | `/api/v1/hello?name=Kotlin` | Greeting; name defaults to Ktor |
| GET | `/api/v1/users/42` | Generated demo user, not a database lookup |
| POST | `/api/v1/echo` | Echo a JSON `message`, trimmed, 1–1000 characters |

Invalid user IDs, invalid JSON and invalid messages return 400 with an `error` field.
Unknown paths return JSON 404.

Use `requests.http` in IntelliJ's HTTP client, or PowerShell:

```powershell
Invoke-RestMethod http://localhost:8080/health
Invoke-RestMethod 'http://localhost:8080/api/v1/hello?name=Kotlin'
Invoke-RestMethod http://localhost:8080/api/v1/users/42
Invoke-RestMethod http://localhost:8080/api/v1/echo -Method Post -ContentType 'application/json' -Body '{"message":"Hello Ktor"}'
```

## Test

```powershell
.\gradlew.bat test
```

Tests load the application's real configuration using Ktor's test engine.
Routing functions in `src/main/kotlin/routes/DemoRoutes.kt` serve the role of controllers.
