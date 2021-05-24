package com.yo1000.kamel.util

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class KamelUtils_ChoiceTests {
    @Test
    fun testChoice() {
        val context = DefaultCamelContext().also {
            it.addRoutes(ChoiceTestRouteBuilder())
            it.start()
        }

        val producerTemplate = context.createProducerTemplate()

        producerTemplate.send(
            "direct://${ChoiceTestRouteBuilder::class.simpleName}",
            DefaultExchange(context).also {
                it.message.body = 1
            }
        ).let {
            Assertions.assertThat(it.message.body).isEqualTo("A")
        }

        producerTemplate.send(
            "direct://${ChoiceTestRouteBuilder::class.simpleName}",
            DefaultExchange(context).also {
                it.message.body = 2
            }
        ).let {
            Assertions.assertThat(it.message.body).isEqualTo("B")
        }

        producerTemplate.send(
            "direct://${ChoiceTestRouteBuilder::class.simpleName}",
            DefaultExchange(context).also {
                it.message.body = 888
            }
        ).let {
            Assertions.assertThat(it.message.body).isEqualTo("Z")
        }
    }

    class ChoiceTestRouteBuilder : RouteBuilder() {
        override fun configure() {
            from("direct://${ChoiceTestRouteBuilder::class.simpleName}")
                .choiceFrom {
                    it.whenOn { it.message.body == 1 }
                        .process { it.message.body = "A" }

                    it.whenOn { it.message.body == 2 }
                        .process { it.message.body = "B" }

                    it.otherwise()
                        .process { it.message.body = "Z" }
                }
        }
    }
}
