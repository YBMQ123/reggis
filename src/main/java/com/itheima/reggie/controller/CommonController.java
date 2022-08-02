package com.itheima.reggie.controller;

import com.alibaba.druid.pool.ha.selector.StickyRandomDataSourceSelector;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    //设置一个根目录
    @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件的上传
     */
   @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
       //获取图片原始文件名
       String originalFilename = file.getOriginalFilename();
       //截取文件的后缀扩展名
       String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
       //使用UUID重新生成文件名，防止文件名称造成重复造成文件的覆盖
       String filename= UUID.randomUUID().toString()+suffix;

       //判断目录是否存在，不存在则创建
       File dir=new File(basePath);
       if (!dir.exists()){
           //不存在则创建
           dir.mkdirs();
       }
       //file是一个临时文件，需要转存到指定位置，否则本次请求完后临时文件会被删除
       try {
           file.transferTo(new File(basePath+filename));
       } catch (IOException e) {
           e.printStackTrace();
       }
       return R.success(filename);
   }

    /**
     * 文件的下载
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //输入流，通过输入流读取文件的内容
            FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));
            //输出流，通过输出流将文件写回到浏览器，在浏览器中展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            int len=0;
            byte[] bytes=new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭资源
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
