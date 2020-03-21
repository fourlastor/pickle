package com.fourlastor.pickle

import cucumber.runtime.model.CucumberFeature
import cucumber.runtime.model.FeatureParser
import java.io.File

internal class FeatureParser {
    fun parse(featuresDirPath: String): List<CucumberFeature> {
        val featuresFileDir = File(featuresDirPath)
        if (!featuresFileDir.isDirectory) {
            throw FeatureFilesPathIsNotDirectoryException(featuresDirPath)
        }

        return featuresFileDir
                .walkTopDown()
                .filter { it.name.endsWith(".feature", ignoreCase = true) }
                .map(::FileResource)
                .map(FeatureParser::parseResource)
                .toList()
    }
}

class FeatureFilesPathIsNotDirectoryException(path: String) : PickleException("$path is not a directory")
