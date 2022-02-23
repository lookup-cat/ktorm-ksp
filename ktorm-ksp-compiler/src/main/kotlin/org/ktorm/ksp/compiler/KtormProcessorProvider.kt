@file:OptIn(ExperimentalStdlibApi::class, KotlinPoetKspPreview::class)

package org.ktorm.ksp.compiler

import Id
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import org.ktorm.ksp.annotation.Column
import org.ktorm.ksp.annotation.Table

public class KtormProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.info("create ktorm symbolProcessor")
        return KtormProcessor(environment.codeGenerator, environment.logger)
    }
}

public class KtormProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("start ktorm processor")
        val symbols = resolver.getSymbolsWithAnnotation(Table::class.qualifiedName!!)
        logger.info("symbols:${symbols.toList()}")
        val tableDefinitions = mutableListOf<TableDefinition>()
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(EntityVisitor(tableDefinitions), Unit) }
        KtormCodeGenerator().generate(tableDefinitions, codeGenerator, logger)
        return ret
    }


    public inner class EntityVisitor(
        private val tableDefinitions: MutableList<TableDefinition>,
    ) : KSVisitorVoid() {

        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {

            val entityClassName = classDeclaration.toClassName()
            val table = classDeclaration.getAnnotationsByType(Table::class).first()
            val tableClassName = ClassName(entityClassName.packageName, entityClassName.simpleName + "s")

            val tableName = table.tableClassName.ifEmpty { entityClassName.simpleName }

            val columnDefs = classDeclaration
                .getAllProperties()
                .mapNotNull {
                    val propertyKSType = it.type.resolve()
                    if (it.isAnnotationPresent(Transient::class)) {
                        return@mapNotNull null
                    }
                    val columnAnnotation = it.getAnnotationsByType(Column::class).firstOrNull()
                    val isId = it.getAnnotationsByType(Id::class).any()
                    val columnName = columnAnnotation?.columnName ?: it.simpleName.asString()
                    ColumnDefinition(
                        columnName,
                        isId,
                        it,
                        propertyKSType.toTypeName(),
                        MemberName(tableClassName, it.simpleName.asString()),
                    )
                }
                .toList()

            val tableDef = TableDefinition(
                tableName,
                tableClassName,
                table.alias,
                table.catalog,
                table.schema,
                entityClassName,
                columnDefs,
                classDeclaration.containingFile!!,
                classDeclaration
            )
            tableDefinitions.add(tableDef)
        }

    }
}