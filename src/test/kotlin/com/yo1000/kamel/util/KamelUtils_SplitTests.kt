package com.yo1000.kamel.util

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class KamelUtils_SplitTests {
    @Test
    fun testSplitAndAggregate() {
        val context = DefaultCamelContext().also {
            it.addRoutes(SplitAndAggregateTestRouteBuilder())
            it.start()
        }

        val producerTemplate = context.createProducerTemplate()

        producerTemplate.send(
            "direct://${SplitAndAggregateTestRouteBuilder::class.simpleName}",
            DefaultExchange(context).also {
                it.message.body = (1..9).toList()
            }
        ).let {
            Assertions.assertThat(it.message.body)
                .isEqualTo("10th, 20th, 30th, 40th, 50th, 60th, 70th, 80th, 90th")
        }
    }

    class SplitAndAggregateTestRouteBuilder : RouteBuilder() {
        override fun configure() {
            from("direct://${SplitAndAggregateTestRouteBuilder::class.simpleName}")
                .split(body(), {
                    it.process {
                        it.message.body = it.message.getBody(Int::class)?.let { it * 10 }
                    }

                    it.process {
                        it.message.body = "${it.message.getBody(Int::class)}th"
                    }
                }, { prevExchange, currentExchange ->
                    if (prevExchange == null) {
                        currentExchange
                    } else {
                        prevExchange.message.body = "${
                            prevExchange.message.body
                        }, ${
                            currentExchange.message.body
                        }"
                        prevExchange
                    }
                })
        }
    }

    @Test
    fun testSplitAndAggregateSum() {
        val context = DefaultCamelContext().also {
            it.addRoutes(SplitAndAggregateSumTestRouteBuilder())
            it.start()
        }

        val producerTemplate = context.createProducerTemplate()

        producerTemplate.send(
            "direct://${SplitAndAggregateSumTestRouteBuilder::class.simpleName}",
            DefaultExchange(context).also {
                it.message.body = (1..3).toList()
            }
        ).let {
            Assertions.assertThat(it.message.body).isEqualTo(90)
        }
    }

    class SplitAndAggregateSumTestRouteBuilder : RouteBuilder() {
        override fun configure() {
            from("direct://${SplitAndAggregateSumTestRouteBuilder::class.simpleName}")
                .process {
                    it.message.body = (1..3)
                }
                .splitTo(body(), {
                    it.process {
                        it.message.body = it.message.getBody(Int::class)?.let { it + 1 }
                    }

                    it.process {
                        it.message.body = it.message.getBody(Int::class)?.let { it * 10 }
                    }
                }, { prevExchange, currentExchange ->
                    if (prevExchange == null) {
                        currentExchange
                    } else {
                        val prev = prevExchange.message.getBody(Int::class)!!
                        val curr = currentExchange.message.getBody(Int::class)!!
                        prevExchange.message.body = prev + curr
                        prevExchange
                    }
                })
                .process {
                    println(it.message.body)
                }
        }
    }

    @Test
    fun testSplit() {
        val syncList: MutableList<String> = Collections.synchronizedList(mutableListOf())

        val context = DefaultCamelContext().also {
            it.addRoutes(SplitTestRouteBuilder(syncList))
            it.start()
        }

        val producerTemplate = context.createProducerTemplate()

        producerTemplate.send(
            "direct://${SplitTestRouteBuilder::class.simpleName}",
            DefaultExchange(context).also {
                it.message.body = (3..7).toList()
            }
        ).let {
            Assertions.assertThat(syncList).containsAll(listOf(
                "30th", "40th", "50th", "60th", "70th"
            ))
        }
    }

    class SplitTestRouteBuilder(
        private val mutList: MutableList<String>
    ) : RouteBuilder() {
        override fun configure() {
            from("direct://${SplitTestRouteBuilder::class.simpleName}")
                .splitTo(body()) {
                    it.process {
                        it.message.body = it.message.getBody(Int::class)?.let { it * 10 }
                    }

                    it.process {
                        it.message.body = "${it.message.getBody(Int::class)}th"
                    }

                    it.process {
                        mutList += it.message.body as String
                    }
                }
        }
    }
}
