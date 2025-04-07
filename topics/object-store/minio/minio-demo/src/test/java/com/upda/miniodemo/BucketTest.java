package com.upda.miniodemo;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author luguosong
 */
@SpringBootTest
public class BucketTest {

	@Resource
	private MinioClient minioClient;

	/*
	 * 判断Bucket是否存在
	 * */
	@Test
	void isBucketExist() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-bucket").build());
		System.out.println(exists);
	}


	/*
	 * 创建Bucket
	 * */
	@Test
	void createBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("test-bucket").build());
		if (!exists) {
			// 创建Bucket
			minioClient.makeBucket(MakeBucketArgs.builder().bucket("test-bucket").build());

		} else {
			System.out.println("Bucket已存在");
		}
	}

	/*
	* 自定义Bucket策略
	* */
	@Test
	void setBucketPolicy() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		// 自定义Bucket策略
		String policyJson = "{\n" +
				"  \"Statement\": [\n" +
				"    {\n" +
				"      \"Action\": [\n" +
				"        \"s3:GetBucketLocation\",\n" +
				"        \"s3:ListBucket\"\n" +
				"      ],\n" +
				"      \"Effect\": \"Allow\",\n" +
				"      \"Principal\": \"*\",\n" +
				"      \"Resource\": \"arn:aws:s3:::test-bucket\"\n" +
				"    },\n" +
				"    {\n" +
				"      \"Action\": \"s3:GetObject\",\n" +
				"      \"Effect\": \"Allow\",\n" +
				"      \"Principal\": \"*\",\n" +
				"      \"Resource\": \"arn:aws:s3:::test-bucket/myobject*\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"Version\": \"2012-10-17\"\n" +
				"}";

		minioClient.setBucketPolicy(
				SetBucketPolicyArgs
						.builder()
						.bucket("test-bucket")
						.config(policyJson)
						.build());
	}

	/*
	 * 遍历Bucket
	 * */
	@Test
	void listBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		List<Bucket> list = minioClient.listBuckets();
		list.forEach(bucket -> System.out.println(bucket.name()));
	}

	/*
	 * 删除Bucket
	 * */
	@Test
	void removeBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		minioClient.removeBucket(RemoveBucketArgs.builder().bucket("test-bucket").build());
	}
}
