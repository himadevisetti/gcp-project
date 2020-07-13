package com.myapp.source;

import java.util.List;
import java.util.logging.Logger;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.myapp.pojos.AudioMetadata;
import com.myapp.pojos.UserData;

public class ConvertSpeechToText {
	private static final Logger logger = Logger.getLogger(ConvertSpeechToText.class.getName());

	/** Demonstrates using the Speech API to transcribe an audio file. */
	public String convert(UserData userData, byte[] bytes) throws Exception {
		
		// Extracts audio metadata
		String metadata = userData.getMetadata();
		Gson gson = new Gson();
		AudioMetadata audioMetadata = gson.fromJson(metadata, AudioMetadata.class);
		
		// Instantiates a client
		try (SpeechClient speechClient = SpeechClient.create()) {

			ByteString audioBytes = ByteString.copyFrom(bytes);

			String fileName = userData.getFile_name();
			String bucketName = userData.getBucket_name();
			String sourceLanguage = userData.getSource_language();

			String audioFormat = audioMetadata.getFormat().toUpperCase(); 
			int audioChannelCount = Integer.parseInt(audioMetadata.getChannelCount()); 
			int sampleRate = Integer.parseInt(audioMetadata.getSampleRate()); 
			boolean enableSeparateRecognitionPerChannel = false; 
			if (audioChannelCount > 1) {
				enableSeparateRecognitionPerChannel = true; 
			}
			double duration = Double.parseDouble(audioMetadata.getDuration()); 
			
			
			boolean ignoreSomeMetaData = false;
			if (audioFormat.equals("FLAC") || audioFormat.equals("WAV")) {
				ignoreSomeMetaData = true;
			}

			// Builds the sync recognize request
			
			// Builds the configuration
			RecognitionConfig config = null;

			if (ignoreSomeMetaData) {
				logger.info("It is a FLAC/ WAV file");
				if (audioFormat.equals("FLAC")) {
					config = RecognitionConfig.newBuilder()
							.setEncoding(AudioEncoding.FLAC)
							.setAudioChannelCount(audioChannelCount)
							.setEnableSeparateRecognitionPerChannel(enableSeparateRecognitionPerChannel)
							.setSampleRateHertz(sampleRate)
							.setLanguageCode(sourceLanguage)
							.build();
				} else {
					config = RecognitionConfig.newBuilder()
							.setEncoding(AudioEncoding.LINEAR16)
							.setAudioChannelCount(audioChannelCount)
							.setEnableSeparateRecognitionPerChannel(enableSeparateRecognitionPerChannel)
							.setSampleRateHertz(sampleRate)
							.setLanguageCode(sourceLanguage)
							.build();
				}

			} else {
				logger.info("Do not ignore Encoding and SampleRateHertz in building the config");
				config = RecognitionConfig.newBuilder()
						.setEncoding(AudioEncoding.LINEAR16)
						.setSampleRateHertz(sampleRate)
						.setLanguageCode(sourceLanguage)
						.build();
			}

			// Builds the content
			RecognitionAudio audio = null;
			List<SpeechRecognitionResult> results = null;

			// Performs speech recognition on the audio file
			try {
				
				if (duration <= 55.0) {
					logger.info("This is not a long audio file");
					audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();
					RecognizeResponse response = speechClient.recognize(config, audio);
					results = response.getResultsList();
				} else {
					logger.info("This is a long audio file");
					String gcsUri = "gs://" + bucketName + "/" + fileName;
					logger.info("gcsUri for the long audio file is: " + gcsUri);
					audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();
					// Use non-blocking call for getting file transcription
					OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> asyncResponse = speechClient
							.longRunningRecognizeAsync(config, audio);
					while (!asyncResponse.isDone()) {
						logger.info("Waiting for response...");
						Thread.sleep(300000);
					}
					results = asyncResponse.get().getResultsList();
				}
				
			} catch (Exception ex) {
				logger.info("Couldn't convert due to: " + ex.getMessage());
//				logger.info("Using Asynchronous speech recognition to proceed further");
//				String EXCEPTION_MSG1 = "INVALID_ARGUMENT: Request payload size exceeds the limit";
//				String EXCEPTION_MSG2 = "INVALID_ARGUMENT: Sync input too long";
//				if (ex.getMessage().contains(EXCEPTION_MSG1) || ex.getMessage().contains(EXCEPTION_MSG2)) {
//					logger.info("This is a long audio file");
//					String gcsUri = "gs://" + bucketName + "/" + fileName;
//					logger.info("gcsUri for the long audio file is: " + gcsUri);
//					audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();
//					// Use non-blocking call for getting file transcription
//					OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> asyncResponse = speechClient
//							.longRunningRecognizeAsync(config, audio);
//					while (!asyncResponse.isDone()) {
//						logger.info("Waiting for response...");
//						Thread.sleep(300000);
//					}
//					results = asyncResponse.get().getResultsList();
//				}
			}

			
			if (results != null) {
				StringBuffer transcript = new StringBuffer();
				for (SpeechRecognitionResult result : results) {
					// There can be several alternative transcripts for a given chunk of speech.
					// Just use the first (most likely) one here.
					SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
					transcript.append(alternative.getTranscript());
				}
				return transcript.toString();
			}
			
			return "Audio file could not be transcribed due to some issue";
		}
	}

}
