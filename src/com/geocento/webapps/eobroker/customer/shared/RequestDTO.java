package com.geocento.webapps.eobroker.customer.shared;

/**
 * Created by thomas on 06/07/2016.
 */
public class RequestDTO {

    public static enum TYPE {image, imageservice, imageprocessing, product}

    String id;
    String description;
    TYPE type;

    public RequestDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }
}