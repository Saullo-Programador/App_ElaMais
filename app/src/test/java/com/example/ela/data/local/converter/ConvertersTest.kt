package com.example.ela.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `fromList deve transformar a lista em JSON`() {
        assertEquals("""["a","b"]""", converters.fromList(listOf("a", "b")))
    }

    @Test
    fun `toList deve transformar o JSON em lista`() {
        assertEquals(listOf("a", "b"), converters.toList("""["a","b"]"""))
    }

    @Test
    fun `lista vazia deve fazer o caminho de ida e volta`() {
        assertTrue(converters.toList(converters.fromList(emptyList())).isEmpty())
    }

    @Test
    fun `lista com caracteres especiais deve fazer o caminho de ida e volta`() {
        val original = listOf("Açaí", "café, com leite", "\"aspas\"", "Chocolate 🍫")

        assertEquals(original, converters.toList(converters.fromList(original)))
    }
}
