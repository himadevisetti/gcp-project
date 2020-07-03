package com.myapp.source;

import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.protobuf.ByteString;
import com.myapp.pojos.UserData;

public class ConvertSpeechToText {
	private static final Logger logger = Logger.getLogger(ConvertSpeechToText.class.getName());
	
	/** Demonstrates using the Speech API to transcribe an audio file. */
	public String convert(UserData userData, byte[] bytes) throws Exception {
		// Instantiates a client
		try (SpeechClient speechClient = SpeechClient.create()) {

			ByteString audioBytes = ByteString.copyFrom(bytes);
			logger.info("Source language from userdata: " + userData.getSource_language()); 
			// Builds the sync recognize request
			RecognitionConfig config = RecognitionConfig.newBuilder().setEncoding(AudioEncoding.LINEAR16)
					.setSampleRateHertz(16000).setLanguageCode(userData.getSource_language()).build();
			//		.setSampleRateHertz(8000).setLanguageCode("en-US").build();
			RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

			// Performs speech recognition on the audio file
			RecognizeResponse response = speechClient.recognize(config, audio);
			List<SpeechRecognitionResult> results = response.getResultsList();

			StringBuffer sb = new StringBuffer(); 
			for (SpeechRecognitionResult result : results) {
				// There can be several alternative transcripts for a given chunk of speech.
				// Just use the first (most likely) one here.
				SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
				sb.append(alternative.getTranscript()); 
			}
			
			return sb.toString(); 
		}
	}

}
