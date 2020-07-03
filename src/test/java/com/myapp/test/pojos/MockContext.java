package com.myapp.test.pojos;

import com.google.cloud.functions.Context;

// Class that mocks Cloud Functions "context" objects
// Used to create fake context objects for function tests
public class MockContext implements Context {
  public String eventId;
  public String eventType;
  public String timestamp;
  public String resource;

  @Override
  public String eventId() {
    return this.eventId;
  }

  @Override
  public String timestamp() {
    return this.timestamp;
  }

  @Override
  public String eventType() {
    return this.eventType;
  }

  @Override
  public String resource() {
    return this.resource;
  }
}
