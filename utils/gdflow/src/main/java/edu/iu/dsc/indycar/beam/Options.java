package edu.iu.dsc.indycar.beam;

import org.apache.beam.sdk.options.*;

public interface Options extends PipelineOptions, StreamingOptions {
  @Description(
      "The Cloud Pub/Sub subscription to consume from. "
          + "The name should be in the format of "
          + "projects/<project-id>/subscriptions/<subscription-name>.")
  @Validation.Required
  ValueProvider<String> getInputSubscription();

  void setInputSubscription(ValueProvider<String> inputSubscription);

  @Description(
      "The Cloud Pub/Sub topic to publish to. "
          + "The name should be in the format of "
          + "projects/<project-id>/topics/<topic-name>.")
  @Validation.Required
  ValueProvider<String> getOutputTopic();

  void setOutputTopic(ValueProvider<String> outputTopic);

  @Description(
      "Filter events based on an optional attribute key. "
          + "No filters are applied if a filterKey is not specified.")
  @Validation.Required
  ValueProvider<String> getFilterKey();

  void setFilterKey(ValueProvider<String> filterKey);

  @Description(
      "Filter attribute value to use in case a filterKey is provided. Accepts a valid Java regex"
          + " string as a filterValue. In case a regex is provided, the complete expression"
          + " should match in order for the message to be filtered. Partial matches (e.g."
          + " substring) will not be filtered. A null filterValue is used by default.")
  @Validation.Required
  ValueProvider<String> getFilterValue();

  void setFilterValue(ValueProvider<String> filterValue);


  /** Set this required option to specify where to write the output. */
  @Description("Path of the file to write to")
  @Validation.Required
  String getOutput();

  void setOutput(String value);
}
