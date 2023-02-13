package com.imooc.files.controller;

import com.imooc.api.controller.files.FileUploadControllerApi;
import com.imooc.exceptions.GraceException;
import com.imooc.files.FileResource;
import com.imooc.files.GridFSConfig;
import com.imooc.files.services.UploadService;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;

import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.utils.FileUtils;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


@RestController
public class FileUploadController implements FileUploadControllerApi {
    final static Logger logger = LoggerFactory.getLogger(FileUploadController.class);


    @Autowired
    private GridFSConfig gridFSConfig;

    @Autowired
    private GridFSBucket bucket;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private FileResource fileResource;

    @Override
    public GraceJSONResult uploadFace(String userId, MultipartFile file) throws Exception {
        String path = null;
        if(file!=null){
            //获得文件上传的名称
            String filename = file.getOriginalFilename();
            //判断文件名不能为空
            if(StringUtils.isNotBlank(filename)){
                String nameArr[] = filename.split("\\.");
                //获得后缀
                String suffix = nameArr[nameArr.length-1];
                //判断上传文件后缀是否符合预定义规范
                if(!suffix.equalsIgnoreCase("png")&&
                        !suffix.equalsIgnoreCase("jpg")&&
                        !suffix.equalsIgnoreCase("jpeg")){
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
                }
                path = uploadService.uploadFdfs(file,suffix);
            }else{
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
            }
        }else{
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        if(StringUtils.isNotBlank(path)){
            path = fileResource.getHost()+path;
        }else{
            GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        logger.info("path: "+path);

        return GraceJSONResult.ok(path);
    }

    @Override
    public GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) {
        //获得图片base64字符串
        String file64 = newAdminBO.getImg64();
        String fileIdStr = null;
        try {
            //将base64字符串转换为byte数组
            byte[] bytes = new BASE64Decoder().decodeBuffer(file64.trim());

            //转换为输入流
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

            //上传到gridfs
            ObjectId fileId =  bucket.uploadFromStream(newAdminBO.getUsername()+".png",inputStream);

            //获得文件在gridfs中的主键id
            fileIdStr = fileId.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return GraceJSONResult.ok(fileIdStr);
    }

    @Override
    public void readInGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
        //判断是否存在
        if(StringUtils.isBlank(faceId)||faceId.equals("null")){
           GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        //从gridfs中读取图片信息
        File adminFace = readGridFsByFaceId(faceId);

        //将图片输出到浏览器
        FileUtils.downloadFileByStream(response,adminFace);

    }

    private File readGridFsByFaceId(String faceId) throws FileNotFoundException {
        //根据主键查询
        GridFSFindIterable iterable = bucket.find(Filters.eq("_id",new ObjectId(faceId)));
        GridFSFile gridFSFile = iterable.first();

        //判断是否为空
        if(gridFSFile==null){
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        String fileName = gridFSFile.getFilename();

        //获取文件流，保存文件到本地或者服务器的临时目录
        File filetemp = new File("/Users/Shared/temp_face/");
        if(!filetemp.exists()){
            filetemp.mkdirs();
        }
        File myFile = new File("/Users/Shared/temp_face/"+fileName);

        //创建文件输出流
        OutputStream os = new FileOutputStream(myFile);
        //下载到服务器/本地
        bucket.downloadToStream(new ObjectId(faceId),os);

        return myFile;
    }

    @Override
    public GraceJSONResult readFace64InGridFS(String faceId, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
        //获得人脸文件
        File myFace = readGridFsByFaceId(faceId);

        //转换为base64
        String base64pic =  FileUtils.fileToBase64(myFace);
        return GraceJSONResult.ok(base64pic);

    }

    @Override
    public GraceJSONResult uploadSomeFiles(String userId, MultipartFile[] files) throws Exception {
        //声明list，用于存放多个图片的路径地址，返回到前端
        List<String> imageUrl = new ArrayList<>();
        if(files!=null&&files.length>0){
            for(MultipartFile file:files){
                String path = "";
                if(file!=null){
                    //获得文件上传的名称
                    String filename = file.getOriginalFilename();
                    //判断文件名不能为空
                    if(StringUtils.isNotBlank(filename)){
                        String nameArr[] = filename.split("\\.");
                        //获得后缀
                        String suffix = nameArr[nameArr.length-1];
                        //判断上传文件后缀是否符合预定义规范
                        if(!suffix.equalsIgnoreCase("png")&&
                                !suffix.equalsIgnoreCase("jpg")&&
                                !suffix.equalsIgnoreCase("jpeg")){
                            continue;
                        }
                        path = uploadService.uploadFdfs(file,suffix);
                    }else{
                        continue;
                    }
                }else{
                        continue;
                }
                if(StringUtils.isNotBlank(path)){
                    path = fileResource.getHost()+path;
                    //进行图片审核
                    imageUrl.add(path);
                }else{
                    continue;
                }
                logger.info("path: "+path);
            }
        }
        return GraceJSONResult.ok(imageUrl);
    }
}
