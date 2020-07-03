package com.myapp.pojos;

import java.util.Date;

public class UserData {

	private String document_id; 
	private String user_name; 
	private String source_language;
	private String target_language;
	private String bucket_name; 
	private String file_name;
	private String public_url;	
	private Date created; 
	
	public String getDocument_id() {
		return document_id;
	}

	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}
	
	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getSource_language() {
		return source_language;
	}

	public void setSource_language(String source_language) {
		this.source_language = source_language;
	}

	public String getTarget_language() {
		return target_language;
	}

	public void setTarget_language(String target_language) {
		this.target_language = target_language;
	}

	public String getBucket_name() {
		return bucket_name;
	}

	public void setBucket_name(String bucket_name) {
		this.bucket_name = bucket_name;
	}

	public String getFile_name() {
		return file_name;
	}
	
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	
	public String getPublic_url() {
		return public_url;
	}
	
	public void setPublic_url(String public_url) {
		this.public_url = public_url;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
    public String toString() {
        return "document_id=" + document_id + ","
        	   + "user_name=" + user_name + ","
        	   + "source_language=" + source_language + ","
               + "target_language=" + target_language + ","
               + "bucket_name=" + bucket_name + "," 
               + "file_name=" + file_name + ","
               + "public_url=" + public_url + ","
               + "created_time=" + created.toString();

    }

}
