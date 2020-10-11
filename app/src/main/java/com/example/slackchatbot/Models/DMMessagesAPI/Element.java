
package com.example.slackchatbot.Models.DMMessagesAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Element {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("elements")
    @Expose
    private List<Element_> elements = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Element_> getElements() {
        return elements;
    }

    public void setElements(List<Element_> elements) {
        this.elements = elements;
    }

}
