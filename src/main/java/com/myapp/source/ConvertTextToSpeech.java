package com.myapp.source;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.common.io.Files;
import com.google.protobuf.ByteString;
import com.myapp.pojos.PubSubMessage;
import com.myapp.utils.CommonUtils;

public class ConvertTextToSpeech implements BackgroundFunction<PubSubMessage> {

	private static final Logger logger = Logger.getLogger(ConvertTextToSpeech.class.getName());
	final static String GCLOUD_STORAGE_OUTPUT_BUCKET = System.getenv("GCLOUD_STORAGE_OUTPUT_BUCKET");

	@Override
	public void accept(PubSubMessage message, Context context) throws Exception {
		if (message.getData() == null) {
			logger.info("No message provided");
			return;
		}

		String messageString = new String(
				Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
		logger.info(messageString);

		Map<String, String> attrMap = message.getAttributes();
		convertTextToSpeech(messageString, attrMap);
	}

	/** Demonstrates using the Text-to-Speech API. */
	public void convertTextToSpeech(String message, Map<String, String> attrMap) throws Exception {

		String targetLanguage = attrMap.get("target_language");
		String fileName = attrMap.get("file_name");

		// Instantiates a client
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
			// Set the text input to be synthesized
			SynthesisInput input = SynthesisInput.newBuilder().setText(message).build();

			// Build the voice request, select the language code and the ssml voice gender
			VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode(targetLanguage)
					.setSsmlGender(SsmlVoiceGender.NEUTRAL).build();

			// Select the type of audio file you want returned
			AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

			// Perform the text-to-speech request on the text input with the selected voice
			// parameters and
			// audio file type
			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

			// Get the audio contents from the response
			ByteString audioContents = response.getAudioContent();

			String fileNameNoExt = Files.getNameWithoutExtension(fileName);
			String objName = "Translated_" + fileNameNoExt + ".mp3";

			byte[] content = audioContents.toByteArray();
			writeToGcs(GCLOUD_STORAGE_OUTPUT_BUCKET, objName, content);
		}
	}

	public void writeToGcs(String bucketName, String objectName, byte[] content) throws Exception {
		Storage storage = CommonUtils.getStorageClient();
		Bucket bucket = storage.get(bucketName);
		bucket.create(objectName, content);
		BlobId blobId = BlobId.of(bucketName, objectName);
		storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
		logger.info("Uploaded to bucket " + bucketName + " as " + objectName);
	}

}
