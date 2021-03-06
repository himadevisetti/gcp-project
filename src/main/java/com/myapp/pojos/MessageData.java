package com.myapp.pojos;

public class MessageData {

	private String text; 
	private String document_id; 
	private String source_language;
	private String target_language;
	private String bucket_name; 
	private String file_name;
	private String public_url;	
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getDocument_id() {
		return document_id;
	}

	public void setDocument_id(String document_id) {
		this.document_id = document_id;
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

	@Override
    public String toString() {
        return "document_id=" + document_id + ","
        	   + "source_language=" + source_language + ","
               + "target_language=" + target_language + ","
               + "bucket_name=" + bucket_name + "," 
               + "file_name=" + file_name + ","
               + "public_url=" + public_url;

    }

}
