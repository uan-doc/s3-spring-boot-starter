package br.com.uan.s3.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.uan.s3.S3Template;

@Configuration
@ConditionalOnMissingBean(S3Template.class)
@ConfigurationProperties(prefix = "s3")
public class S3AutoConfiguration
{
    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.region}")
    private String region;

    @Value("${s3.accessKey}")
    private String accessKey;

    @Value("${s3.secretKey}")
    private String secretKey;

    @Value("${s3.tempFolder}")
    private String tempFolder;

    @Bean
    S3Template amazonS3Template() throws IOException {
        return new S3Template(endpoint, region, accessKey, secretKey, tempFolder);
    }
}