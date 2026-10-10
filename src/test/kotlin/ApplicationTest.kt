package org.example

import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun `Swagger serves local assets and documents all demo endpoints`() = testApplication {
        environment { config = io.ktor.server.config.ApplicationConfig("application.conf") }
        val page = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, page.status)
        assertTrue(page.bodyAsText().contains("/swagger-assets@5.31.0/swagger-ui-bundle.js"))
        for (asset in listOf("swagger-ui.css", "swagger-ui-bundle.js", "swagger-ui-standalone-preset.js")) {
            assertEquals(HttpStatusCode.OK, client.get("/swagger-assets@5.31.0/$asset").status)
        }
        val response = client.get("/swagger/openapi.json")
        assertEquals(HttpStatusCode.OK, response.status)
        val doc = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val paths = doc.getValue("paths").jsonObject
        assertEquals(setOf("/health", "/api/v1/hello", "/api/v1/users/{id}", "/api/v1/echo"), paths.keys)
        val user = paths.getValue("/api/v1/users/{id}").jsonObject.getValue("get").jsonObject
        assertEquals(true, user.getValue("parameters").jsonArray.single().jsonObject.getValue("required").jsonPrimitive.boolean)
        assertTrue(user.getValue("responses").jsonObject.containsKey("400"))
        val echo = paths.getValue("/api/v1/echo").jsonObject.getValue("post").jsonObject
        assertTrue(echo.getValue("requestBody").jsonObject.getValue("content").jsonObject.containsKey("application/json"))
    }

    @Test
    fun `GET endpoints support query and path parameters`() = testApplication {
        environment { config = io.ktor.server.config.ApplicationConfig("application.conf") }
        val health = client.get("/health")
        assertEquals(HttpStatusCode.OK, health.status)
        assertEquals(ContentType.Application.Json, health.contentType()?.withoutParameters())
        assertEquals("UP", Json.parseToJsonElement(health.bodyAsText()).jsonObject["status"]?.jsonPrimitive?.content)
        assertEquals("{\"message\":\"Hello, Ktor!\"}", client.get("/api/v1/hello").bodyAsText())
        assertEquals("{\"message\":\"Hello, Kotlin!\"}", client.get("/api/v1/hello?name=Kotlin").bodyAsText())
        assertEquals("{\"id\":42,\"name\":\"Demo user 42\"}", client.get("/api/v1/users/42").bodyAsText())
        assertEquals(HttpStatusCode.BadRequest, client.get("/api/v1/users/abc").status)
        assertEquals(HttpStatusCode.BadRequest, client.get("/api/v1/users/0").status)
    }

    @Test
    fun `POST accepts JSON and rejects invalid input`() = testApplication {
        environment { config = io.ktor.server.config.ApplicationConfig("application.conf") }
        suspend fun echo(body: String) = client.post("/api/v1/echo") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val response = echo("""{"message":" Hello Ktor "}""")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("{\"message\":\"Hello Ktor\"}", response.bodyAsText())
        for (body in listOf("""{"message":" "}""", "{}", "{broken", """{"message":"${"a".repeat(1001)}"}""")) {
            val invalid = echo(body)
            assertEquals(HttpStatusCode.BadRequest, invalid.status)
            assertTrue(Json.parseToJsonElement(invalid.bodyAsText()).jsonObject.containsKey("error"))
        }
    }

    @Test
    fun `unknown endpoint returns JSON 404`() = testApplication {
        environment { config = io.ktor.server.config.ApplicationConfig("application.conf") }
        val response = client.get("/missing")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("{\"error\":\"Endpoint not found\"}", response.bodyAsText())
    }
}

