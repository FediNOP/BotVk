package com.petersamokhin.bots.sdk.objects;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Button {

    String playLoad, label;
    Color color = Color.Default;

    public Button() {

    }

    public Button setColor(Color color) {
        this.color = color;
        return this;
    }

    public Button setLabel(String label) {
        this.label = label;
        return this;
    }

    public Button setPlayLoad(String playLoad) {
        this.playLoad = playLoad;
        return this;
    }

    private static final String token = "8d670a914cb1ff1f67938246bd5e673320f4286e8c1856e00d9f48eeac5846b1f48476cfee81f2287b229";
    private static final int id = 171773847;

    public JSONObject getJSON() {
        JSONObject button = new JSONObject();
        JSONObject action = new JSONObject();
        action.put("type", "text");

        if (playLoad != null) {
            JSONObject payload = new JSONObject();
            payload.put("button", playLoad);
            action.put("payload", payload.toString());
        }
        action.put("label", label);

        button.put("action", action);
        button.put("color", color.toString());
        return button;
    }
}
