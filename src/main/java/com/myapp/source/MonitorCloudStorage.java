package com.myapp.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Blob.BlobSourceOption;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myapp.pojos.GcsEvent;
import com.myapp.pojos.UserData; 

public class MonitorCloudStorage implements BackgroundFunction<GcsEvent> {
	private static final Logger logger = Logger.getLogger(MonitorCloudStorage.class.getName());

	@Override
	public void accept(GcsEvent event, Context context) { 
		 
		try {
			logger.info("Event: " + context.eventId());
			logger.info("Event Type: " + context.eventType());
			String bucket = event.getBucket(); 
			logger.info("Bucket: " + bucket);
			String objName = event.getName(); 
			logger.info("File: " + event.getName());
			logger.info("Metageneration: " + event.getMetageneration());
			logger.info("Created: " + event.getTimeCreated());
			logger.info("Updated: " + event.getUpdated());
			byte[] fileContent = readBlobData(bucket, objName); 
			
			GetUserData getUserData = new GetUserData();
			UserData userData = getUserData.queryUserData(bucket, objName); 
			logger.info("Data passed by user: " + userData);
			
			ConvertSpeechToText convertSpeechToText = new ConvertSpeechToText();
			String transcription = convertSpeechToText.convert(userData, fileContent);
			logger.info("Transcribed Text: " + transcription);
			
			String projectId = "teak-mantis-279104";
			String topicId = "translation";
			List<String> messages = new ArrayList<String>();
			// publish transcribed text and attributes passed by the user
			messages.add(transcription);
			Map<String, String> attrMap = new Gson().fromJson(new Gson().toJson(userData), 
		            new TypeToken<HashMap<String, String>>() {}.getType()
		    );
			PublishMessages publish = new PublishMessages(); 
			List<String> messageIds = publish.publishMessages(projectId, topicId, messages, attrMap);
			for (String messageId : messageIds) {
				logger.info("Published with message ID: " + messageId);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public byte[] readBlobData(String bucketName, String objectName) throws IOException {
		Storage storage = StorageOptions.getDefaultInstance().getService();
		Blob blob = storage.get(BlobId.of(bucketName, objectName));
		byte[] content = blob.getContent(BlobSourceOption.generationMatch());
		return content; 
	  }
}
