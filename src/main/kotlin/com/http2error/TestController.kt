package com.http2error

import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpAttributes
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.web.router.RouteMatch
import io.micronaut.web.router.UriRouteMatch
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Optional
import java.util.function.Function

@Filter("/**")
class SomeFilter : HttpServerFilter {
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>> {
        return Publishers.map(chain.proceed(request)) {
            it
        }
    }
}

@Controller
open class TestController {
    private val LOGGER: Logger = LoggerFactory.getLogger(TestController::class.java)

    @Get("things")
    @ExecuteOn(TaskExecutors.IO)
    fun doThings(
        @QueryValue("opt") opt: String,
        @Header("x-opt") cHeader: String
    ): Thing {
        Thread.sleep(1000)

        repeat(10) {
            ServerRequestContext.currentRequest<HttpRequest<Any>>().orElseGet {
                error("Expected a current http server request")
            }
        }

        val currentRequest = ServerRequestContext.currentRequest<HttpRequest<Any>>().orElseGet {
            error("Expected a current http server request")
        }

        val header = currentRequest.headers["x-opt"].toString()

        if (header != cHeader) {
            LOGGER.info("Headers dont match")
            error("headers dont match")
        }

        return Thing(
            opt,
            header,
            cHeader
        )
    }

    data class Thing(
        val query: String,
        val header: String,
        val cHeader: String
    )
}