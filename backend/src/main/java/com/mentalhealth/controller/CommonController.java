package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
 * 通用文件上传控制器
 * 将上传的图片转换为 Base64 Data URL 返回给前端，
 * 由前端存入用户资料字段（存储在数据库中），不再写入磁盘文件。
 */
@Slf4j
@RestController
@RequestMapping("/api/common")
public class CommonController {

    /**
     * 上传图片：接收 multipart 文件，转为 Base64 Data URL 返回
     * 返回格式：data:image/png;base64,iVBORw0KGgoAAAANS...
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        // 限制文件大小为 2MB（Base64 编码后体积会膨胀约 33%）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("图片大小不能超过 2MB");
        }

        try {
            // 获取 MIME 类型（如 image/png、image/jpeg）
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("仅支持图片格式文件");
            }

            // 将文件字节转为 Base64 编码
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            // 拼接 Data URL 格式
            String dataUrl = "data:" + contentType + ";base64," + base64;

            log.info("图片上传成功，Base64 长度: {} 字符", dataUrl.length());
            return Result.success(dataUrl);
        } catch (Exception e) {
            log.error("文件转 Base64 失败", e);
            return Result.error("上传处理失败: " + e.getMessage());
        }
    }
}
