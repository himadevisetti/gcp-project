#!/bin/bash

echo "Making bucket: gs://$DEVSHELL_PROJECT_ID-media"
gsutil mb gs://exotic_place

echo "Exporting GCLOUD_PROJECT and GCLOUD_BUCKET"
export GCLOUD_PROJECT=teak-mantis-279104
export GCLOUD_BUCKET=gs://exotic_place

echo "Deploying java-gcs-function"
gcloud functions deploy java-gcs-function --entry-point com.myapp.source.MonitorCloudStorage --runtime java11 --memory 512MB --trigger-resource exotic_place --trigger-event google.storage.object.finalize

echo "Deploying java-pubsub-translate-function"
gcloud functions deploy java-pubsub-translate-function --entry-point com.myapp.source.TranslateText --runtime java11 --memory 512MB --trigger-topic translation

echo "Deploying java-pubsub-write-function"
gcloud functions deploy java-pubsub-write-function --entry-point com.myapp.source.ConvertTextToSpeech --runtime java11 --memory 512MB --trigger-topic filewriter

echo "Deploying java-pubsub-getuserdata-function"
gcloud functions deploy java-pubsub-getuserdata-function --entry-point com.myapp.source.GetUserData --runtime java11 --memory 512MB --trigger-topic userdata
