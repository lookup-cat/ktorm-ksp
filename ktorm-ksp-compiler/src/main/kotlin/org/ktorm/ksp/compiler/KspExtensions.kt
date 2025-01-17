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

package org.ktorm.ksp.compiler

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeReference

public fun KSNode.ktormValidate(predicate: (KSNode?, KSNode) -> Boolean = { _, _ -> true }): Boolean {
    return this.accept(KtormValidateVisitor(predicate), null)
}

public fun KSClassDeclaration.findSuperTypeReference(name: String): KSTypeReference? {
    for (superType in this.superTypes) {
        val ksType = superType.resolve()
        val declaration = ksType.declaration
        if (declaration is KSClassDeclaration && declaration.qualifiedName!!.asString() == name) {
            return superType
        }
        if (declaration is KSClassDeclaration) {
            val result = declaration.findSuperTypeReference(name)
            if (result != null) {
                return result
            }
        }
    }
    return null
}
