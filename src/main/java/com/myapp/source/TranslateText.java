package com.myapp.source;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.myapp.pojos.PubSubMessage;

public class TranslateText implements BackgroundFunction<PubSubMessage> {

	private static final Logger logger = Logger.getLogger(TranslateText.class.getName());
	final static String GOOGLE_CLOUD_PROJECT = System.getenv("GOOGLE_CLOUD_PROJECT");

	@Override
	public void accept(PubSubMessage message, Context context) {
		try {
			if (message.getData() == null) {
				logger.info("No message provided");
				return;
			}

			String messageString = new String(
					Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
					StandardCharsets.UTF_8);
			logger.info(messageString);

			// Supported Languages: https://cloud.google.com/translate/docs/languages
			Map<String, String> attrMap = message.getAttributes();
			String targetLanguage = attrMap.get("target_language");

			String translatedText = translateText(GOOGLE_CLOUD_PROJECT, targetLanguage, messageString);
			logger.info("Translated Text: " + translatedText);
			String topicId = "filewriter";
			List<String> messages = new ArrayList<String>();
			// publish translated text and attributes passed by the user
			messages.add(translatedText);
			PublishMessages publish = new PublishMessages();
			List<String> messageIds = publish.publishMessages(GOOGLE_CLOUD_PROJECT, topicId, messages, attrMap);
			for (String messageId : messageIds) {
				logger.info("Published with message ID: " + messageId);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Translating Text
	public String translateText(String projectId, String targetLanguage, String text) throws IOException {

		// Initialize client that will be used to send requests. This client only needs
		// to be created once, and can be reused for multiple requests. After completing
		// all of your
		// requests, call the "close" method on the client to safely clean up any
		// remaining background
		// resources.
		try (TranslationServiceClient client = TranslationServiceClient.create()) {
			// Supported Locations: `global`, [glossary location], or [model location]
			// Glossaries must be hosted in `us-central1`
			// Custom Models must use the same location as your model. (us-central1)
			LocationName parent = LocationName.of(projectId, "global");

			// Supported Mime Types:
			// https://cloud.google.com/translate/docs/supported-formats
			TranslateTextRequest request = TranslateTextRequest.newBuilder().setParent(parent.toString())
					.setMimeType("text/plain").setTargetLanguageCode(targetLanguage).addContents(text).build();

			TranslateTextResponse response = client.translateText(request);

			StringBuffer sb = new StringBuffer();
			// Display the translation for each input text provided
			for (Translation translation : response.getTranslationsList()) {
				sb.append(translation.getTranslatedText());
			}
			return sb.toString();
		}
	}

}
