package com.myapp.test.source;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import com.myapp.pojos.GcsEvent;
import com.myapp.source.MonitorCloudStorage;
import com.myapp.test.pojos.MockContext;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MonitorCloudStorageTest {

	  // Must be declared at class-level, or LoggingHandler won't detect log records!
	  private static final Logger logger = Logger.getLogger(MonitorCloudStorage.class.getName());

	  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

	  @BeforeClass
	  public static void beforeClass() {
	    logger.addHandler(LOG_HANDLER);
	  }

	  @Test
	  public void functionsMonitorCloudStorage_shouldPrintEvent() throws IOException {
	    GcsEvent event = new GcsEvent();
	    event.setBucket("some-bucket");
	    event.setName("some-file.txt");
	    event.setTimeCreated(new Date());
	    event.setUpdated(new Date());

	    MockContext context = new MockContext();
	    context.eventType = "google.storage.object.finalize";

	    new MonitorCloudStorage().accept(event, context);

	    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
	    assertThat(logs.get(1).getMessage()).isEqualTo(
	        "Event Type: google.storage.object.finalize");
	    assertThat(logs.get(2).getMessage()).isEqualTo("Bucket: some-bucket");
	    assertThat(logs.get(3).getMessage()).isEqualTo("File: some-file.txt");
	  }
	
}
