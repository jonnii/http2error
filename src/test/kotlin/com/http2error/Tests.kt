package com.http2error

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.test.extensions.kotest.annotation.MicronautTest
import java.util.UUID

@MicronautTest
class Tests(@Client("/") private val client: ReactorHttpClient) : StringSpec({
    "do things".config(invocations = 10000, threads = 50) {
        val param = UUID.randomUUID().toString()

        val request = HttpRequest.GET<TestController.Thing>("/things?opt=$param")
            .header("x-opt", param)

        val response = client
            .exchange(
                request,
                TestController.Thing::class.java
            ).blockFirst()!!

        response.status.shouldBe(HttpStatus.OK)

        assertSoftly(response.body()) {
            header.shouldBe(param)
            query.shouldBe(param)
        }
    }
})