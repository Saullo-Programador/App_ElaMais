package com.example.ela.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class JsonUtilsTest {

    @Test
    fun `encode de lista vazia deve gerar array vazio`() {
        assertEquals("[]", JsonUtils.encode(emptyList()))
    }

    @Test
    fun `encode deve gerar um array JSON com os itens`() {
        assertEquals("""["Pizza","Sorvete"]""", JsonUtils.encode(listOf("Pizza", "Sorvete")))
    }

    @Test
    fun `decode de array vazio deve retornar lista vazia`() {
        assertTrue(JsonUtils.decode("[]").isEmpty())
    }

    @Test
    fun `decode deve retornar os itens na mesma ordem`() {
        assertEquals(listOf("a", "b", "c"), JsonUtils.decode("""["a","b","c"]"""))
    }

    @Test
    fun `encode e decode devem preservar acentos e emojis`() {
        val original = listOf("Açaí", "Pão de queijo", "Chocolate 🍫", "Coração ❤️")

        assertEquals(original, JsonUtils.decode(JsonUtils.encode(original)))
    }

    @Test
    fun `encode e decode devem preservar aspas virgulas e barras`() {
        val original = listOf("Ele disse \"oi\"", "a, b, c", "C:\\pasta", "linha1\nlinha2")

        assertEquals(original, JsonUtils.decode(JsonUtils.encode(original)))
    }

    @Test
    fun `encode e decode devem preservar itens vazios e repetidos`() {
        val original = listOf("", "x", "x", "")

        assertEquals(original, JsonUtils.decode(JsonUtils.encode(original)))
    }

    @Test
    fun `decode de texto que nao e JSON deve lancar excecao`() {
        assertFailsWith<IllegalArgumentException> {
            JsonUtils.decode("isto não é json")
        }
    }

    @Test
    fun `decode de JSON que nao e uma lista deve lancar excecao`() {
        assertFailsWith<IllegalArgumentException> {
            JsonUtils.decode("""{"a":1}""")
        }
    }
}
