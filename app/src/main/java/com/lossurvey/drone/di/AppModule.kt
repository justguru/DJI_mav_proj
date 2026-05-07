package com.lossurvey.drone.di

import android.content.Context
import com.lossurvey.drone.data.parser.SurveyFileParser
import com.lossurvey.drone.report.PdfReportGenerator
import com.lossurvey.drone.storage.MetadataWriter
import com.lossurvey.drone.storage.ProjectFolderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    @Provides @Singleton
    fun provideProjectFolderManager(@ApplicationContext ctx: Context): ProjectFolderManager =
        ProjectFolderManager(ctx)

    @Provides @Singleton
    fun provideMetadataWriter(json: Json): MetadataWriter = MetadataWriter(json)

    @Provides @Singleton
    fun providePdfGenerator(): PdfReportGenerator = PdfReportGenerator()

    @Provides @Singleton
    fun provideSurveyParser(): SurveyFileParser = SurveyFileParser()
}
