package br.com.uan.s3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

//@Component
public class S3Template
{
    protected Logger logger = LoggerFactory.getLogger(S3Template.class);

    private String   endpoint;
    private String   region;
    private String   accessKey;
    private String   secretKey;
    private File     tempFolder;

    public S3Template(String endpoint, String region, String accessKey, String secretKey, String tempFolder) throws IOException {
        this.endpoint = endpoint;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.tempFolder = new File(tempFolder);

        if (!this.tempFolder.exists()) {
            Files.createDirectory(this.tempFolder.toPath());
        }
        else {
            if (!this.tempFolder.isDirectory()) {
                throw new RuntimeException(
                        "O arquivo temporário informado na propriedade s3.tempFolder = '" + tempFolder + "' não é um diretório");
            }
        }
    }

    /**
     * O Client do S3.
     */
    private AmazonS3 s3;

    public File download(String bucket, String filename) throws IOException {
        S3Object o;
        try {
            logger.info("Encontrando o arquivo {}@{} no S3...", filename, bucket);
            o = getS3Client().getObject(bucket, filename);
            logger.info("Arquivo encontrado!");
        }
        catch (AmazonS3Exception e) {
            logger.error("Erro ao procurar o arquivo no S3", e);
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {// O arquivo não
                                                               // existe no S3
                throw new FileNotFoundException();
            }
            throw e;
        }

        // Técnica para sincronizar através de uma String
        final String synchronizing = filename.intern();

        synchronized (synchronizing) {
            File result = new File(tempFolder, filename);
            logger.info("Baixando o arquivo {}@{} do S3 de forma síncrona para {}", filename, bucket, result.getPath());
            S3ObjectInputStream s3is = o.getObjectContent();
            FileOutputStream fos = new FileOutputStream(result);
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
            logger.info("Bytes do arquivo baixados e salvos com sucesso em {}", result.getPath());
            return result;
        }
    }

    public void save(String bucket, String filename, File file) {
        getS3Client().putObject(bucket, filename, file);
    }

    public void save(String bucket, String filename, InputStream inputStream) {
        getS3Client().putObject(bucket, filename, inputStream, null);
    }

    public void delete(String bucket, String filename) {
        getS3Client().deleteObject(bucket, filename);
    }

    private synchronized AmazonS3 getS3Client() {
        if (s3 == null) {
            logger.info("Abrindo a conexão com o S3 pela primeira vez.");
            s3 = AmazonS3ClientBuilder.standard().withEndpointConfiguration(new EndpointConfiguration(endpoint, region))
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))).build();
            logger.info("Conexão aberta com sucesso!");
        }
        return s3;
    }
}
