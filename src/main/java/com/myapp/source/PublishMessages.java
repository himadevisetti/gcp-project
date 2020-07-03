package com.myapp.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

public class PublishMessages {

	public List<String> publishMessages(String projectId, String topicId, List<String> messages, Map<String, String> attributes) {
		
		ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
		Publisher publisher = null;
		List<ApiFuture<String>> messageIdFutures = new ArrayList<>();
		List<String> messageIds = new ArrayList<>();

		try {
			// Create a publisher instance with default settings bound to the topic
			publisher = Publisher.newBuilder(topicName).build();

			// schedule publishing one message at a time : messages get automatically batched
			for (String message : messages) {
				ByteString data = ByteString.copyFromUtf8(message);
				PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).putAllAttributes(attributes).build();

				// Once published, returns a server-assigned message id (unique within the topic)
				ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
				messageIdFutures.add(messageIdFuture);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
			try {
			// wait on any pending publish requests.
			messageIds = ApiFutures.allAsList(messageIdFutures).get();

			if (publisher != null) {
				// When finished with the publisher, shutdown to free up resources.
				publisher.shutdown();
				publisher.awaitTermination(1, TimeUnit.MINUTES);
			}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return messageIds;
	}

}
