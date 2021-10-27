package br.com.seven.training.corda

import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.springframework.core.io.ByteArrayResource
import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class Attachment : Closeable {

    companion object {
        private val TMP_FILE_PREFIX = "wp7-uploaded-attachment-"
        private val TMP_FILE_SUFFIX = "-tmp.zip"
        private val ACCEPTED_TYPES = arrayOf("application/zip", "application/java-archive")
        fun unwrapAttachment(zipAttachment: InputStream): ByteArrayResource {
            val tempDir = System.getProperty("java.io.tmpdir")
            ZipInputStream(zipAttachment).use { zip ->
                val entry: ZipEntry = zip.nextEntry
                var size: Int
                val buffer = ByteArray(2048)

                FileOutputStream("$tempDir/${entry.name}").use {
                    ByteArrayOutputStream(buffer.size).use {
                        size = zip.read(buffer, 0, buffer.size)
                        while (size > 0) {
                            it.write(buffer, 0, size)
                            size = zip.read(buffer, 0, buffer.size)
                        }
                        val resource = ByteArrayResource(it.toByteArray())
                        File("$tempDir/${entry.name}").delete()
                        return resource
                    }
                }
            }
        }
    }

    val tmpFile: File?
    val inputStream: InputStream
    val filename: String?
    val original: Boolean
    val size: Long?
    val contentType: String?

    constructor(multipartFile: MultipartFile) {
        if (ACCEPTED_TYPES.contains(multipartFile.contentType)) {
            this.tmpFile = null
            this.inputStream = multipartFile.inputStream
            this.filename = multipartFile.originalFilename
            this.original = true
            this.contentType = multipartFile.contentType
            this.size = multipartFile.size
        } else {
            val archive = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX)
            ArchiveStreamFactory()
                    .createArchiveOutputStream(ArchiveStreamFactory.ZIP, archive.outputStream())
                    .use { zipOs ->
                        zipOs.putArchiveEntry(ZipArchiveEntry(multipartFile.originalFilename))
                        multipartFile.inputStream.use { it.copyTo(zipOs) }
                        zipOs.closeArchiveEntry()
                        zipOs.finish()
                    }
            this.tmpFile = archive
            this.inputStream = archive.inputStream()
            this.filename = multipartFile.originalFilename
            this.original = false
            this.contentType = multipartFile.contentType
            this.size = multipartFile.size
        }
    }

    override fun close() {
        if (tmpFile != null && tmpFile.exists()) tmpFile.delete()
    }
}