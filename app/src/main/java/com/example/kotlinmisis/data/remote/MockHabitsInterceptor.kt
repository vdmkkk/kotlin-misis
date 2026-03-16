package com.example.kotlinmisis.data.remote

import com.example.kotlinmisis.data.remote.dto.CreateHabitRequest
import com.example.kotlinmisis.data.remote.dto.HabitDto
import com.example.kotlinmisis.data.remote.dto.UpdateHabitRequest
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

class MockHabitsInterceptor(
    private val gson: Gson
) : Interceptor {
    private val lock = Any()
    private val store = linkedMapOf<String, HabitDto>()

    override fun intercept(chain: Interceptor.Chain): Response {
        Thread.sleep(300)

        val request = chain.request()
        val segments = request.url.pathSegments.filter { it.isNotBlank() }
        val method = request.method.uppercase()

        val (code, json) = synchronized(lock) {
            when {
                method == "GET" && segments == listOf("habits") -> {
                    200 to gson.toJson(store.values.sortedByDescending { it.createdAt })
                }

                method == "GET" && segments.size == 2 && segments[0] == "habits" -> {
                    val id = segments[1]
                    val habit = store[id]
                    if (habit != null) 200 to gson.toJson(habit)
                    else 404 to """{"detail":"Not found"}"""
                }

                method == "POST" && segments == listOf("habits") -> {
                    val body = readBody(request.body)
                    val req = gson.fromJson(body, CreateHabitRequest::class.java)
                    val dto = HabitDto(
                        id = req.id, title = req.title, description = req.description,
                        frequency = req.frequency, colorHex = req.colorHex,
                        createdAt = req.createdAt, lastCompletedDate = null
                    )
                    store[dto.id] = dto
                    201 to gson.toJson(dto)
                }

                method == "PUT" && segments.size == 2 && segments[0] == "habits" -> {
                    val id = segments[1]
                    val existing = store[id]
                    if (existing == null) {
                        404 to """{"detail":"Not found"}"""
                    } else {
                        val body = readBody(request.body)
                        val req = gson.fromJson(body, UpdateHabitRequest::class.java)
                        val updated = existing.copy(
                            lastCompletedDate = req.lastCompletedDate
                        )
                        store[id] = updated
                        200 to gson.toJson(updated)
                    }
                }

                method == "DELETE" && segments.size == 2 && segments[0] == "habits" -> {
                    store.remove(segments[1])
                    204 to ""
                }

                else -> 404 to """{"detail":"Not found"}"""
            }
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code < 300) "OK" else "Error")
            .body(json.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun readBody(body: okhttp3.RequestBody?): String {
        val buffer = Buffer()
        body?.writeTo(buffer)
        return buffer.readUtf8()
    }
}
