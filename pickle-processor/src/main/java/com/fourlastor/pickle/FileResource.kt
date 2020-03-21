package com.fourlastor.pickle

import cucumber.runtime.io.Resource
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI

internal class FileResource(private val file: File) : Resource {
    override fun getPath(): URI = file.toURI()

    @Throws(IOException::class)
    override fun getInputStream(): InputStream = FileInputStream(file)

}
