package ru.quipy

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.loadtest4j.LoadTester
import org.loadtest4j.Request
import org.loadtest4j.Result
import org.loadtest4j.drivers.jmeter.JMeterBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import ru.quipy.api.*
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LoadTestTwo {

    @LocalServerPort
    private val port: Int = 8080

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun testCreateProject() {
        val projectTitle = "Test Project"

        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        val creatorId = responseUser.body?.userId

        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем, что запрос вернул статус 200 OK
        Assertions.assertEquals(response.statusCode, HttpStatus.OK)

        // Проверяем, что ответ содержит созданный проект
        val projectCreatedEvent = response.body
        Assertions.assertNotNull(projectCreatedEvent)
        Assertions.assertNotNull(projectCreatedEvent?.projectId)
        Assertions.assertEquals(projectCreatedEvent?.title, projectTitle)
        Assertions.assertEquals(projectCreatedEvent?.creatorId, creatorId)
    }
    val loadTesterBuilder = JMeterBuilder.withUrl("http", "localhost", 8080)
    // *numThreads* - Количество пользователей
    // *Ramp-Up Period* - указывает JMeter, какую задержку перед запуском следующего пользователя нужно сделать.
    //      Например, если у нас 100 пользователей и период Ramp-Up 100 секунд,
    //      то задержка между запуском пользователей составит 1 секунду (100 секунд /100 пользователей)
    // numThreads / RumpUp = количество запросов в секунду

    fun loadGeneralTest(numThreads: Int, rampUp: Int): Result {
        val projectTitle = "Test Project"

        val userName = UUID.randomUUID().toString()
        val nickname = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()

        val responseUser = restTemplate.postForEntity(
            "http://localhost:$port/users/1/$userName?nickname=$nickname&password=$password",
            null,
            UserCreatedEvent::class.java
        )

        val creatorId = responseUser.body?.userId

        val response = restTemplate.postForEntity(
            "http://localhost:$port/projects/$projectTitle?creatorId=$creatorId",
            null,
            ProjectCreatedEvent::class.java
        )

        // Проверяем, что запрос вернул статус 200 OK
        Assertions.assertEquals(response.statusCode, HttpStatus.OK)

        val loadTester: LoadTester = loadTesterBuilder
            .withNumThreads(numThreads) // Количество пользователей
            .withRampUp(rampUp)
            .build()

        val result = loadTester.run(
            listOf(
                Request.put("http://localhost:$port/projects/$projectTitle?creatorId=$creatorId")
                    .withHeader("Accept", "*/*")
            )
        )
        buildTable(result)
        Assertions.assertTrue(result.percentOk > 99.99f)
        Assertions.assertTrue(result.diagnostics.requestsPerSecond >= (numThreads / rampUp - 0.1f))
        return result
    }

    fun buildTable(result: Result) {
        println(
            "${'-'.enlarge(83, "-")}\n" +
                    "Test Duration (s) | Requests count | Requests per second | Ok requests | Ok percent\n" +
                    " ${result.diagnostics.duration.seconds.enlarge(17)}|" +
                    " ${result.diagnostics.requestCount.total.enlarge(15)}|" +
                    " ${result.diagnostics.requestsPerSecond.enlarge(20)}|" +
                    " ${result.diagnostics.requestCount.ok.enlarge(12)}|" +
                    " ${result.percentOk.enlarge(10)}\n" +
                    " ${'-'.enlarge(82, "-")}\n"
        )
    }

    fun Any.enlarge(length: Int, symbol: String = " "): String {
        var str = this.toString()
        if (str.length < length) {
            str = str.plus(symbol.repeat(length - str.length))
        }
        return str
    }

    @Test
    fun loadTest20RequestsPerSeconds() {
        val result = loadGeneralTest(100, 5)
        Assertions.assertTrue(result.diagnostics.duration.seconds <= 5)
    }

    @Test
    fun loadTest50RequestsPerSeconds() {
        val result = loadGeneralTest(250, 5)
        Assertions.assertTrue(result.diagnostics.duration.seconds <= 5)
    }

    @Test
    fun loadTest100RequestsPerSeconds() {
        val result = loadGeneralTest(500, 5)
        Assertions.assertTrue(result.diagnostics.duration.seconds <= 5)
    }


    /**
    FAIL
    -----------------------------------------------------------------------------------
    Test Duration (s) | Requests count | Requests per second | Ok requests | Ok percent
    4                | 650            | 131.1276982045592   | 626         | 96.3076923076923
    ----------------------------------------------------------------------------------
     */
    @Test
    fun loadTest130RequestsPerSeconds() {
        val result = loadGeneralTest(650, 5)
        Assertions.assertTrue(result.diagnostics.duration.seconds <= 5)
    }


    /**
    fail
    -----------------------------------------------------------------------------------
    Test Duration (s) | Requests count | Requests per second | Ok requests | Ok percent
    62               | 2500           | 39.91506075072246   | 1565        | 62.6
    ----------------------------------------------------------------------------------
    65 per second
     */
    @Test
    fun loadTest650Requests10() {
        val result = loadGeneralTest(650, 10)
        Assertions.assertTrue(result.diagnostics.duration.seconds <= 10)
    }


    /**
    OK?
    -----------------------------------------------------------------------------------
    Test Duration (s) | Requests count | Requests per second | Ok requests | Ok percent
    6                | 650            | 92.93680297397769   | 649         | 99.84615384615385
    ----------------------------------------------------------------------------------
     */
    @Test
    fun loadTest650Requests7() {
        val result = loadGeneralTest(650, 7)
        Assertions.assertTrue(result.diagnostics.duration.seconds <= 7)
    }

}