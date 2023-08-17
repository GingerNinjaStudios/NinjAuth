package me.gingerninja.authenticator.core.common

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScopesModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideAppScope(
        @Dispatcher(DispatcherType.Default) dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope