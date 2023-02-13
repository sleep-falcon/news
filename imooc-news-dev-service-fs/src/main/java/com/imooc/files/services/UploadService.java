package com.imooc.files.services;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    public String uploadFdfs(MultipartFile file, String fileExtName) throws Exception;
}
