package com.lossurvey.drone.data.parser

import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SurveyType

sealed class ParserResult {
    data class Success(
        val surveyType: SurveyType,
        val sites: List<Site>,
        val validationErrors: List<String>,
        val fileName: String
    ) : ParserResult()

    data class Error(val message: String) : ParserResult()
}
