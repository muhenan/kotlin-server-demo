package org.example

import io.ktor.http.HttpStatusCode
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.response.respondResource
import io.ktor.server.routing.get
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.routing.openapi.hide
import io.ktor.utils.io.ExperimentalKtorApi
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.example.routes.demoRoutes

fun main(args: Array<String>) = EngineMain.main(args)

@OptIn(ExperimentalKtorApi::class)
fun Application.module() {
    install(ContentNegotiation) { json() }
    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ApiError("Invalid request body"))
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ApiError("Endpoint not found"))
        }
    }
    routing {
        demoRoutes()
        get("/swagger-assets@5.31.0/{file}") {
            val file = call.parameters["file"]
            val assets = setOf("swagger-ui.css", "swagger-ui-bundle.js", "swagger-ui-standalone-preset.js", "favicon-32x32.png", "oauth2-redirect.js")
            if (file in assets) {
                call.respondResource("5.31.0/$file", "META-INF/resources/webjars/swagger-ui")
            } else {
                call.respond(HttpStatusCode.NotFound, ApiError("Endpoint not found"))
            }
        }.hide()
        swaggerUI("/swagger") {
            info = OpenApiInfo("Kotlin Ktor Demo", "1.0")
            source = OpenApiDocSource.Routing()
            remotePath = "openapi.json"
            version = "5.31.0"
            packageLocation = "/swagger-assets"
            faviconLocation = "/swagger-assets@5.31.0/favicon-32x32.png"
            deepLinking = true
        }
    }
}

@Serializable
data class ApiError(val error: String)
