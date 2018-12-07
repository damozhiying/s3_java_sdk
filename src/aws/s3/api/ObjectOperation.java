package aws.s3.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingRequest;
import com.amazonaws.services.s3.model.GetObjectTaggingResult;
import com.amazonaws.services.s3.model.SetObjectTaggingRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.Tag;
import utils.CommonUtils;



public class ObjectOperation extends BaseOperation {

	public static Logger log= Logger.getLogger(ObjectOperation.class.getName());
	

	/*list桶中的对象*/
	public static void listObjects(String bucket_name) {
		try {
//				AmazonS3 s3client  = getS3Client();
				ListObjectsV2Result result = s3client.listObjectsV2(bucket_name);
				List<S3ObjectSummary> objects = result.getObjectSummaries();
				log.info("listObjects successfully!");
				objects.forEach(object -> log.info("object: "+ object.getKey()));
		} catch (AmazonServiceException e) {
			e.printStackTrace();
		}
	}
	
	/*上传单个文件*/
	public static void putObject(String bucket_name, String object_key,  String file_path) {
		try {
			   log.debug("start to upload file: " + file_path);
			   PutObjectRequest por = new PutObjectRequest(bucket_name, object_key,  new File(file_path));
			   s3client.putObject(por);
//			   s3client.putObject(bucket_name, object_key, new File(file_path));  // work too
			   log.info("upload object successfully, object_key: " + object_key);
	         } catch (AmazonServiceException e) {
	        	 e.printStackTrace();
	            log.error("upload object failed,  object_key:" + object_key + " error: " + e.getMessage());
	        }
	    }
	
	
	/*递归上传某个目录的文件*/
	
	public static Boolean interateFolder(String start_path) {
		File current_file = new File(start_path);
		if( ! current_file.isDirectory() ) {  // 首先校验是否是目录
			System.out.println("filepath is not a  directory!");
			return false;
		}
		for(File file : current_file.listFiles()) {  // 开始遍历当前目录
			String current_path =file.getAbsolutePath();
			if(file.isFile()) {  // 文件，则上传
				System.out.println(current_path);
				String object_key = current_path.substring(upload_dir.length()).replace("\\", "/");
				if(object_key.startsWith("/")) {
					object_key = object_key.substring(1);
				}
				putObject(bucket, object_key, current_path);  
			}else { // 目录，则递归
				interateFolder(current_path);
			}
		}
		return true;			
   }
	
	
	/*下载单个文件*/
	public static void getObject(String bucket_name, String object_key,  String target_file) {
		// target_file:  对象下载后，对应的本地文件路径
		try {
			   log.debug("start to download object_key: " + object_key);
			   GetObjectRequest gor =new  GetObjectRequest(bucket_name, object_key);
			   S3Object object =  s3client.getObject(gor);
//			   S3Object object=  s3client.getObject(bucket_name, object_key);  // work too
			   S3ObjectInputStream  s3is = object.getObjectContent();
			   CommonUtils.saveFileByFIS(s3is, target_file);
			   log.info("download object successfully, target_file: " + target_file);
	         } catch (AmazonServiceException e) {
	        	 e.printStackTrace();
	            log.error("download object failed,  target_file:" + target_file + " error: " + e.getMessage());
	           }
	    }

	/*复制对象*/
	public static void copyObject( String from_bucket,  String from_object, String to_bucket,  String to_object) {
		try {
				s3client.copyObject(from_bucket, from_object, to_bucket,  to_object);
			   log.info("copy  object successfully, object_key: " + from_object);
	         } catch (AmazonServiceException e) {
	        	 e.printStackTrace();
	        }
	    }
	
	
	/* 删除单个文件*/
	public static void deleteObject(String bucket_name, String object_key) {
		try {
			   log.debug("start to delete object_key: " + object_key);
			   DeleteObjectRequest dor = new DeleteObjectRequest(bucket_name, object_key);
			   s3client.deleteObject(dor);
//			   s3client.deleteObject(bucket_name,  object_key);  // work too
			   log.info("delete object successfully, object_key: " + object_key);
	        } catch (AmazonServiceException e) {
	        	 e.printStackTrace();
	            log.error("delete object failed,  object_key:" + object_key + " error: " + e.getMessage());
	           }
	    }
	
	/* 删除单个文件*/
	public static void deleteObjects(String bucket_name, String[] object_keys) {
		try {
			   log.debug("start to delete object_keys: " + Arrays.toString(object_keys));
			   DeleteObjectsRequest dor = new DeleteObjectsRequest(bucket_name).withKeys(object_keys);
			   s3client.deleteObjects(dor);
			   // s3client.deleteObject(bucket_name,  object_key);  // work too
			  log.info("delete object successfully, object_keys: " + Arrays.toString(object_keys));
	        } catch (AmazonServiceException e) {
	        	 e.printStackTrace();
	            log.error("delete object failed,  object_keys:" + Arrays.toString(object_keys) + " error: " + e.getMessage());
	           }
	    }
	
	
	/* 设置标签*/
	public static void setTags(String bucket_name, String object_key, List<Tag> new_tags) {
		try {
		   SetObjectTaggingRequest sotr = new SetObjectTaggingRequest(bucket_name, object_key, null);
		   // tag不能增量添加，只能每次拿到之前的，然后全量put
		   List<Tag> old_tags = getTags(bucket_name, object_key);
		   for(Tag old_tag : old_tags) {
			   if( ! new_tags.contains(old_tag) ){
				   new_tags.add(old_tag);
			   }
		   	}
		   new_tags.forEach(tag -> log.info("set tag " + tag.getKey() + " : " + tag.getValue()));
		   sotr.setTagging(new ObjectTagging(new_tags));
		   s3client.setObjectTagging(sotr);
		   log.info("set new tag successfully!");
			} catch (AmazonServiceException e) {
        	 e.printStackTrace();
           }
	}
	
	
	/* 获取标签*/
	public static  List<Tag> getTags(String bucket_name, String object_key) {
		 List<Tag> tags = null;
		try {
		   GetObjectTaggingRequest gotr = new GetObjectTaggingRequest(bucket_name, object_key);
		   GetObjectTaggingResult result =  s3client.getObjectTagging(gotr);
		   tags =  result.getTagSet();
		   tags.forEach(tag -> log.info("get tag " +tag.getKey()+ " : " + tag.getValue()));
        } catch (AmazonServiceException e) {
        	 e.printStackTrace();
           }
		return tags;
	}
	
	
	/*设置元数据*/
	public static void setMetaData(String bucket_name, String object_key, String file_path, ObjectMetadata metadata) {
		try {
			// 无法单独设置对象元数据，只能在上传的时候设置
			PutObjectRequest por = new PutObjectRequest(bucket_name, object_key,  new File(file_path));
			por.setMetadata(metadata);
			s3client.putObject(por);
			log.info("set metadata successfully");
		}catch(AmazonServiceException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/*获取元数据*/
	public static Object getMetaData(String bucket_name, String object_key, String meta_key) {
		Object meta_value = null;
		try {
			GetObjectMetadataRequest gmr = new GetObjectMetadataRequest(bucket_name, object_key) ;
			ObjectMetadata metadata  = s3client.getObjectMetadata(gmr);
			meta_value = metadata.getUserMetaDataOf(meta_key);
			if (meta_value == null) {
				meta_value = metadata.getRawMetadataValue(meta_key);
			}
			Map<String, String> map = metadata.getUserMetadata();
			map.keySet().forEach(key -> log.info(meta_key + ":" + map.get(key)));
			log.debug("get metadata " + meta_key + "  :  " + meta_value);
		}catch(AmazonServiceException e) {
			e.printStackTrace();
		}
		return meta_value;
	}
	
	
	public static void main(String[] args) {
		String local_file = System.getProperty("user.dir") + "\\test_resource\\common.log";
		String object_key = "test_resource/common.log";   // ====warning==== object_key can't start with '/' 
		String object_key2 = "test_resource/中文.log2"; 
		String target_file = System.getProperty("user.dir") + "\\test_resource\\common.log_temp";
		String object_keys[] = {"test_resource/common.log", "test_resource/common.log2"};
		ObjectMetadata metadata =new ObjectMetadata();
		metadata.addUserMetadata("name", "wakesy");
		List<Tag> new_tags = new ArrayList<>();
		for(int i =0; i <2; i ++) {
			new_tags.add(new Tag("name"+i, "wakesy"+i));
		}
		
		ObjectOperation object_operation =new ObjectOperation();
//		listObjects(bucket);
//		putObject(bucket, object_key, local_file); 
//		copyObject(bucket, "test_resource/common.log", "wakesy2", "test_resource/common.log");
//		getObject(bucket, object_key, target_file);
//		deleteObject(bucket, object_key);
//		deleteObjects(bucket, object_keys);
//		setTags(bucket, object_key, new_tags);    // failed, Service: Amazon S3; Status Code: 501; Error Code: NotImplemented;
//		getTags(bucket, object_key);
//		setMetaData(bucket, object_key, local_file, metadata);
//		getMetaData(bucket, object_key, "name");
//		interateFolder(upload_dir);
	}

}