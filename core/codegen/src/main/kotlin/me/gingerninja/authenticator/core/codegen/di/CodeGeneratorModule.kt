package me.gingerninja.authenticator.core.codegen.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.gingerninja.authenticator.core.codegen.CodeGenerator
import me.gingerninja.authenticator.core.codegen.DefaultTimeProvider
import me.gingerninja.authenticator.core.codegen.OtpGenerator
import me.gingerninja.authenticator.core.codegen.TimeProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CodeGeneratorModule {
    @Singleton
    @Provides
    fun provideTimeProvider(): TimeProvider = DefaultTimeProvider


    @Singleton
    @Provides
    fun provideCodeGenerator(timeProvider: TimeProvider): CodeGenerator = OtpGenerator(timeProvider)
}