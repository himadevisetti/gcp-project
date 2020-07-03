package com.myapp.source;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.myapp.pojos.UserData;

public class GetUserData {
	private static final Logger logger = Logger.getLogger(GetUserData.class.getName());

	/**
	 * Queries firestore collection userdata using file_name
	 *
	 * @return UserData
	 */
	public UserData queryUserData(String bucketName, String fileName) throws Exception {

		Firestore db = FirestoreOptions.getDefaultInstance().getService();

		// Create a reference to the userdata collection
		CollectionReference cities = db.collection("userdata");
		
		// Create a query against the collection.
		Query query = cities.whereEqualTo("bucket_name", bucketName).whereEqualTo("file_name", fileName);

		// retrieve query results asynchronously using query.get()
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		
		List<QueryDocumentSnapshot> docs = querySnapshot.get().getDocuments(); 
		UserData userData = new UserData(); 
		logger.info("Number of documents: " + docs.size());
		
		if (docs.size() > 0) {
			QueryDocumentSnapshot qds = docs.get(0); 
			userData = qds.toObject(UserData.class);
			userData.setDocument_id(qds.getId());
		}  
		
		return userData;
	}

	public static void main(String args[]) throws Exception {

		GetUserData ud = new GetUserData();
		UserData user = ud.queryUserData("exotic_place", "audio.raw");
		System.out.println(user);
		
		Map<String, String> attrMap = new Gson().fromJson(new Gson().toJson(user), 
	            new TypeToken<HashMap<String, String>>() {}.getType()
	    );
		
		System.out.println("\nPrinting Attributes: ");
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		System.out.println("Source Language: " + attrMap.get("source_language"));
		System.out.println("Target Language: " + attrMap.get("target_language"));
		System.out.println("File Name: " + attrMap.get("file_name"));
		System.out.println("Created Date: " + attrMap.get("created"));

	}

}
