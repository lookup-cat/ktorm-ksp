/*
 * Copyright 2018-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ktorm.ksp.api

import net.sf.cglib.proxy.Enhancer
import net.sf.cglib.proxy.MethodInterceptor
import net.sf.cglib.proxy.MethodProxy
import org.ktorm.entity.Entity
import org.ktorm.entity.EntityExtensionsApi
import org.ktorm.schema.ColumnBinding
import sun.misc.Unsafe
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

public class CreateUndefinedException : Exception {
    public constructor() : super()
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(cause)
    public constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )
}

public object EntityUtil {

    @PublishedApi
    internal val undefinedValues: ConcurrentMap<Class<*>, Any> = ConcurrentHashMap()

    @PublishedApi
    internal val entityExtensionsApi: EntityExtensionsApi = EntityExtensionsApi()

    public inline fun <reified T> undefined(): T {
        return undefinedValues.computeIfAbsent(T::class.java) { cls: Class<*> ->
            if (cls.isInterface) {
                createJdkDynamicProxy(cls)
            } else if (Modifier.isAbstract(cls.modifiers)) {
                createCglibProxy(cls)
            } else {
                createUnsafeInstance(cls)
            }
        } as T
    }

    @PublishedApi
    internal fun createJdkDynamicProxy(cls: Class<*>): Any {
        val handler = object : InvocationHandler {
            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
                return when (method.name) {
                    "hashCode" -> this.hashCode()
                    "equals" -> proxy === args!!.first()
                    "toString" -> this.toString()
                    else -> throw NotImplementedError()
                }
            }
        }
        try {
            return Proxy.newProxyInstance(cls.classLoader, arrayOf(cls), handler)
        } catch (e: Exception) {
            throw CreateUndefinedException("Failed to create instance with jdk dynamic proxy", e)
        }
    }

    @PublishedApi
    internal fun createCglibProxy(cls: Class<*>): Any {
        try {
            val enhancer = Enhancer()
            enhancer.setSuperclass(cls)
            enhancer.classLoader = cls.classLoader
            enhancer.setCallback(object : MethodInterceptor {
                override fun intercept(
                    obj: Any?,
                    method: Method,
                    args: Array<out Any>?,
                    proxy: MethodProxy
                ): Any? {
                    if (method.name == "hashCode"
                        || method.name == "equals"
                        || method.name == "toString"
                    ) {
                        return proxy.invokeSuper(obj, args)
                    }
                    throw NotImplementedError()
                }
            })
            return enhancer.create()
        } catch (e: Exception) {
            throw CreateUndefinedException("Failed to create instance with cglib", e)
        }
    }

    @PublishedApi
    internal fun createUnsafeInstance(cls: Class<*>): Any {
        try {
            val field = Unsafe::class.java.getDeclaredField("theUnsafe")
            field.isAccessible = true
            return (field.get(null) as Unsafe).allocateInstance(cls)
        } catch (e: Exception) {
            throw CreateUndefinedException("Failed to create instance with Unsafe", e)
        }
    }

    public inline fun <reified T> getValueOrUndefined(entity: Entity<*>, columnBinding: ColumnBinding): T {
        with(entityExtensionsApi) {
            if (entity.hasColumnValue(columnBinding)) {
                return entity.getColumnValue(columnBinding) as T
            } else {
                return undefined()
            }
        }
    }

}
