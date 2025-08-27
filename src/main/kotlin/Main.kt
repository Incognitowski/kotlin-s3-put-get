package org.example

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.net.URI

fun main() {
    val s3Client = buildS3Client()

    val bucketName = "my-test-bucket"
    val fileName = "my-test-file.csv"
    val file = File(System.getProperty("java.io.tmpdir"), fileName)
    file.writer().use { writer ->
        writer.write("HEADER1,HEADER2\n")
        repeat(20) { num ->
            writer.write("value1_$num,value2_$num\n")
        }
    }

    val putRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build()
    s3Client.putObject(putRequest, RequestBody.fromFile(file))

    val getRequest = GetObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .build()
    val fileContent = s3Client.getObject(getRequest).use { str ->
        str.bufferedReader().use { it.readText() }
    }
    println(fileContent)
}

private fun buildS3Client(): S3Client = S3Client.builder()
    .endpointOverride(URI.create("http://localhost:4566"))
    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
    .region(Region.US_EAST_1)
    .serviceConfiguration(
        S3Configuration.builder().pathStyleAccessEnabled(true).chunkedEncodingEnabled(false).build()
    )
    .build()