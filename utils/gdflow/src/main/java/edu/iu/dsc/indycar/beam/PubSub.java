package edu.iu.dsc.indycar.beam;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PubSub {
  public static void main(String[] args) throws IOException {

    try {


      ProjectTopicName topicName = ProjectTopicName.of("dataflow-test-270217", "telemetry");
      // Set the channel and credentials provider when creating a `Publisher`.
      // Similarly for Subscriber
      Publisher publisher =
          Publisher.newBuilder(topicName)
              .build();

      ApiFuture<String> publish = publisher.publish(PubsubMessage.newBuilder()
          .setData(ByteString.copyFrom("0,1,10,11,12".getBytes())).build());
      ApiFutures.addCallback(publish, new ApiFutureCallback<String>() {
        @Override
        public void onFailure(Throwable throwable) {
          throwable.printStackTrace();
        }

        @Override
        public void onSuccess(String s) {
          System.out.println(s);
        }
      }, MoreExecutors.directExecutor());

      publisher.shutdown();
      publisher.awaitTermination(1, TimeUnit.HOURS);
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
