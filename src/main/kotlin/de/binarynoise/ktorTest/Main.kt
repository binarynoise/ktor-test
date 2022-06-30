package de.binarynoise.ktorTest

import kotlinx.coroutines.runBlocking
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

object Main {
    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {
        val c = HttpClient(Java) { }
        println(c.get("example.com").bodyAsText())
    }
}
