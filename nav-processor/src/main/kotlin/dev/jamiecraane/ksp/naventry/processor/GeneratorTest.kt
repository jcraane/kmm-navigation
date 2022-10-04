package dev.jamiecraane.ksp.naventry.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.jamiecraane.ksp.naventry.annotations.ArgType
import dev.jamiecraane.ksp.naventry.annotations.Argument

fun main(args: Array<String>) {
    val file = FileSpec.builder(packageName = "", fileName = "Test")
        .addType(createCompanionWithFullRouteAndArguments("route"))
        .build()

    file.writeTo(System.out)
}

private fun createCompanionWithFullRouteAndArguments(fullRouteName: String): TypeSpec {
    //            val arguments = listOf(raceId to ArgType.STRING)
    return TypeSpec.companionObjectBuilder()
        .addProperty(
            PropertySpec.builder("route", String::class)
                .initializer("%S", fullRouteName)
                .build()
        )
        /*.apply {
            argumentAnnotations.forEach { argument ->
                addProperty(
                    PropertySpec.builder(argument.name, getTypePoet(argument.type))
                        .initializer("%S", argument.name)
                        .build()
                )
            }
        }*/
        .addProperty(
            PropertySpec.builder(
                "arguments", List::class.asClassName().parameterizedBy(
                    Pair::class.asClassName().parameterizedBy(
                        String::class.asTypeName(), ArgType::class.asTypeName()
                    )
                )
            ).initializer(buildString {
                append("listOf(")

                append(")")
            })
                .build()
        )
        .build()
}
