package com.fourlastor.pickle

import cucumber.runtime.io.FileResourceLoader
import cucumber.runtime.model.CucumberFeature
import java.io.File

class FeatureParser {
    fun parse(featuresDirPath: String): List<CucumberFeature> {
        val featuresFileDir = File(featuresDirPath)
        if (!featuresFileDir.isDirectory) {
            throw FeatureFilesPathIsNotDirectoryException(featuresDirPath)
        }

        val featureFiles = featuresFileDir
                .walkTopDown()
                .filter { it.name.endsWith(".feature", ignoreCase = true) }
                .map { it.absolutePath }
                .toList()

        return CucumberFeature.load(FileResourceLoader(), featureFiles, emptyList())
    }
}

class FeatureFilesPathIsNotDirectoryException(path: String) : PickleException("$path is not a directory")
