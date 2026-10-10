package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.example.ApiError

// Ktor uses routing functions instead of annotation-based controllers.
@OptIn(ExperimentalKtorApi::class)
fun Route.demoRoutes() {
    get("/health") {
        call.respond(HealthResponse("UP"))
    }.describe {
        summary = "Health check"
        responses { HttpStatusCode.OK { schema = jsonSchema<HealthResponse>() } }
    }
    route("/api/v1") {
        get("/hello") {
            val name = call.request.queryParameters["name"]?.trim().orEmpty().ifBlank { "Ktor" }
            call.respond(GreetingResponse("Hello, $name!"))
        }.describe {
            summary = "Say hello"
            parameters { query("name") { description = "Name to greet; defaults to Ktor"; required = false } }
            responses { HttpStatusCode.OK { schema = jsonSchema<GreetingResponse>() } }
        }
        get("/users/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null || id <= 0) {
                call.respond(HttpStatusCode.BadRequest, ApiError("id must be a positive integer"))
                return@get
            }
            // Demonstration data only; no persistence or actual user lookup.
            call.respond(UserResponse(id, "Demo user $id"))
        }.describe {
            summary = "Get a demo user"
            description = "Generated demonstration data, not a database lookup."
            parameters { path("id") { description = "Positive integer user ID, e.g. 42"; schema = jsonSchema<Long>() } }
            responses {
                HttpStatusCode.OK { schema = jsonSchema<UserResponse>() }
                HttpStatusCode.BadRequest { description = "ID must be a positive integer"; schema = jsonSchema<ApiError>() }
            }
        }
        post("/echo") {
            val message = call.receive<EchoRequest>().message.trim()
            if (message.isEmpty() || message.length > 1000) {
                call.respond(HttpStatusCode.BadRequest, ApiError("message must contain 1 to 1000 characters"))
                return@post
            }
            call.respond(EchoResponse(message))
        }.describe {
            summary = "Echo a JSON message"
            description = "Trims the message and accepts 1 to 1000 characters."
            requestBody { required = true; schema = jsonSchema<EchoRequest>() }
            responses {
                HttpStatusCode.OK { schema = jsonSchema<EchoResponse>() }
                HttpStatusCode.BadRequest { description = "Invalid JSON or message"; schema = jsonSchema<ApiError>() }
            }
        }
    }
}

@Serializable data class HealthResponse(val status: String)
@Serializable data class GreetingResponse(val message: String)
@Serializable data class UserResponse(val id: Long, val name: String)
@Serializable data class EchoRequest(val message: String)
@Serializable data class EchoResponse(val message: String)
