package com.luguosong.controller;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

/**
 * 文件上传
 *
 * @author luguosong
 */
@Controller
@RequestMapping("/fileUpload")
public class FileUploadController {

	@RequestMapping("/springMvc")
	public String springMvc(
			@RequestParam("fileName") MultipartFile file,
			@RequestParam("username") String username,
			HttpServletRequest request) throws IOException {
		System.out.println("请求参数名：" + file.getName());
		String originalFilename = file.getOriginalFilename();
		System.out.println("文件真实名称：" + originalFilename);

		System.out.println("额外参数：" + username);
		//将文件存储到服务端
		//输入流
		InputStream in = file.getInputStream();
		BufferedInputStream bis = new BufferedInputStream(in);
		//输出流
		ServletContext application = request.getServletContext();//获取ServletContext
		String folderPath = application.getRealPath("/upload");
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File saveFile = new File(folder.getAbsolutePath() + "/" + UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf('.')));
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(saveFile));
		//开始传输文件
		byte[] bytes = new byte[1024 * 10];
		int readCount = 0;
		while ((readCount = bis.read(bytes)) != -1) {
			bos.write(bytes, 0, readCount);
		}
		bos.flush();
		//释放资源
		bos.close();
		bis.close();

		return "get-parameters/form";
	}
}
