package dev.jamiecraane.ksp.naventry.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import dev.jamiecraane.ksp.naventry.annotations.ArgType
import dev.jamiecraane.ksp.naventry.annotations.Argument
import dev.jamiecraane.ksp.naventry.annotations.Route
import java.io.OutputStream

class RouteAnnotationProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("RouteProcessor was invoked.")

        val routeSymbols = resolver.getSymbolsWithAnnotation(Route::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }

        routeSymbols
            .forEach { it.accept(BuilderVisitor(), Unit) }

        return emptyList()
    }

    inner class BuilderVisitor(val baseClass: String? = null) : KSVisitorVoid() {
        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            logger.info("visitClassDeclaration")

            val routeAnnotation = classDeclaration.getAnnotationsByType(Route::class)
            val argumentAnnotations = classDeclaration.getAnnotationsByType(Argument::class)

            val superType = routeAnnotation.first().baseClassQualifiedName
            val fullRouteName = routeAnnotation.first().path

            val packageName = classDeclaration.containingFile!!.packageName.asString()
            val className = "${classDeclaration.simpleName.asString()}NavEvent"

            val primaryConstructor = FunSpec.constructorBuilder().apply {
                argumentAnnotations.forEach { argument ->
                    addParameter(argument.name, getTypePoet(argument.type))
                }
            }.build()

            val routeFunction = createRouteResolveFunction(fullRouteName, argumentAnnotations)
            val companionObject = createCompanionWithFullRouteAndArguments(fullRouteName, argumentAnnotations)

            val outputFile = FileSpec.builder(packageName = packageName, fileName = className)
                .addType(
                    TypeSpec.classBuilder(className)
                        .superclass(ClassName("", superType))
                        .primaryConstructor(primaryConstructor)
                        .apply {
                            argumentAnnotations.forEach { argument ->
                                addProperty(
                                    PropertySpec.builder(argument.name, getTypePoet(argument.type)).initializer(argument.name).build()
                                )
                            }
                        }
                        .addFunction(routeFunction)
                        .addType(companionObject)
                        .build()
                )
                .build()

            outputFile.writeTo(codeGenerator, Dependencies(aggregating = false))
        }

        private fun createRouteResolveFunction(fullRouteName: String, argumentAnnotations: Sequence<Argument>): FunSpec {
            val replaceStatements = buildString {
                append("return \"$fullRouteName\"\n")
                argumentAnnotations.forEach { argument ->
                    append(".replace(\"{${argument.name}}\", ${argument.name})\n")
                }
            }
            return FunSpec.builder("route")
                .returns(String::class)
                .addCode(replaceStatements)
                .build()
        }

        private fun createCompanionWithFullRouteAndArguments(fullRouteName: String, argumentAnnotations: Sequence<Argument>): TypeSpec {
            val argumentNamesAndTypes = PropertySpec.builder(
                "arguments", List::class.asClassName().parameterizedBy(
                    Pair::class.asClassName().parameterizedBy(
                        String::class.asTypeName(), ArgType::class.asTypeName()
                    )
                )
            ).initializer(buildString {
                append("listOf(")
                append(
                    argumentAnnotations.map { argument ->
                        "Pair(\"${argument.name}\",ArgType.${argument.type})"
                    }.joinToString(",")
                )
                append(")")
            }).build()

            //            val arguments = listOf(raceId to ArgType.STRING)
            return TypeSpec.companionObjectBuilder()
                .addProperty(
                    PropertySpec.builder("route", String::class)
                        .initializer("%S", fullRouteName)
                        .build()
                )
                .apply {
                    argumentAnnotations.forEach { argument ->
                        addProperty(
                            PropertySpec.builder(argument.name, getTypePoet(argument.type))
                                .initializer("%S", argument.name)
                                .build()
                        )
                    }
                }
                .addProperty(argumentNamesAndTypes)
                .build()
        }
    }
}

private fun getTypePoet(type: ArgType) = when (type) {
    ArgType.STRING -> String::class
    ArgType.FLOAT -> Float::class
    ArgType.INT -> Int::class
    ArgType.LONG -> Long::class
}

class RouteAnnotationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RouteAnnotationProcessor(environment.codeGenerator, environment.logger)
    }
}

private fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}
