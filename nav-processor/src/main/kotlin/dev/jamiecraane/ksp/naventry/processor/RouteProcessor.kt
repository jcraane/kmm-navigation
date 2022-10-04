package dev.jamiecraane.ksp.naventry.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import dev.jamiecraane.ksp.naventry.annotations.ArgType
import dev.jamiecraane.ksp.naventry.annotations.Argument
import dev.jamiecraane.ksp.naventry.annotations.Route
import java.io.OutputStream

class RouteProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("RouteProcessor was invoked4.")

        val routeSymbols = resolver.getSymbolsWithAnnotation(Route::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }


        logger.info("routeSymbols = ${routeSymbols.toList()}")

//        todo use Kotlin Poet lib to generate code
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
            val classConstructorArgs = argumentAnnotations.joinToString(",") { argument ->
                "private val ${argument.name}: ${getType(argument.type)}"
            }
            val argumentsInCompanionObject = argumentAnnotations.joinToString("\n") { argument ->
                "const val ${argument.name}: ${getType(argument.type)} = \"${argument.name}\""
            }
            val argumentReplacer = argumentAnnotations.joinToString("\n") { argument ->
                ".replace(\"{${argument.name}}\", ${argument.name})"
            }

            val packageName = classDeclaration.containingFile!!.packageName.asString()
            val className = "${classDeclaration.simpleName.asString()}NavEvent"
            val file = codeGenerator.createNewFile(Dependencies(true, classDeclaration.containingFile!!), packageName, className)
            file.appendText("package $packageName\n\n")
            file.appendText("data class $className($classConstructorArgs) : $superType() {\n")

            val resolveRouteFunction = """
                    #    fun route(): String {
                    #        return "$fullRouteName"
                    #            $argumentReplacer                             
                    #    }
                """.trimMargin("#")

            file.appendText(resolveRouteFunction)
            file.appendText("\n\n")

            val companionObject = """
                #    companion object {
                #        const val route = "$fullRouteName"                   
                #        $argumentsInCompanionObject
                #    }
                """.trimMargin("#")
            file.appendText(companionObject)
            file.appendText("\n}\n")

            file.close()
        }
    }
}

private fun getType(type: ArgType): String = when (type) {
    ArgType.STRING -> "String"
    ArgType.FLOAT -> "Float"
    ArgType.INT -> "Int"
    ArgType.LONG -> "Long"
}

class RouteProcessorProvider : SymbolProcessorProvider {
    override fun create(env: SymbolProcessorEnvironment): SymbolProcessor {
        return RouteProcessor(env.codeGenerator, env.logger)
    }
}

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}
