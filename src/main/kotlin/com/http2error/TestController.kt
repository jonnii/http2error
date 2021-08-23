package com.http2error

import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import kotlinx.coroutines.delay

@Controller
class TestController {
    @Get("things")
    @ExecuteOn(TaskExecutors.IO)
    suspend fun doThings(
        @QueryValue("opt") opt: String
    ): Thing {
        delay(5L)
        val header = getHeader()
        delay(5L)

        return Thing(
            opt,
            header
        )
    }

    private suspend fun getHeader(): String {
        delay(5L)
        delay(5L)
        delay(5L)
        delay(5L)

        val currentRequest = ServerRequestContext.currentRequest<HttpRequest<Any>>().orElseGet {
            error("Expected a current http server request")
        }

        delay(5L)

        return currentRequest.headers["x-opt"].toString()
    }

    data class Thing(
        val query: String,
        val header: String
    )
}