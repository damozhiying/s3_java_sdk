package aws.s3.api;


import org.apache.log4j.Logger;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import utils.CommonUtils;
import utils.PropertyUtil;


public class BaseOperation {
	
	public static String endpoint ;
	public static String access_key ;
	public static String secret_key ;
	public static String bucket ;
	public static String upload_dir ;
	public static AmazonS3 s3client ;
	public static Logger log= Logger.getLogger(BaseOperation.class.getName());
	
	public BaseOperation() {
		this.access_key = PropertyUtil.getProperty("access_key");
		this.secret_key = PropertyUtil.getProperty("secret_key");
		this.endpoint = PropertyUtil.getProperty("endpoint");
		this.bucket = PropertyUtil.getProperty("bucket");
		this.upload_dir = PropertyUtil.getProperty("upload_dir");
		CommonUtils.initLog4jConfig();  // 日志配置文件重定向
		initS3Connection();   // 初始化s3连接
	}
	
	/* 初始化s3 连接客户端*/
	public static void initS3Connection() {
		try {
			if(s3client  == null) {
				AWSCredentials credentials = new BasicAWSCredentials(access_key, secret_key);
				ClientConfiguration connconfig = new ClientConfiguration();
	            connconfig.setProtocol(Protocol.HTTP);
	            s3client = new  AmazonS3Client(credentials, connconfig);
	            s3client.setEndpoint(endpoint);
	            log.info("Create s3 connection successfully!");
//	            s3client.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build());
//	            s3client.setRegion(Region.getRegion(Regions.CN_NORTHWEST_1));
			}
		}catch (AmazonServiceException e) {
			e.printStackTrace();
		}
	}
	
	
}
