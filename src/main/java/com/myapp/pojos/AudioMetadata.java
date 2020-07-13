package com.myapp.pojos;

public class AudioMetadata {

	private String duration; 
	private String sampleCount; 
	private String channelCount;
	private String sampleRate;
	private String format; 
	private String bitRate;	

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getSampleCount() {
		return sampleCount;
	}

	public void setSampleCount(String sampleCount) {
		this.sampleCount = sampleCount;
	}

	public String getChannelCount() {
		return channelCount;
	}

	public void setChannelCount(String channelCount) {
		this.channelCount = channelCount;
	}

	public String getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(String sampleRate) {
		this.sampleRate = sampleRate;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getBitRate() {
		return bitRate;
	}

	public void setBitRate(String bitRate) {
		this.bitRate = bitRate;
	}

	@Override
    public String toString() {
        return "duration(seconds)=" + duration + ","
        	   + "sampleCount=" + sampleCount + ","
        	   + "channelCount=" + channelCount + ","
               + "sampleRate=" + sampleRate + ","
               + "format=" + format + "," 
               + "bitRate=" + bitRate; 

    }

}
