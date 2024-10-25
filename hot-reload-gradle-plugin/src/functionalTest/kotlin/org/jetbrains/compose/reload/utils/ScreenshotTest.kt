package org.jetbrains.compose.reload.utils

import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import java.util.stream.Stream
import kotlin.coroutines.Continuation

@TestTemplate
@ExtendWith(ScreenshotTestInvocationContextProvider::class)
@DefaultSettingsGradleKts
annotation class ScreenshotTest

class ScreenshotTestInvocationContextProvider : TestTemplateInvocationContextProvider {
    private val hotReloadInvocationContextProvider = HotReloadTestInvocationContextProvider()

    override fun supportsTestTemplate(context: ExtensionContext): Boolean {
        return hotReloadInvocationContextProvider.supportsTestTemplate(context)
    }

    override fun provideTestTemplateInvocationContexts(context: ExtensionContext): Stream<TestTemplateInvocationContext?>? {
        return hotReloadInvocationContextProvider.provideTestTemplateInvocationContexts(context)
            .flatMap { parent ->
                ProjectMode.entries.stream().map { mode ->
                    ScreenshotTestInvocationContext(parent, mode)
                }
            }
    }
}

private class ScreenshotTestInvocationContext(
    private val parentContext: TestTemplateInvocationContext,
    private val mode: ProjectMode
) : TestTemplateInvocationContext {
    override fun getDisplayName(invocationIndex: Int): String {
        return parentContext.getDisplayName(invocationIndex) + " [$mode]"
    }

    override fun getAdditionalExtensions(): List<Extension> {
        return parentContext.additionalExtensions + ScreenshotTestFixtureProvider(mode)
    }
}

private class ScreenshotTestFixtureProvider(
    private val mode: ProjectMode
) : BeforeTestExecutionCallback, ParameterResolver {

    override fun beforeTestExecution(context: ExtensionContext) {
        val fixture = context.getHotReloadTestFixtureOrThrow()
        when (mode) {
            ProjectMode.Kmp -> fixture.setupKmpProject()
            ProjectMode.Jvm -> fixture.setupJvmProject()
        }
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Boolean {
        return parameterContext.parameter.type == ScreenshotTestFixture::class.java ||
                parameterContext.parameter.type == Continuation::class.java
    }

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext
    ): Any? {
        return ScreenshotTestFixture(mode, extensionContext.getHotReloadTestFixtureOrThrow())
    }
}
