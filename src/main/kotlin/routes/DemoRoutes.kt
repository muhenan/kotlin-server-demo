package org.example.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.example.ApiError

// Ktor uses routing functions instead of annotation-based controllers.
fun Route.demoRoutes() {
    get("/health") {
        call.respond(HealthResponse("UP"))
    }
    route("/api/v1") {
        get("/hello") {
            val name = call.request.queryParameters["name"]?.trim().orEmpty().ifBlank { "Ktor" }
            call.respond(GreetingResponse("Hello, $name!"))
        }
        get("/users/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null || id <= 0) {
                call.respond(HttpStatusCode.BadRequest, ApiError("id must be a positive integer"))
                return@get
            }
            // Demonstration data only; no persistence or actual user lookup.
            call.respond(UserResponse(id, "Demo user $id"))
        }
        post("/echo") {
            val message = call.receive<EchoRequest>().message.trim()
            if (message.isEmpty() || message.length > 1000) {
                call.respond(HttpStatusCode.BadRequest, ApiError("message must contain 1 to 1000 characters"))
                return@post
            }
            call.respond(EchoResponse(message))
        }
    }
}

@Serializable data class HealthResponse(val status: String)
@Serializable data class GreetingResponse(val message: String)
@Serializable data class UserResponse(val id: Long, val name: String)
@Serializable data class EchoRequest(val message: String)
@Serializable data class EchoResponse(val message: String)
