package com.snapchat.launchpad.mpc.config;


import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MpcBatchConfig {

    @JsonProperty("publisher-url-config")
    private String publisherUrlConfig;

    @JsonProperty("publisher-url-job")
    private String publisherUrlJob;

    @JsonProperty("image-name")
    private String imageName;

    @JsonProperty("timeout-seconds")
    private int timeoutSeconds;

    public String getPublisherUrlConfig() {
        return publisherUrlConfig;
    }

    public void setPublisherUrlConfig(String publisherUrlConfig) {
        this.publisherUrlConfig = publisherUrlConfig;
    }

    public String getPublisherUrlJob() {
        return publisherUrlJob;
    }

    public void setPublisherUrlJob(String publisherUrlJob) {
        this.publisherUrlJob = publisherUrlJob;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
