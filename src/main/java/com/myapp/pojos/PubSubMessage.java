package com.myapp.pojos;

// [START functions_helloworld_pubsub_message]
import java.util.Map;

public class PubSubMessage {
  // Cloud Functions uses GSON to populate this object.
  // Field types/names are specified by Cloud Functions
  // Changing them may break your code!
  String data;
  Map<String, String> attributes;
  String messageId;
  String publishTime;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getPublishTime() {
    return publishTime;
  }

  public void setPublishTime(String publishTime) {
    this.publishTime = publishTime;
  }
}
