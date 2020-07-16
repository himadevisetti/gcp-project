package com.myapp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.contrib.nio.CloudStorageFileSystem;

public class CommonUtils {

	private static final Logger logger = Logger.getLogger(CommonUtils.class.getName());
	final static String GOOGLE_CLOUD_PROJECT = System.getenv("GOOGLE_CLOUD_PROJECT");
	final static String GCLOUD_SERVICE_ACCOUNT = System.getenv("GCLOUD_SERVICE_ACCOUNT");

	/**
	 * String -> Path. This *should* not be necessary (use
	 * Paths.get(URI.create(...)) instead) , but it currently is on Spark because
	 * using the fat, shaded jar breaks the registration of the GCS
	 * FilesystemProvider. To transform other types of string URLs into Paths, use
	 * IOUtils.getPath instead.
	 */
	public static Path getPathOnGcs(String gcsUrl) {
		// use a split limit of -1 to preserve empty split tokens, especially trailing
		// slashes on directory names
		final String[] split = gcsUrl.split("/", -1);
		final String BUCKET = split[2];
		final String pathWithoutBucket = String.join("/", Arrays.copyOfRange(split, 3, split.length));
		return CloudStorageFileSystem.forBucket(BUCKET).getPath(pathWithoutBucket);
	}

	public static ServiceAccountCredentials getServiceAccountCredentials() {
//		Path path = Paths.get(URI.create(GCLOUD_SERVICE_ACCOUNT));
		Path path = CommonUtils.getPathOnGcs(GCLOUD_SERVICE_ACCOUNT);
		InputStream fis;
		ServiceAccountCredentials credentials = null;
		try {
			fis = Files.newInputStream(path);
			credentials = ServiceAccountCredentials.fromStream(fis);
		} catch (IOException ex) {
			logger.info(ex.getMessage());
		}
		return credentials;
	}

	public static Storage getStorageClient() throws IOException {
		Storage storage = StorageOptions.newBuilder().setCredentials(getServiceAccountCredentials())
				.setProjectId(GOOGLE_CLOUD_PROJECT).build().getService();
		return storage;
	}

	public static Firestore getFirestoreClient() throws IOException {
		Firestore firestore = FirestoreOptions.newBuilder().setCredentials(getServiceAccountCredentials())
				.setProjectId(GOOGLE_CLOUD_PROJECT).build().getService();
		return firestore;
	}

}
