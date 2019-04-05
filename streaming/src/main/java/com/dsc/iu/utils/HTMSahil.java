package com.dsc.iu.utils;

/* ---------------------------------------------------------------------
 * Numenta Platform for Intelligent Computing (NuPIC)
 * Copyright (C) 2014, Numenta, Inc.  Unless you have an agreement
 * with Numenta, Inc., for a separate license for this software code, the
 * following terms and conditions apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 *
 * http://numenta.org/licenses/
 * ---------------------------------------------------------------------
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.numenta.nupic.Parameters;
import org.numenta.nupic.Parameters.KEY;
import org.numenta.nupic.algorithms.Anomaly;
import org.numenta.nupic.algorithms.AnomalyLikelihood;
import org.numenta.nupic.algorithms.SpatialPooler;
import org.numenta.nupic.algorithms.TemporalMemory;
import org.numenta.nupic.network.Inference;
import org.numenta.nupic.network.Layer;
import org.numenta.nupic.network.Network;
import org.numenta.nupic.network.PublisherSupplier;
import org.numenta.nupic.network.Region;
import org.numenta.nupic.network.sensor.ObservableSensor;
import org.numenta.nupic.network.sensor.Publisher;
import org.numenta.nupic.network.sensor.Sensor;
import org.numenta.nupic.network.sensor.SensorParams;
import org.numenta.nupic.util.Tuple;
import org.numenta.nupic.util.UniversalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo.None;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class HTMSahil {
    protected static final Logger LOGGER = LoggerFactory.getLogger(HTMSahil.class);

    private Network network;

    private PublisherSupplier supplier;
    private static double prev_likelihood;

    /**
     * Create HTM Model to be used by NAB
     * @param modelParams OPF Model parameters to parameters from
     */
    public HTMSahil(JsonNode modelParams) {
        LOGGER.trace("HTMModel({})", modelParams);

        // Create Sensor publisher to push NAB input data to network
        supplier = PublisherSupplier.builder()
                .addHeader("timestamp,value")
                .addHeader("datetime,float")
                .addHeader("T,B")
                .build();

        // Get updated model parameters
        Parameters parameters = getModelParameters(modelParams);
        
        LOGGER.info("RUNNING WITH NO EXPLICIT P_RADIUS SET");

        // Create NAB Network
        network = Network.create("NAB Network", parameters)
            .add(Network.createRegion("NAB Region")
                .add(Network.createLayer("NAB Layer", parameters)
                    .add(Anomaly.create())
                    .add(new TemporalMemory())
                    .add(new SpatialPooler())
                    .add(Sensor.create(ObservableSensor::create,
                            SensorParams.create(SensorParams.Keys::obs, "Manual Input", supplier)))));
        
        prev_likelihood = 0.;
    }

    /**
     * Update encoders parameters
     * @param modelParams OPF Model parameters to get encoder parameters from
     * @return Updated Encoder parameters suitable for {@link Parameters.KEY.FIELD_ENCODING_MAP}
     */
    public Map<String, Map<String, Object>> getFieldEncodingMap(JsonNode modelParams) {
        Map<String, Map<String, Object>> fieldEncodings = new HashMap<>();
        String fieldName;
        Map<String, Object> fieldMap;
        JsonNode encoders = modelParams.path("encoders");
        LOGGER.trace("getFieldEncodingMap({})", encoders);
        for (JsonNode node : encoders) {
            if (node.isNull())
                continue;

            fieldName = node.path("fieldname").textValue();
            fieldMap = fieldEncodings.get(fieldName);
            if (fieldMap == null) {
                fieldMap = new HashMap<>();
                fieldMap.put("fieldName", fieldName);
                fieldEncodings.put(fieldName, fieldMap);
            }
            fieldMap.put("encoderType", node.path("type").textValue());
            if (node.has("timeOfDay")) {
                JsonNode timeOfDay = node.get("timeOfDay");
                fieldMap.put("fieldType", "datetime");
                //fieldMap.put(KEY.DATEFIELD_PATTERN.getFieldName(), "YYYY-MM-dd HH:mm:ss.SSS");
                fieldMap.put(KEY.DATEFIELD_PATTERN.getFieldName(), "YYYY-MM-dd HH:mm:ss");
                fieldMap.put(KEY.DATEFIELD_TOFD.getFieldName(),
                        new Tuple(timeOfDay.get(0).asInt(), timeOfDay.get(1).asDouble()));
            } else {
                fieldMap.put("fieldType", "float");
            }
            if (node.has("resolution")) {
                fieldMap.put("resolution", node.get("resolution").asDouble());
            }
        }
        LOGGER.trace("getFieldEncodingMap => {}", fieldEncodings);
        return fieldEncodings;
    }

    /**
     * Update Spatial Pooler parameters
     * @param modelParams OPF Model parameters to get spatial pooler parameters from
     * @return Updated Spatial Pooler parameters
     */
    public Parameters getSpatialPoolerParams(JsonNode modelParams) {
        Parameters p = Parameters.getSpatialDefaultParameters();
        JsonNode spParams = modelParams.path("spParams");
        LOGGER.trace("getSpatialPoolerParams({})", spParams);
        if (spParams.has("columnCount")) {
            p.set(KEY.COLUMN_DIMENSIONS, new int[]{spParams.get("columnCount").asInt()});
        }
        if (spParams.has("maxBoost")) {
            p.set(KEY.MAX_BOOST, spParams.get("maxBoost").asDouble());
        }
        if (spParams.has("synPermInactiveDec")) {
            p.set(KEY.SYN_PERM_INACTIVE_DEC, spParams.get("synPermInactiveDec").asDouble());
        }
        if (spParams.has("synPermConnected")) {
            p.set(KEY.SYN_PERM_CONNECTED, spParams.get("synPermConnected").asDouble());
        }
        if (spParams.has("synPermActiveInc")) {
            p.set(KEY.SYN_PERM_ACTIVE_INC, spParams.get("synPermActiveInc").asDouble());
        }
        if (spParams.has("numActiveColumnsPerInhArea")) {
            p.set(KEY.NUM_ACTIVE_COLUMNS_PER_INH_AREA, spParams.get("numActiveColumnsPerInhArea").asDouble());
        }
        if (spParams.has("globalInhibition")) {
            p.set(KEY.GLOBAL_INHIBITION, spParams.get("globalInhibition").asBoolean());
        }
        if (spParams.has("potentialPct")) {
            p.set(KEY.POTENTIAL_PCT, spParams.get("potentialPct").asDouble());
        }

        LOGGER.trace("getSpatialPoolerParams => {}", p);
        return p;
    }

    /**
     * Update Temporal Memory parameters
     * @param modelParams OPF Model parameters to get Temporal Memory parameters from
     * @return Updated Temporal Memory parameters
     */
    public Parameters getTemporalMemoryParams(JsonNode modelParams) {
        Parameters p = Parameters.getTemporalDefaultParameters();
        JsonNode tpParams = modelParams.path("tpParams");
        LOGGER.trace("getTemporalMemoryParams({})", tpParams);
        if (tpParams.has("columnCount")) {
            p.set(KEY.COLUMN_DIMENSIONS, new int[]{tpParams.get("columnCount").asInt()});
        }
        if (tpParams.has("activationThreshold")) {
            p.set(KEY.ACTIVATION_THRESHOLD, tpParams.get("activationThreshold").asInt());
        }
        if (tpParams.has("cellsPerColumn")) {
            p.set(KEY.CELLS_PER_COLUMN, tpParams.get("cellsPerColumn").asInt());
        }
        if (tpParams.has("permanenceInc")) {
            p.set(KEY.PERMANENCE_INCREMENT, tpParams.get("permanenceInc").asDouble());
        }
        if (tpParams.has("minThreshold")) {
            p.set(KEY.MIN_THRESHOLD, tpParams.get("minThreshold").asInt());
        }
        if (tpParams.has("initialPerm")) {
            p.set(KEY.INITIAL_PERMANENCE, tpParams.get("initialPerm").asDouble());
        }
        if(tpParams.has("maxSegmentsPerCell")) {
            p.set(KEY.MAX_SEGMENTS_PER_CELL, tpParams.get("maxSegmentsPerCell").asInt());
        }
        if(tpParams.has("maxSynapsesPerSegment")) {
            p.set(KEY.MAX_SYNAPSES_PER_SEGMENT, tpParams.get("maxSynapsesPerSegment").asInt());
        }
        if (tpParams.has("permanenceDec")) {
            p.set(KEY.PERMANENCE_DECREMENT, tpParams.get("permanenceDec").asDouble());
        }
        if (tpParams.has("predictedSegmentDecrement")) {
            p.set(KEY.PREDICTED_SEGMENT_DECREMENT, tpParams.get("predictedSegmentDecrement").asDouble());
        }
        if (tpParams.has("newSynapseCount")) {
            p.set(KEY.MAX_NEW_SYNAPSE_COUNT, tpParams.get("newSynapseCount").intValue());
        }

        LOGGER.trace("getTemporalMemoryParams => {}", p);
        return p;
    }

    /**
     * Update Sensor parameters
     * @param modelParams OPF Model parameters to get Sensor parameters from
     * @return Updated Sensor parameters
     */
    public Parameters getSensorParams(JsonNode modelParams) {
        JsonNode sensorParams = modelParams.path("sensorParams");
        LOGGER.trace("getSensorParams({})", sensorParams);
        Map<String, Map<String, Object>> fieldEncodings = getFieldEncodingMap(sensorParams);
        Parameters p = Parameters.empty();
        p.set(KEY.CLIP_INPUT, true);
        p.set(KEY.FIELD_ENCODING_MAP, fieldEncodings);

        LOGGER.trace("getSensorParams => {}", p);
        return p;
    }

    /**
     * Update NAB parameters
     * @param params OPF parameters to get NAB model parameters from
     * @return Updated Model parameters
     */
    public Parameters getModelParameters(JsonNode params) {
        JsonNode modelParams = params.path("modelParams");
        LOGGER.trace("getModelParameters({})", modelParams);
        Parameters p = Parameters.getAllDefaultParameters()
            .union(getSpatialPoolerParams(modelParams))
            .union(getTemporalMemoryParams(modelParams))
            .union(getSensorParams(modelParams));
        
        // TODO https://github.com/numenta/htm.java/issues/482
        // if (spParams.has("seed")) {
        //     p.set(KEY.SEED, spParams.get("seed").asInt());
        // }
        p.set(KEY.RANDOM, new UniversalRandom(42));
        // Setting the random above is done as a work-around to this.
        //p.set(KEY.SEED, 42);
        
        LOGGER.trace("getModelParameters => {}", p);
        return p;
    }

    public Publisher getPublisher() {
        return supplier.get();
    }

    public Network getNetwork() {
        return network;
    }

    /**
     * Launch htm.java NAB detector
     *
     * Usage:
     *      As a standalone application (for debug purpose only):
     *
     *          java -jar htm.java-nab.jar "{\"modelParams\":{....}}" < nab_data.csv > anomalies.out
     *
     *      For complete list of command line options use:
     *
     *          java -jar htm.java-nab.jar --help
     *
     *      As a NAB detector (see 'htmjava_detector.py'):
     *
     *          python run.py --detect --score --normalize -d htmjava
     *
     *      Logging options, see "log4j.properties":
     *
     *          - "LOGLEVEL": Controls log output (default: "OFF")
     *          - "LOGGER": Either "CONSOLE" or "FILE" (default: "CONSOLE")
     *          - "LOGFILE": Log file destination (default: "htmjava.log")
     *
     *      For example:
     *
     *          java -DLOGLEVEL=TRACE -DLOGGER=FILE -jar htm.java-nab.jar "{\"modelParams\":{....}}" < nab_data.csv > anomalies.out
     *
     */
    @SuppressWarnings("resource")
    public static void main(String[] args) {
        try {
//            LOGGER.trace("main({})",  Arrays.asList(args));
        		System.out.println("in main..");
        		String arg1 = "{\"aggregationInfo\": {\"seconds\": 0, \"fields\": [], \"months\": 0, \"days\": 0, \"years\": 0, \"hours\": 0, \"microseconds\": 0, \"weeks\": 0, \"minutes\": 0, \"milliseconds\": 0}, \"model\": \"HTMPrediction\", \"version\": 1, \"predictAheadTime\": null, \"modelParams\": {\"sensorParams\": {\"sensorAutoReset\": null, \"encoders\": {\"value\": {\"name\": \"value\", \"resolution\": 2.5143999999999997, \"seed\": 42, \"fieldname\": \"value\", \"type\": \"RandomDistributedScalarEncoder\"}, \"timestamp_dayOfWeek\": null, \"timestamp_timeOfDay\": {\"fieldname\": \"timestamp\", \"timeOfDay\": [21, 9.49], \"type\": \"DateEncoder\", \"name\": \"timestamp\"}, \"timestamp_weekend\": null}, \"verbosity\": 0}, \"anomalyParams\": {\"anomalyCacheRecords\": null, \"autoDetectThreshold\": null, \"autoDetectWaitRecords\": 5030}, \"spParams\": {\"columnCount\": 2048, \"synPermInactiveDec\": 0.0005, \"spatialImp\": \"cpp\", \"inputWidth\": 0, \"spVerbosity\": 0, \"synPermConnected\": 0.2, \"synPermActiveInc\": 0.003, \"potentialPct\": 0.8, \"numActiveColumnsPerInhArea\": 40, \"boostStrength\": 0.0, \"globalInhibition\": 1, \"seed\": 1956}, \"trainSPNetOnlyIfRequested\": false, \"clParams\": {\"alpha\": 0.035828933612158, \"verbosity\": 0, \"steps\": \"1\", \"regionName\": \"SDRClassifierRegion\"}, \"tmParams\": {\"columnCount\": 2048, \"activationThreshold\": 20, \"cellsPerColumn\": 32, \"permanenceDec\": 0.008, \"minThreshold\": 13, \"inputWidth\": 2048, \"maxSynapsesPerSegment\": 128, \"outputType\": \"normal\", \"initialPerm\": 0.24, \"globalDecay\": 0.0, \"maxAge\": 0, \"newSynapseCount\": 31, \"maxSegmentsPerCell\": 128, \"permanenceInc\": 0.04, \"temporalImp\": \"tm_cpp\", \"seed\": 1960, \"verbosity\": 0, \"predictedSegmentDecrement\": 0.001}, \"tmEnable\": true, \"clEnable\": false, \"spEnable\": true, \"inferenceType\": \"TemporalAnomaly\"}}";
        	
            // Parse command line args
            OptionParser parser = new OptionParser();
            parser.nonOptions("OPF parameters object (JSON)");
            parser.acceptsAll(Arrays.asList("p", "params"), "OPF parameters file (JSON).\n(default: first non-option argument)")
                .withOptionalArg()
                .ofType(File.class);
            parser.acceptsAll(Arrays.asList("i", "input"), "Input data file (csv).\n(default: stdin)")
                .withOptionalArg()
                .ofType(File.class);
            parser.acceptsAll(Arrays.asList("o", "output"), "Output results file (csv).\n(default: stdout)")
                .withOptionalArg()
                .ofType(File.class);
            parser.acceptsAll(Arrays.asList("s", "skip"), "Header lines to skip")
                .withOptionalArg()
                .ofType(Integer.class)
                .defaultsTo(0);
            parser.acceptsAll(Arrays.asList("h", "?", "help"), "Help");
            OptionSet options = parser.parse(arg1);
//            if (args.length == 0 || options.has("h")) {
//                parser.printHelpOn(System.out);
//                return;
//            }

            // Get in/out files
//            final PrintStream output;
//            final InputStream input;
//            if (options.has("i")) {
//                input = new FileInputStream((File)options.valueOf("i"));
//            } else {
//                input = System.in;
//            }
            
            
//            FileInputStream inp = new FileInputStream(new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/htmjava_indy2018-13-vspeed-inout.csv"));
            FileInputStream inp = new FileInputStream(new File("/scratch_ssd/sahil/"+args[0]));
//            if (options.has("o")) {
//                output = new PrintStream((File)options.valueOf("o"));
//            } else {
//                output = System.out;
//            }
            
//            File infile = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/speed-13/DATASAHIL.csv");
//            PrintWriter inpw = new PrintWriter(infile);
//            File outfile = new File("/Users/sahiltyagi/Desktop/benchmarks/HTMjava/modifiedHTMparams/speed-13/SCORESAHIL.csv");
//            PrintWriter outpw = new PrintWriter(outfile);
            
            File infile = new File("/scratch_ssd/sahil/data-13-" + args[1] + ".csv");
            PrintWriter inpw = new PrintWriter(infile);
            File outfile = new File("/scratch_ssd/sahil/inference-13-" + args[1] + ".csv");
            PrintWriter outpw = new PrintWriter(outfile);

            // Parse OPF Model Parameters
            JsonNode params;
            ObjectMapper mapper = new ObjectMapper();
            if (options.has("p")) {
                params = mapper.readTree((File)options.valueOf("p"));
            } 
            else if (options.nonOptionArguments().isEmpty()) {
                try { inp.close(); }catch(Exception ignore) {}
                if(options.has("o")) {
                    try { outpw.flush(); outpw.close(); }catch(Exception ignore) {}
                }
                throw new IllegalArgumentException("Expecting OPF parameters. See 'help' for more information");
            } else {
                params = mapper.readTree((String)options.nonOptionArguments().get(0));
            }

            // Number of header lines to skip
            int skip = (int) options.valueOf("s");

            // Force timezone to UTC
            DateTimeZone.setDefault(DateTimeZone.UTC);
            
            //anomaly likelihood calculation
            //AnomalyLikelihood likelihood = new AnomalyLikelihood(false, 375, false, 375, 375);
           AnomalyLikelihood likelihood = new AnomalyLikelihood(true, 8640, false, 375, 375);
            
            // Create NAB Network Model
            System.out.println("going to start htm..");
            HTMSahil model = new HTMSahil(params);
            Network network = model.getNetwork();
            network.observe().subscribe((Inference inference) -> {
                double score = inference.getAnomalyScore();
                int record = inference.getRecordNum();
                double value = (Double)inference.getClassifierInput().get("value").get("inputValue");
                DateTime timestamp = (DateTime)inference.getClassifierInput().get("timestamp").get("inputValue");
                
                
                double anomaly_likelihood = likelihood.anomalyProbability(value, score, timestamp);
                if (anomaly_likelihood >=0.99999 && prev_likelihood >= 0.99999){
                    prev_likelihood = anomaly_likelihood;
                    anomaly_likelihood = 0.999;
                }
                else{
                    prev_likelihood = anomaly_likelihood;
                }
                
                double logscore = AnomalyLikelihood.computeLogLikelihood(anomaly_likelihood);
                //System.out.println("13," + (record + 1) + ",speed," + value + "," + logscore   );
                LOGGER.trace("record = {}, score = {}", record, score);
                
                // Output log likelihood anomaly score
                outpw.write("20," + (record + 1) + ",speed," + value + "," + logscore + "," + System.currentTimeMillis() + "\n");
                //outpw.flush();
               
            }, (error) -> {
                LOGGER.error("Error processing data", error);
            }, () -> {
                LOGGER.trace("Done processing data");
            });
            network.start();

            // Pipe data to network
            Publisher publisher = model.getPublisher();
            BufferedReader in = new BufferedReader(new InputStreamReader(inp));
            String line;
            int ctr=0;
            
            while ((line = in.readLine()) != null && line.trim().length() > 0) {
                // Skip header lines
                if (skip > 0) {
                    skip--;
                    continue;
                }
//                publisher.onNext(line.split(",")[0] + "," + line.split(",")[1]);
                ctr++;
                publisher.onNext(line.split(",")[0].substring(0, line.split(",")[0].length()-4) + "," + line.split(",")[1]);
                inpw.write(line.split(",")[0].substring(0, line.split(",")[0].length()-4) + "," + line.split(",")[1] + "," + ctr + "," + System.currentTimeMillis() + "\n");
                //inpw.flush();
                try {
                	 Thread.sleep(10);
                } catch(InterruptedException i) {}
            }
        
            publisher.onComplete();
            in.close();
            LOGGER.trace("Done publishing data");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
