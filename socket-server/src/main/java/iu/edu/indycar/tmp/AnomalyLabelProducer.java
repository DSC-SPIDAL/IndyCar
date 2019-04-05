package iu.edu.indycar.tmp;

import iu.edu.indycar.models.AnomalyLabel;

import java.util.ArrayList;
import java.util.List;

public class AnomalyLabelProducer {

    private static final String LABEL_STARTUP = "Startup";
    private static final String LABEL_PITSTOP_SLOWDOWN = "Pitstop Slowdown";
    private static final String LABEL_PITSTOP_SPEEDUP = "Pitstop Speedup";
    private static final String LABEL_OTHER = "Unknown";

    private List<Double> pastSpeeds = new ArrayList<>(10);

    private AnomalyLabel anomalyLabel;

    private int getSpeedDirection() {
        int direction = 0;
        for (int i = 0; i < pastSpeeds.size() - 1; i++) {
            direction += Double.compare(pastSpeeds.get(i), pastSpeeds.get(i + 1));
        }
        return direction;
    }

    public AnomalyLabel getLabel(double distance, double rpm, double speed) {
        if (pastSpeeds.size() >= 10) {
            pastSpeeds.remove(0);
        }
        pastSpeeds.add(speed);
        int speedDirection = this.getSpeedDirection();
        String detecetedLabel = null;
        if (rpm <= 3 &&
                distance >= 3500 &&
                speedDirection >= 8 &&
                speed <= 185) {
            detecetedLabel = LABEL_PITSTOP_SLOWDOWN;
        } else if (rpm <= 3 &&
                distance >= 0 && distance <= 1100 &&
                speedDirection <= -4 &&
                speed <= 185) {
            detecetedLabel = LABEL_PITSTOP_SPEEDUP;
        }

        if (detecetedLabel != null) {
            if (anomalyLabel == null || !anomalyLabel.getLabel().equals(detecetedLabel)) {
                anomalyLabel = new AnomalyLabel();
                anomalyLabel.setLabel(detecetedLabel);
            }
            return anomalyLabel;
        } else {
            anomalyLabel = null;
        }
        return null;
    }
}
