package com.upda.miniodemo;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @author luguosong
 */
@SpringBootTest
public class ObjectTest {

	@Resource
	private MinioClient minioClient;


	/*
	 * 判断文件是否存在
	 *
	 * 如果文件不存在，会抛出异常
	 * */
	@Test
	void isExist() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		StatObjectResponse response = minioClient.statObject(StatObjectArgs.builder().
				bucket("test-bucket").
				object("sample.txt").build());
		System.out.println(response);
	}

	/*
	 * 文件上传
	 * */
	@Test
	void putObject() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		File file = new File(getClass().getClassLoader().getResource("sample.txt").getFile());
		minioClient.putObject(PutObjectArgs.builder()
				.bucket("test-bucket")
				.object("sample.txt")
				.stream(new FileInputStream(file), file.length(), -1)
				.build());
	}

	/*
	 * 获取文件访问路径
	 * */
	@Test
	void getFilePath() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		//获取对象的预签名 URL 用于 HTTP 方法、到期时间和自定义请求参数。
		String url = minioClient.getPresignedObjectUrl(
				GetPresignedObjectUrlArgs.builder()
						.bucket("test-bucket") //桶
						.object("sample.txt") //文件名
						.expiry(3, TimeUnit.MINUTES) //设置链接有效时间为3分钟
						.method(Method.GET) // GET 方法
						.build());
		System.out.println(url);
	}

	/*下载文件
	 * */
	@Test
	void getFile() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		GetObjectResponse response = minioClient.getObject(
				GetObjectArgs
						.builder()
						.bucket("test-bucket")
						.object("sample.txt")
						.build());
		response.transferTo(new FileOutputStream("./sample_download.txt"));
	}

	/*
	 * 遍历文件
	 * */
	@Test
	void listObject() {
		Iterable<Result<Item>> listObjects = minioClient.listObjects(
				ListObjectsArgs.builder()
						.bucket("test-bucket")
						.build());

		listObjects.forEach(itemResult -> {
			Item item = null;
			try {
				item = itemResult.get();
				System.out.println(item.objectName());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	/*
	 * 删除文件
	 * */
	@Test
	void removeObject() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
		minioClient.removeObject(
				RemoveObjectArgs
						.builder()
						.bucket("test-bucket")
						.object("sample.txt")
						.build());
	}

}
