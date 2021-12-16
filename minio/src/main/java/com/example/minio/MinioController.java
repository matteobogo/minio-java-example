package com.example.minio;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.UploadObjectArgs;
import io.minio.messages.Item;

@RestController
public class MinioController {

    @Value("${MINIO_ENDPOINT:http://localhost:9000}")
    private String endpoint;

    @Value("${MINIO_ACCESSKEY:test123456}")
    private String accessKey;

    @Value("${MINIO_SECRETKEY:test123456}")
    private String secretKey;

    @Value("${MINIO_BUCKET:test}")
    private String bucketName;

    @GetMapping("/work")
    public void doWork() {
        String filename = "myfile.txt";
		String path = "/tmp/";
		String fullPath = path + filename;

		File sourceFile = new File(fullPath);
		File destFile = new File(fullPath + "copy-" + filename);

		try {

			// create random file of 129kb size
			try (FileOutputStream fos = new FileOutputStream(sourceFile);) {
				try (BufferedOutputStream bos = new BufferedOutputStream(fos);) {
					int filesize = 129 * 1024;
					byte[] bytes = new byte[filesize];
					Random rand = new Random();
					rand.nextBytes(bytes);
					bos.write(bytes);
					bos.flush();
				}
				fos.flush();
			}

			// client
			MinioClient minioClient =
				MinioClient.builder()
					.endpoint(endpoint)
					.credentials(accessKey, secretKey)
					.build();

			//bucket
			boolean found = minioClient.bucketExists(
				BucketExistsArgs.builder()
					.bucket(bucketName)
					.build());
			if (!found) {
				//create bucket
				minioClient.makeBucket(
					MakeBucketArgs.builder()
					.bucket(bucketName)
					.build());
			}

			//upload
			minioClient.uploadObject(
				UploadObjectArgs.builder()
					.bucket(bucketName)
					.object(filename)
					.filename(fullPath)
					.build());

			// browse objects (recursively)
			Iterable<Result<Item>> results =
			minioClient.listObjects(
				ListObjectsArgs.builder()
					.bucket(bucketName)
					.recursive(true)
					.build());

			for (Result<Item> result : results) {
				Item item = result.get();
				System.out.println(item.lastModified() + "\t" + item.size() + "\t" + item.objectName());
			}

			//get obj
			// InputStream is = minioClient.getObject(
			// 	GetObjectArgs.builder()
			// 		.bucket(bucketName)
			// 		.object(filename)
			// 		.build());

			// Files.copy(is, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			// is.close();
			
			// // delete obj
			// minioClient.removeObject(
          	// 	RemoveObjectArgs.builder()
			// 	  	.bucket(bucketName)
			// 		.object(filename)
			// 		.build());

			// // delete bucket 
			// minioClient.removeBucket(
			// 	RemoveBucketArgs.builder()
			// 		.bucket(bucketName)
			// 		.build());
			
		} catch (Exception e) { 
			System.out.println(e);
		} finally {
			sourceFile.delete();
			destFile.delete();
		}
	}
}