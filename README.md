# :camel: kamel-util

Apache Camel utilities for Kotlin.

## Dependencies

```xml
<dependency>
    <groupId>com.yo1000</groupId>
    <artifactId>kamel-util</artifactId>
    <version>1.1.1</version>
</dependency>
```

## Examples

Details refer to `src/test/kotlin/com/yo1000/kamel/util/KamelUtilsTests.kt`

### for Kotlin

#### choice

```kotlin
from("direct://choiceExample")
    .process {
        it.message.body = 2
    }
    .choiceFrom {
        it.whenOn { it.message.body == 1 }
            .process { it.message.body = "A" }

        it.whenOn { it.message.body == 2 }
            .process { it.message.body = "B" }

        it.otherwise()
            .process { it.message.body = "Z" }
    }
    .process {
        println(it.message.body) // B
    }
```

#### split, aggregate

```kotlin
from("direct://splitExample")
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
    }, { accExchange, exchange ->
        if (accExchange == null) {
            exchange
        } else {
            val acc = accExchange.message.getBody(Int::class)!!
            val value = exchange.message.getBody(Int::class)!!
            accExchange.message.body = acc + value
            accExchange
        }
    })
    .process {
        println(it.message.body) // 90
    }
```

#### getBody

```kotlin
val stringExchange = DefaultExchange(context).also {
    it.message.body = "Hello, world!"
}
println(stringExchange.message.getBody(String::class)) // Hello, world!

val listExchange = DefaultExchange(context).also {
    it.message.body = listOf(100, 200, 300)
}
listExchange.message.getBody(object : TypeRef<List<Int>>() {}).let {
    println(it.sum()) // 600
}
```

#### getHeaderFirst, getHeaders

```kotlin
val stringExchange = DefaultExchange(context).also {
    it.message.setHeader("Hello, world!")
}
println(stringExchange.message.getHeaderFirst(String::class)) // Hello, world!

val listExchange = DefaultExchange(context).also {
    it.message.setHeader(listOf(100, 200, 300))
}
listExchange.message.getHeaderFirst(object : TypeRef<List<Int>>() {}).let {
    println(it.sum()) // 600
}

val integersExchange = DefaultExchange(context).also {
    it.message.setHeader(100)
    it.message.setHeader(200)
    it.message.setHeader(300)
    it.message.setHeader(400)
}
integersExchange.message.getHeaders(Int::class).let {
    println(it.sum()) // 1000
}
```
