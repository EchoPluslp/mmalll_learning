package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件处理的service
 *
 * @author Liupeng
 * @create 2018-05-01 21:26
 **/
public interface IFileService {
    String upload(MultipartFile file, String path);
}