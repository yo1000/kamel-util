package com.yo1000.kamel.util

import org.apache.camel.Exchange
import org.apache.camel.Expression
import org.apache.camel.Message
import org.apache.camel.model.ChoiceDefinition
import org.apache.camel.model.ProcessorDefinition
import org.apache.camel.model.SplitDefinition
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

inline fun <reified T> Message.getHeaders(typeRef: TypeRef<T>): List<T> {
    return headers.filterValues {
        it is T
    }.values.toList() as List<T>
}

inline fun <reified T : Any> Message.getHeaderFirst(typeRef: TypeRef<T>): T? {
    return getHeaders(typeRef).firstOrNull()
}

fun <T : Any> Message.getHeaders(kclass: KClass<T>): List<T> {
    return headers.filterValues {
        it != null && it::class.isSubclassOf(kclass)
    }.values.toList() as List<T>
}

fun <T : Any> Message.getHeaders(jclass: Class<T>): List<T> {
    return getHeaders(jclass.kotlin)
}

fun <T : Any> Message.getHeaderFirst(kclass: KClass<T>): T? {
    return getHeaders(kclass).firstOrNull()
}

fun <T : Any> Message.getHeaderFirst(jclass: Class<T>): T? {
    return getHeaders(jclass).firstOrNull()
}

fun <T : Any> Message.setHeader(value: T) {
    // Refs to separator-value: https://datatracker.ietf.org/doc/html/rfc7230#section-3.2.6
    setHeader("${UUID.randomUUID()}|${value::class.qualifiedName}", value)
}

fun <T : Any> Message.getBody(kclass: KClass<T>): T? {
    return getBody(kclass.java)
}

inline fun <reified T> Message.getBody(typeRef: TypeRef<T>): T? {
    if (body is T?) {
        return body as T?
    } else {
        throw ClassCastException("Parameter type (${typeRef.type}) is mismatched body object (${body::class.qualifiedName})")
    }
}

fun ProcessorDefinition<*>.choice(definition: (ChoiceDefinition) -> Unit): ProcessorDefinition<*> {
    return choice().also {
        definition(it)
    }.endChoice()
}

fun ChoiceDefinition.whenOn(condition: (Exchange) -> Boolean): ChoiceDefinition {
    return `when` { condition(it) }
}

fun ProcessorDefinition<*>.split(
    expression: Expression,
    definition: (SplitDefinition) -> Unit,
    aggregation: (prevExchange: Exchange?, currentExchange: Exchange) -> Exchange
): ProcessorDefinition<*> {
    return split(expression) { oldExchange, newExchange ->
        aggregation(oldExchange, newExchange)
    }.also {
        definition(it)
    }.end()
}

fun ProcessorDefinition<*>.split(
    expression: Expression,
    definition: (SplitDefinition) -> Unit
): ProcessorDefinition<*> {
    return split(expression).also {
        definition(it)
    }.end()
}

abstract class TypeRef<T> {
    val type: Type = javaClass.genericSuperclass.let {
        if (it is ParameterizedType)
            it.actualTypeArguments[0]
        else throw IllegalStateException("Inheritance depth is too deep")
    }
}
