
package com.example.slackchatbot.Models.ChannelJoinAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseMetadata {

    @SerializedName("warnings")
    @Expose
    private List<String> warnings = null;

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

}
