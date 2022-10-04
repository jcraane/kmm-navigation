package dev.jamiecraane.ksp.naventry.processor

import com.squareup.kotlinpoet.*
import java.io.StringWriter

fun main(args: Array<String>) {
    val routeFunction = FunSpec.builder("route")
        .returns(String::class)
        .addCode(
            """
            |return "race/{seasonId}/{raceId}"
            |    .replace("{seasonId}", seasonId)
            |    .replace("{raceId}", raceId)
        """.trimMargin()
        )
        .build()

    val companionObject = TypeSpec.companionObjectBuilder()
        .addProperty(
            PropertySpec.builder("route", String::class)
                .initializer("%S", "race/{seasonId}/{raceId}")
                .build()
        )
        .addProperty(
            PropertySpec.builder("seasonId", String::class)
                .initializer("%S", "seasonId")
                .build()
        )
        .addProperty(
            PropertySpec.builder("raceId", String::class)
                .initializer("%S", "raceID")
                .build()
        )
        .build()

    val myFile = FileSpec.builder("", "TestClass")
        .addType(
            TypeSpec.classBuilder("TestClass")
                .superclass(ClassName("", "NavigationEvent"))
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("seasonId", String::class)
                        .addParameter("raceId", String::class)
                        .build()
                )
                .addProperty(PropertySpec.builder("seasonId", String::class).initializer("seasonId").build())
                .addProperty(PropertySpec.builder("raceId", String::class).initializer("raceId").build())
                .addFunction(routeFunction)
                .addType(companionObject)
                .build()
        )
        .build()

//    myFile.writeTo(System.out)
    val writer = StringWriter()
    myFile.writeTo(writer)
    println(writer)
}
