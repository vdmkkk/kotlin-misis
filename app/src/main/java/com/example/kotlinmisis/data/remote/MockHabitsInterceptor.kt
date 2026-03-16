package com.example.kotlinmisis.data.remote

import com.example.kotlinmisis.data.remote.dto.HabitDto
import com.example.kotlinmisis.data.remote.dto.SyncHabitsRequest
import com.example.kotlinmisis.data.remote.dto.SyncHabitsResponse
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
    private val remoteHabits = linkedMapOf<String, HabitDto>()

    override fun intercept(chain: Interceptor.Chain): Response {
        Thread.sleep(500)

        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method.uppercase()

        val responseJson = synchronized(lock) {
            when {
                method == "GET" && path.endsWith("/habits") -> {
                    gson.toJson(remoteHabits.values.sortedByDescending { it.createdAt })
                }

                method == "POST" && path.endsWith("/habits/sync") -> {
                    val body = request.body ?: error("Missing request body")
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    val payload = gson.fromJson(buffer.readUtf8(), SyncHabitsRequest::class.java)

                    payload.habits.forEach { habit ->
                        remoteHabits[habit.id] = habit
                    }

                    gson.toJson(SyncHabitsResponse(uploadedCount = payload.habits.size))
                }

                else -> {
                    """{"message":"Not found"}"""
                }
            }
        }

        val responseCode = when {
            method == "GET" && path.endsWith("/habits") -> 200
            method == "POST" && path.endsWith("/habits/sync") -> 200
            else -> 404
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(responseCode)
            .message(if (responseCode == 200) "OK" else "Not found")
            .body(responseJson.toResponseBody("application/json".toMediaType()))
            .build()
    }
}
