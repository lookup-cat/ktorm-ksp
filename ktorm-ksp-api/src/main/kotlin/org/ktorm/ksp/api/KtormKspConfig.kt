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

import org.ktorm.schema.BaseTable
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * Global ktorm-ksp configuration, you can add this annotation to any class, but only allow this
 * annotation to be added once.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class KtormKspConfig(

    /**
     * Allow entity creation using reflection in [BaseTable.doCreateEntity] (this property only affects tables
     * generated by class entity) By creating an entity class through reflection, the default value parameters in
     * the entity construction can take effect. When an entity construction parameter has a default value, and the
     * query result [ResultSet] does not have the parameter value or the value is null, The entity is instantiated
     * with parameter default values. This behavior has a slight performance penalty (although it can be ignored
     * in most cases), setting this property to false will not use reflection to create entities, This will result
     * in not being able to create instances with default value parameters
     */
    val allowReflectionCreateClassEntity: Boolean = true,

    /**
     * The global enum converter, the value can only be the implementation type of Nothing::class or singleton
     * [EnumConverter], the default conversion rules can be viewed in [Converter].
     */
    val enumConverter: KClass<out EnumConverter> = Nothing::class,

    /**
     * The global single type converter, the value can only be the implementation type of Nothing::class or
     * singleton [SingleTypeConverter], the default conversion rules can be viewed in [Converter].
     */
    val singleTypeConverters: Array<KClass<out SingleTypeConverter<*>>> = [],

    /**
     * Global naming strategy, the value can only be Nothing::class or the implementation type of [NamingStrategy]
     * of a singleton. By default, the table name is the entity class name, and the column name is the entity
     * class attribute name.
     */
    val namingStrategy: KClass<out NamingStrategy> = Nothing::class,

    /**
     * Default extension code generate configuration.
     */
    val extension: ExtensionGenerator = ExtensionGenerator()
)

/**
 * Default extension build configuration.
 */
@Retention(AnnotationRetention.SOURCE)
public annotation class ExtensionGenerator(

    /**
     * Generate EntitySequence Extension Property.
     * ```kotlin
     * val Database.employees: EntitySequence<Employee,Employees>
     *     get() = this.sequenceOf(Employees)
     * ```
     */
    val enableSequenceOf: Boolean = true,

    /**
     * Generate EntitySequence add Extension Function. Generated only for class entity.
     * ```kotlin
     * fun EntitySequence<Employee,Employees>.add(employee)
     * ```
     */
    val enableClassEntitySequenceAddFun: Boolean = true,

    /**
     * Generate EntitySequence update Extension Function. Generated only for class entity.
     * ```kotlin
     * fun EntitySequence<Employee,Employees>.update(employee)
     * ```
     */
    val enableClassEntitySequenceUpdateFun: Boolean = true,

    /**
     * Generate Constructor Function, Components Function, Copy Function. Generated only for interface entity.
     *
     * Constructor Function:
     * ```kotlin
     * public fun Employee(
     *      id: Int? = undefined(),
     *      name: String = undefined(),
     *      job: String = undefined(),
     * ): Employee
     * ```
     * Components Function:
     * ```kotlin
     * public operator fun Employee.component1(): Int? = this.id
     * public operator fun Employee.component2(): String = this.name
     * public operator fun Employee.component3(): String = this.job
     * ```
     *
     * Copy Function:
     * ```kotlin
     * public fun Employee.copy(
     *      id: Int? = getValueOrUndefined(this, Employees.id.binding!!),
     *      name: String = getValueOrUndefined(this, Employees.name.binding!!),
     *      job: String = getValueOrUndefined(this, Employees.job.binding!!)
     * ): Employee
     * ```
     *
     * It should be noted here that if no property is assigned when creating an entity instance in ktorm, different SQL
     * statements will be generated.
     * ```kotlin
     * val employee1 = Entity.create<Employee>()
     * employee1.id = null
     * database.employees.add(employee1)
     * // SQL: insert into employee (id) values (null)
     *
     * val employee2 = Entity.create<Employee>()
     * employee2.id = null
     * employee2.name = null
     * database.employees.add(employee2)
     * // SQL: insert into employee (id, name) values (null, null)
     * ```
     *
     * There is essentially a difference between assigning null and not assigning an entity property.
     * The constructor function and copy function generated by ksp have similar effects
     * ```kotlin
     * val employee = Employee(id = null)
     * // The actual effect is equivalent to
     * val employee = Entity.create<Employee>()
     * employee.id = null
     * // will not assign name property: employee.name = null
     * ```
     * When calling the function, the created entity instance will not assign the corresponding properties
     * to the parameters that are not passed.
     * In order to achieve this, the default value in the constructor and copy function may generate JDK dynamic
     * proxy object, proxy object generated by CGLIB, object created by Unsafe（It depends on what the specific
     * type is）This generated instance is unique and will not conflict with the parameters passed when calling
     * （Unless you also call the undefined function to get the instance）Therefore, it can help us determine which
     * parameters have passed values and which parameters have not passed values when calling the method.
     *
     * A limitation of this implementation is that the parameter type cannot be a non-null primitive type.
     * This is because the non-null primitive type in kotlin will be automatically unboxed, which will cause our
     * above implementation to fail, and there is no way to tell which parameter values were passed when calling.
     *
     * So in the generated Constructor Function, Copy Function，If the property is a non-null primitive type,
     * it is automatically converted to a nullable type. And in the process of actually creating the instance，
     * Will judge whether the parameter value is null, if it is null, an exception will be thrown
     */
    val enableInterfaceEntitySimulationDataClass: Boolean = true,
)
