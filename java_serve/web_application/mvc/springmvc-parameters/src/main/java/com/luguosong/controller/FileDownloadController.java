package com.luguosong.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 文件下载
 *
 * @author luguosong
 */
@Controller
@RequestMapping("/fileDownload")
public class FileDownloadController {

    @RequestMapping("/springMvc")
    public ResponseEntity<byte[]> springMvc(
            HttpServletRequest request
    ) throws IOException {
        File file = new File(request.getServletContext().getRealPath("/upload") + "/demo.jpg");
        //创建响应头对象
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getName());

        //下载文件
        ResponseEntity<byte[]> entity = new ResponseEntity<>(Files.readAllBytes(file.toPath()), headers, HttpStatus.OK);
        return entity;
    }
}
