package com.myapp.test.source;

import java.util.Date;

import com.google.gson.Gson;
import com.myapp.pojos.GcsEvent;
import com.myapp.test.pojos.MockContext;

public class ConstructJsonRequest {

	public static void main(String[] args) {

		Date now = new Date(); 
		GcsEvent gcsEvent = new GcsEvent(); 
		gcsEvent.setBucket("some-bucket");
		gcsEvent.setMetageneration("1");
		gcsEvent.setName("some-file.txt");
		gcsEvent.setTimeCreated(now);
		gcsEvent.setUpdated(now);
		
		MockContext context = new MockContext(); 
		context.eventType = "google.storage.object.finalize";	
		
		RequestPayload request = new RequestPayload(); 
		request.setEvent(gcsEvent);
		request.setCtx(context);
		
		Gson gson = new Gson();
		String json = gson.toJson(request);
		System.out.println("Request Payload: " + json);
		
	}

}

class RequestPayload {
	
	GcsEvent event; 
	MockContext ctx;
	
	public GcsEvent getEvent() {
		return event;
	}
	public void setEvent(GcsEvent event) {
		this.event = event;
	}
	public MockContext getCtx() {
		return ctx;
	}
	public void setCtx(MockContext ctx) {
		this.ctx = ctx;
	} 
	
}