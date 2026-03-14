package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/common")
public class CommonController {

    // 这里通常配置在 yml 中，作为文件保存物理地址
    // 简单起见，这里放在项目根目录下的 uploads 文件夹
    private final String uploadPath = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            // 确保目录存在
            File targetDir = new File(uploadPath);
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                log.error("创建上传目录失败: {}", uploadPath);
                return Result.error("文件上传失败，服务器配置异常");
            }

            // 获取原文件名及后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            // 生成唯一文件名
            String newFilename = UUID.randomUUID().toString() + suffix;

            // 保存文件
            File destFile = new File(uploadPath + newFilename);
            file.transferTo(destFile);

            // 实际项目中应返回 OSS/CDN 的网络地址或文件在服务器对应的相对路由
            // 这里暂且返回通过特定静态资源映射暴露出来的路径字符串
            String fileUrl = "/uploads/" + newFilename;

            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("上传错误: " + e.getMessage());
        }
    }
}
