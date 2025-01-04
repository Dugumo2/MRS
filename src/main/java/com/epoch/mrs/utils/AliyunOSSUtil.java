package com.epoch.mrs.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class AliyunOSSUtil {

    // OSS 配置参数
    private static final String ENDPOINT = "oss-cn-chengdu.aliyuncs.com";
    private static final String BUCKET_NAME = "mrs-manger";
    private static final String BUCKET_DOMAIN = "https://mrs-manger.oss-cn-chengdu.aliyuncs.com"; // 使用 https
    private static final String REGION = "cn-chengdu";

    /**
     * 上传文件到阿里云 OSS 并返回访问 URL
     *
     * @param file MultipartFile 文件
     * @return 文件访问 URL
     * @throws IOException 上传过程中发生的异常
     */
    public static String uploadFile(MultipartFile file) throws IOException, ClientException {
        // 创建 OSSClient 实例
        EnvironmentVariableCredentialsProvider credentialsProvider =
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(ENDPOINT)
                .credentialsProvider(credentialsProvider)
                .region(REGION)
                .build();

        try {
            // 生成唯一的文件名，避免覆盖
            String originalFilename = file.getOriginalFilename();
            String fileExt = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExt = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString().replaceAll("-", "") + fileExt;

            // 定义文件在 OSS 中存储的路径，可以根据需要进行调整
            String objectName = "images/" + uniqueFileName;

            // 创建 PutObjectRequest 对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, objectName, file.getInputStream());

            // 上传文件
            ossClient.putObject(putObjectRequest);

            // 构建文件访问 URL
            String fileUrl = BUCKET_DOMAIN + "/" + objectName;

            return fileUrl;
        } catch (IOException e) {
            throw e;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}