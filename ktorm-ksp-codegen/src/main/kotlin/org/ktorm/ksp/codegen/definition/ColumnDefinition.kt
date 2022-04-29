package org.ktorm.ksp.codegen.definition

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

/**
 * Column definitions, which contain all the information about the column, including column names,
 * converters, types, etc.
 */
public data class ColumnDefinition(

    /**
     * The column name，Corresponds to the [org.ktorm.ksp.api.Column.columnName] property, may be an empty string.
     */
    val columnName: String,

    /**
     * Specifies whether the column is the primary key, Corresponds to the [org.ktorm.ksp.api.PrimaryKey] annotation
     */
    val isPrimaryKey: Boolean,

    /**
     * Type name of the entity property
     */
    val propertyClassName: ClassName,

    /**
     * Name of the entity property
     */
    val entityPropertyName: MemberName,

    /**
     * Name of the table property
     */
    val tablePropertyName: MemberName,

    /**
     * The converter definition, Corresponds to the [org.ktorm.ksp.api.Column.converter] property
     */
    val converterDefinition: ConverterDefinition?,

    /**
     * Entity property declaration
     */
    val propertyDeclaration: KSPropertyDeclaration,

    /**
     * Entity property type
     */
    val propertyType: KSType,

    /**
     * The table definition
     */
    val tableDefinition: TableDefinition,

    /**
     * Is it a reference column，Corresponds to the [org.ktorm.ksp.api.Column.isReferences] property
     */
    val isReferences: Boolean,

    /**
     * Column definitions for referenced columns
     */
    var referencesColumn: ColumnDefinition?
) {

    /**
     * Is it a mutable property
     */
    val isMutable: Boolean = propertyDeclaration.isMutable

    /**
     * Is it a nullable property
     */
    val isNullable: Boolean = propertyType.nullability != Nullability.NOT_NULL

    /**
     * Is it an Enum type property
     */
    val isEnum: Boolean = (propertyType.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS

    override fun toString(): String {
        return "ColumnDefinition(columnName='$columnName', isPrimaryKey=$isPrimaryKey, propertyClassName=$propertyClassName, entityPropertyName=$entityPropertyName, tablePropertyName=$tablePropertyName, converterDefinition=$converterDefinition, referencesColumn=$referencesColumn)"
    }

}