package com.imooc.api.controller.files;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;

@Api(value = "文件上传的controller", tags = {"文件上传的controller"})
@RequestMapping("/fs")
public interface FileUploadControllerApi {

    /**
     * 上传单文件
     * @param userId
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadFace")
    @ApiOperation(value = "上传用户头像",notes = "上传用户头像",httpMethod = "POST")
    public GraceJSONResult uploadFace(@RequestParam String userId, MultipartFile file) throws Exception;


    /**
     * 上传多个文件
     * @param userId
     * @param files
     * @return
     * @throws Exception
     */
    @PostMapping("/uploadSomeFiles")
    @ApiOperation(value = "上传用户头像",notes = "上传用户头像",httpMethod = "POST")
    public GraceJSONResult uploadSomeFiles(@RequestParam String userId, MultipartFile[] files) throws Exception;

    /**
     * 文件上传到mongodb的gridfs中
     * @param newAdminBO
     * @return
     */
    @PostMapping("/uploadToGridFS")
    public GraceJSONResult uploadToGridFS(@RequestBody NewAdminBO newAdminBO);

    /**
     * 从GridFS中获得图片内容
     * @param faceId
     * @return
     */
    @GetMapping("/readInGridFS")
    public void readInGridFS(@RequestParam String faceId,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws FileNotFoundException;

    /**
     * 从gridfs中读取图片并返回base64
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws FileNotFoundException
     */
    @GetMapping("/readFace64InGridFS")
    public GraceJSONResult readFace64InGridFS(@RequestParam String faceId,
                             HttpServletRequest request,
                             HttpServletResponse response) throws FileNotFoundException;

}
