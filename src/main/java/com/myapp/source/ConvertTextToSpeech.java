package com.myapp.source;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;

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
		logger.info("Message string has " + messageString.length() + " characters");

		Map<String, String> attrMap = message.getAttributes();
		convertTextToSpeech(messageString, attrMap);
	}

	/** Demonstrates using the Text-to-Speech API. */
	public void convertTextToSpeech(String message, Map<String, String> attrMap) throws Exception {

		String targetLanguage = attrMap.get("target_language");
		String fileName = attrMap.get("file_name");
		String bucket = attrMap.get("bucket_name");

		// Instantiates a client
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

			ByteString audioContents = null;
//			SynthesisInput input = null;
			SynthesizeSpeechResponse response = null;

			// Build the voice request, select the language code and the ssml voice gender
			VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode(targetLanguage)
					.setSsmlGender(SsmlVoiceGender.NEUTRAL).build();

			// Select the type of audio file you want returned
			AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

			if (message.length() < 4900) {
				// Set the text input to be synthesized
				SynthesisInput input = SynthesisInput.newBuilder().setText(message).build();

				// Perform the text-to-speech request on the text input with the selected voice
				// parameters and audio file type
				response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

				// Get the audio contents from the response
				audioContents = response.getAudioContent();

			} else {
				int size = 4900;

//				String[] inputTokens = message.split("(?<=\\G.{" + size + "})");
//
//				for (int i = 0; i < inputTokens.length; i++) {
//					logger.info("Message substring " + i + " length is: " + inputTokens[i].length());
//					logger.info("Message substring " + i + " is: " + inputTokens[i]);
//
//					// Set the text input to be synthesized
//					input = SynthesisInput.newBuilder().setText(inputTokens[i]).build();
//
//					// Perform the text-to-speech request on the text input with the selected voice
//					// parameters and audio file type
//					response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
//
//					// Get the audio contents from the response
//					if (audioContents != null) {
//						audioContents = audioContents.concat(response.getAudioContent());
//					} else {
//						audioContents = response.getAudioContent();
//					}
//
//				}

				int index = 0;
				int endIndex = 0;
				String inputToken = "";
				int inputTokenIndex;
				String inputTokenSubstring = "";
				while (index < message.length()) {

//					SynthesisInput input = null;
					response = null;
					logger.info(
							"This is a long message. Process it by splitting it into substrings of permissible length");
					endIndex = Math.min(index + size, message.length());
//					inputToken = message.substring(index, endIndex);
					inputToken = new Segment(message.toCharArray(), index, endIndex).toString();
					if (endIndex < message.length()) {
						inputTokenIndex = inputToken.lastIndexOf(".");
					} else {
						inputTokenIndex = endIndex - 1;
					}
//					inputTokenSubstring = inputToken.substring(index, inputTokenIndex + 1);
					inputTokenSubstring = new Segment(inputToken.toCharArray(), index, inputTokenIndex + 1).toString();
					logger.info("Message substring of length " + inputTokenSubstring.length() + " characters is: "
							+ inputTokenSubstring);

					// Set the text input to be synthesized
					SynthesisInput input = SynthesisInput.newBuilder().setText(inputTokenSubstring).build();

					// Perform the text-to-speech request on the text input with the selected voice
					// parameters and audio file type
					response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
					logger.info("Returned from synthesizeSpeech");

					// Get the audio contents from the response
					if (audioContents != null) {
						audioContents = audioContents.concat(response.getAudioContent());
					} else {
						audioContents = response.getAudioContent();
					}

					index += inputTokenIndex + 1;
				}

			}

			String fileNameNoExt = Files.getNameWithoutExtension(fileName);
//			String objName = "Translated_" + fileNameNoExt + ".mp3";
			String objName = fileNameNoExt + ".mp3";

			byte[] content = audioContents.toByteArray();
			Storage storage = CommonUtils.getStorageClient();
			uploadTranslatedFileToDestBucket(storage, GCLOUD_STORAGE_OUTPUT_BUCKET, objName, content);
			deleteOriginalFileFromSourceBucket(storage, bucket, fileName);
		}
	}

	public void uploadTranslatedFileToDestBucket(Storage storage, String bucketName, String objectName,
			byte[] content) {

		try {
			Bucket bucket = storage.get(bucketName);
			bucket.create(objectName, content);
			BlobId blobId = BlobId.of(bucketName, objectName);
			storage.createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
			logger.info("Uploaded to bucket " + bucketName + " as " + objectName);
		} catch (Exception ex) {
			logger.info("Couldn't upload the object " + objectName + " to bucket " + bucketName + " due to: "
					+ ex.getMessage());
		}

	}

	public void deleteOriginalFileFromSourceBucket(Storage storage, String bucketName, String objectName) {

		try {
			storage.delete(bucketName, objectName);
			logger.info("Object " + objectName + " was deleted from " + bucketName);
		} catch (Exception ex) {
			logger.info("Couldn't delete the object due to: " + ex.getMessage());
		}

	}

	public void segmentString(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
		if ((pos < 0) || ((pos + len) > doc.getLength())) {
			throw new BadLocationException("ConvertTextToSpeech.segmentString", pos);
		}

		Segment data = new Segment();
		int nleft = len;
		int offs = pos;

		while (nleft > 0) {
			int n = Math.min(nleft, 4096);
			doc.getText(offs, n, data);
			out.write(data.array, data.offset, data.count);
			offs += n;
			nleft -= n;
		}
	}

}
