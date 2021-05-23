package com.yo1000.kamel.util

import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class KamelUtilsTests {
    @Test
    fun testGetHeaderFirst() {
        DefaultCamelContext().let {
            DefaultExchange(it).also {
                it.message.setHeader(
                    TestParam(
                        x = "AAA",
                        y = 111,
                        z = LocalDateTime.of(2020, 1, 1, 11, 11, 11)
                    )
                )
            }.message.getHeaderFirst(TestParam::class)?.let {
                Assertions.assertThat(it.x).isEqualTo("AAA")
                Assertions.assertThat(it.y).isEqualTo(111)
                Assertions.assertThat(it.z).isEqualTo(
                    LocalDateTime
                        .of(2020, 1, 1, 11, 11, 11)
                )
            } ?: Assertions.fail("getHeaderFirst is null")

            DefaultExchange(it).also {
                it.message.setHeader(
                    TestParam(
                        x = "BBB",
                        y = 222,
                        z = LocalDateTime.of(2020, 2, 2, 12, 12, 12)
                    )
                )
            }.message.getHeaderFirst(TestParam::class.java)?.let {
                Assertions.assertThat(it.x).isEqualTo("BBB")
                Assertions.assertThat(it.y).isEqualTo(222)
                Assertions.assertThat(it.z).isEqualTo(
                    LocalDateTime
                        .of(2020, 2, 2, 12, 12, 12)
                )
            } ?: Assertions.fail("getHeaderFirst is null")

            DefaultExchange(it).also {
                it.message.setHeader(
                    listOf(
                        TestParam(
                            x = "CCC",
                            y = 333,
                            z = LocalDateTime.of(2020, 3, 3, 13, 13, 13)
                        )
                    )
                )
            }.message.getHeaderFirst(object : TypeRef<List<TestParam>>() {})?.let {
                Assertions.assertThat(it[0].x).isEqualTo("CCC")
                Assertions.assertThat(it[0].y).isEqualTo(333)
                Assertions.assertThat(it[0].z).isEqualTo(
                    LocalDateTime
                        .of(2020, 3, 3, 13, 13, 13)
                )
            } ?: Assertions.fail("getHeaderFirst is null")

            DefaultExchange(it).also {
                it.message.setHeader(SubTestParam(
                    x = "Subclass Test",
                    y = 8888,
                    z = LocalDateTime.of(2025, 1, 1, 11, 11, 11),
                    a = 123.456
                ))
            }.message.getHeaderFirst(TestParam::class)?.let {
                Assertions.assertThat(it.x).isEqualTo("Subclass Test")
                Assertions.assertThat(it.y).isEqualTo(8888)
                Assertions.assertThat(it.z).isEqualTo(LocalDateTime.of(2025, 1, 1, 11, 11, 11))
            } ?: Assertions.fail("getHeaderFirst is null")

            DefaultExchange(it).also {
                it.message.setHeader(TestParam(
                    x = "Superclass Test",
                    y = 6666,
                    z = LocalDateTime.of(2025, 6, 6, 16, 16, 16)
                ))
            }.message.getHeaderFirst(SubTestParam::class).let {
                Assertions.assertThat(it).isNull()
            }

            DefaultExchange(it).also {
                it.message.setHeader(
                    listOf(
                        TestParam(
                            x = "DDD",
                            y = 444,
                            z = LocalDateTime.of(2020, 4, 4, 14, 14, 14)
                        ),
                        TestParam(
                            x = "EEE",
                            y = 555,
                            z = LocalDateTime.of(2020, 5, 5, 15, 15, 15)
                        )
                    )
                )
            }.message.getHeaderFirst(object : TypeRef<List<TestParam>>() {}).let {
                Assertions.assertThat(it).containsAll(
                    listOf(
                        TestParam(
                            x = "DDD",
                            y = 444,
                            z = LocalDateTime.of(2020, 4, 4, 14, 14, 14)
                        ),
                        TestParam(
                            x = "EEE",
                            y = 555,
                            z = LocalDateTime.of(2020, 5, 5, 15, 15, 15)
                        )
                    )
                )
            }

            DefaultExchange(it).also {
                it.message.getHeaders(TestParam::class).let {
                    Assertions.assertThat(it).isEmpty()
                }
            }

            DefaultExchange(it).also {
                it.message.getHeaders(object : TypeRef<Map<String, Any>>() {}).let {
                    Assertions.assertThat(it).isEmpty()
                }
            }

            DefaultExchange(it).also {
                it.message.getHeaderFirst(TestParam::class).let {
                    Assertions.assertThat(it).isNull()
                }
            }

            DefaultExchange(it).also {
                it.message.getHeaderFirst(object : TypeRef<Map<String, Any>>() {}).let {
                    Assertions.assertThat(it).isNull()
                }
            }
        }
    }

    @Test
    fun testGetHeaders() {
        DefaultCamelContext().let {
            DefaultExchange(it).also {
                it.message.setHeader(
                    TestParam(
                        x = "aaa",
                        y = 1111,
                        z = LocalDateTime.of(2021, 1, 1, 11, 11, 11)
                    )
                )
                it.message.setHeader(
                    TestParam(
                        x = "bbb",
                        y = 2222,
                        z = LocalDateTime.of(2021, 2, 2, 12, 12, 12)
                    )
                )
            }.message.getHeaders(TestParam::class).let {
                Assertions.assertThat(it).containsAll(
                    listOf(
                        TestParam(
                            x = "aaa",
                            y = 1111,
                            z = LocalDateTime.of(2021, 1, 1, 11, 11, 11)
                        ),
                        TestParam(
                            x = "bbb",
                            y = 2222,
                            z = LocalDateTime.of(2021, 2, 2, 12, 12, 12)
                        )
                    )
                )
            }

            DefaultExchange(it).also {
                DefaultExchange(it).also {
                    it.message.setHeader(
                        mapOf(
                            "x" to "XXX",
                            "y" to 123
                        )
                    )
                    it.message.setHeader(
                        mapOf(
                            "x" to "XXXX",
                            "y" to 1234
                        )
                    )
                }.message.getHeaders(object : TypeRef<Map<String, Any>>() {}).let {
                    Assertions.assertThat(it).containsAll(
                        listOf(
                            mapOf(
                                "x" to "XXX",
                                "y" to 123
                            ),
                            mapOf(
                                "x" to "XXXX",
                                "y" to 1234
                            )
                        )
                    )
                }
            }

            DefaultExchange(it).also {
                it.message.setHeader(100)
                it.message.setHeader(200)
                it.message.setHeader(300)
                it.message.setHeader(400)
            }.message.getHeaders(Int::class).let {
                Assertions.assertThat(it.sum()).isEqualTo(1000)
            }
        }
    }

    @Test
    fun testGetBody() {
        DefaultCamelContext().let {
            DefaultExchange(it).also {
                it.message.body = "Test string"
            }.message.getBody(String::class)?.let {
                Assertions.assertThat(it).isEqualTo("Test string")
            } ?: Assertions.fail("getBody is null")

            DefaultExchange(it).also {
                it.message.body = TestParam(
                    x = "xxx",
                    y = 999,
                    z = LocalDateTime.of(2022, 10, 10, 20, 20, 20)
                )
            }.message.getBody(TestParam::class)?.let {
                Assertions.assertThat(it.x).isEqualTo("xxx")
                Assertions.assertThat(it.y).isEqualTo(999)
                Assertions.assertThat(it.z).isEqualTo(
                    LocalDateTime
                        .of(2022, 10, 10, 20, 20, 20)
                )
            } ?: Assertions.fail("getBody is null")

            DefaultExchange(it).also {
                it.message.body = listOf(100, 200, 300)
            }.message.getBody(object : TypeRef<List<Int>>() {})?.let {
                Assertions.assertThat(it.sum()).isEqualTo(600)
            } ?: Assertions.fail("getBody is null")

            DefaultExchange(it).also {
                it.message.body = mapOf(
                    "abc" to "ABC",
                    "xyz" to 987
                )
            }.message.getBody(object : TypeRef<Map<String, Any>>() {})?.let {
                Assertions.assertThat(it["abc"]).isEqualTo("ABC")
                Assertions.assertThat(it["xyz"]).isEqualTo(987)
            } ?: Assertions.fail("getBody is null")

            DefaultExchange(it).message.getBody(TestParam::class).let {
                Assertions.assertThat(it).isNull()
            }

            DefaultExchange(it).message.getBody(object : TypeRef<Map<String, Any>>() {}).let {
                Assertions.assertThat(it).isNull()
            }

            DefaultExchange(it).also {
                it.message.body = mapOf(
                    "abc" to "ABC",
                    "xyz" to 987
                )
            }.message.let { message ->
                Assertions.assertThatThrownBy {
                    message.getBody(object : TypeRef<List<String>>() {})
                }.isInstanceOf(ClassCastException::class.java)
            }
        }
    }

    open class TestParam(
        val x: String,
        val y: Int,
        val z: LocalDateTime
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TestParam

            if (x != other.x) return false
            if (y != other.y) return false
            if (z != other.z) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y
            result = 31 * result + z.hashCode()
            return result
        }
    }

    class SubTestParam(
        x: String,
        y: Int,
        z: LocalDateTime,
        val a: Double
    ) : TestParam(x, y, z) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as SubTestParam

            if (a != other.a) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + a.hashCode()
            return result
        }
    }
}
