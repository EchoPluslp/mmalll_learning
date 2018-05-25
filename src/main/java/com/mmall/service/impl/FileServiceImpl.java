package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**文件处理的service的实现类
 * @author Liupeng
 * @create 2018-05-01 21:27
 **/
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     *targetFile 使用设置的path和文件名，新建文件对象
     * @param file MultipartFile用于接收上传的图片信息
     * @param path 上传的目的路径
     * @return 返回targetFileName 目标文件名称
     */
    public String upload(MultipartFile file,String path){
        //得到当前文件名
        String filename = file.getOriginalFilename();
        //得到当前扩展名
        String fileExtensionName = filename.substring(filename.lastIndexOf(".")+1);
        //生成新文件名
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        //logger的书写方式，{}相当于占位符
        logger.info("开始上传文件,上传的文件名{},上传的路径{},新文件名{}",filename,path,uploadFileName);

        //根据当前路径创建文件对象
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.setReadable(true);
            //生成该路径下的所有文件
            fileDir.mkdirs();
        }
        //创建好了新的目标文件路径，开始上传文件
        File targetFile = new File(path,uploadFileName);

        try {
            //将内存中的图片，写入到磁盘。
            file.transferTo(targetFile);

            //将文件夹上传到FTP服务器下
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            //已经上传到ftp服务器下
            // 上传了后，删除upload文件夹下面的文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件失败",e);
            e.printStackTrace();
            return null;
        }
        return targetFile.getName();
    }
}