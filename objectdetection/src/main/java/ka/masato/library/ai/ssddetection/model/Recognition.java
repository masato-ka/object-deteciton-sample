package ka.masato.library.ai.ssddetection.model;

import android.graphics.RectF;

public class Recognition {

    private final String id;
    private final String title;
    private final Float confidence;
    private final RectF location;

    public Recognition(
            final String id, final String title, final float confidence, final RectF location) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence == null ? 0f : confidence;
    }

    public RectF getLocation(){return location;}

    @Override
    public String toString() {
        String resultString = "";
        if (id != null) {
            resultString += "[" + id + "] ";
        }

        if (title != null) {
            resultString += title + " ";
        }

        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f);
        }

        return resultString.trim();
    }

}

