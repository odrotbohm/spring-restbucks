package org.springsource.restbucks.myapplication

import com.fasterxml.jackson.databind.ObjectMapper
import com.googlecode.openbeans.Introspector
import com.jayway.jsonpath.*
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider

import org.aopalliance.intercept.MethodInterceptor

import org.springframework.aop.framework.ProxyFactory
import org.springframework.hateoas.Link
import org.springframework.util.ConcurrentReferenceHashMap
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass

data class HypermediaRemoteResource(val response : String) {

    private val mapper = ObjectMapper()
    private val configuration = Configuration.builder()
        .mappingProvider(JacksonMappingProvider(mapper))
        .build()
    private val factory = JsonPathProxyFactory.from(response, configuration)
    private val context = JsonPath.using(configuration).parse(response)
    private val linkMapTypeRef = object : TypeRef<Map<String, String>>() {}

    fun <T : Any> getPayloadAs(type : KClass<T>) : T = factory.getProxy(type)

    /**
     * Returns the Link with the given relation.
     */
    fun getLink(relation : String) : Link? {

        return try {

            Link(context.read("$._links.$relation", linkMapTypeRef)["href"], relation)

        } catch (e : PathNotFoundException) { null }
    }

    /**
     * Executes the given callback if a link with the given relation is present.
     */
    fun ifPresent(relation : String, callback : (Link) -> Unit) = getLink(relation)?.let(callback)


    private class JsonPathProxyFactory(val context: DocumentContext,
                                       val cache : ConcurrentMap<Method, JsonPath>) {

        companion object {

            private val cache : ConcurrentMap<Method, JsonPath> = ConcurrentReferenceHashMap()

            fun from(response : String, configuration : Configuration) : JsonPathProxyFactory {
                return JsonPathProxyFactory(JsonPath.parse(response, configuration), cache)
            }
        }

        fun <T : Any> getProxy(type : KClass<T>) : T {

            val methods = Introspector.getBeanInfo(type.java).propertyDescriptors

            val factory = ProxyFactory()
            factory.setInterfaces(type.java)
            factory.addAdvice( MethodInterceptor { invocation ->

                val method = invocation.method

                val type = object : TypeRef<T>() {
                    override fun getType() : Type = method.genericReturnType
                }

                val jsonPath = cache.getOrPut(method) {

                    methods.filter { it.readMethod == method }
                        .mapNotNull { JsonPath.compile("$.${it.name}") }
                        .first()
                }

                try { context.read(jsonPath, type) } catch (e : PathNotFoundException) { null }
            })

            return factory.getProxy(type.java.classLoader) as T
        }
    }
}