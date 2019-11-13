package com.navds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OdooRpcResponse {
    int status = -1; // undefined
    String body = "";
    private final static Log LOGGER = LogFactory.getLog(OdooRpcResponse.class);

    public void setStatus(int status) {
        this.status = status;
    }
    public int getStatus() {
        return this.status;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getBody() {
        return this.body;
    }
    public boolean isOkay() {
        boolean isOkay = this.status == HttpStatus.SC_OK;
        isOkay = isOkay && ! new JSONObject(this.body).has("error");
        return isOkay;
    }

    public OdooRpcResponse() {

    }
    public OdooRpcResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }
    public OdooRpcResponse(CloseableHttpResponse httpResponse) {
        this.status = httpResponse.getStatusLine().getStatusCode();
        HttpEntity responseEntity = httpResponse.getEntity();
        if (responseEntity != null) {
            try {
                this.body = EntityUtils.toString(responseEntity);
            } catch (Exception e) {
                LOGGER.warn("Unparseable response", e);
            }
        }
    }

    public JSONObject getJSONObject(){
        JSONObject json = new JSONObject();
        json = new JSONObject(this.body);
        return json;
    }

    public JSONArray getResultArray() {
        JSONArray result = new JSONArray();
        try {
            result = new JSONObject(this.body).getJSONArray("result");
        } catch (JSONException jsonException) {
            LOGGER.error("Result is not an array. " + this.body);
        }
        return result;
    }

    public JSONObject getResultObject() {
        JSONObject result = new JSONObject();
        try {
            result = new JSONObject(this.body).getJSONObject("result");
        } catch (JSONException jsonException) {
            LOGGER.error("Result is not an Object. " + this.body);
        }
        return result;
    }

    public JSONObject getErrorObject() {
        JSONObject error = new JSONObject();
        try {
            error = new JSONObject(this.body).getJSONObject("error");
        } catch (JSONException e) {
            LOGGER.error("Response is not a valid json. " + e.getMessage());
        }
        return error;
    }

    public String getErrorMessage() {
        String error = "";
        try {
            JSONObject data = new JSONObject(this.body)
                .getJSONObject("error")
                .getJSONObject("data");
            error = data.getString("name") + ". " + data.getString("message");
        } catch (JSONException e) {
            LOGGER.error("Response is not a valid json. " + e.getMessage());
        }
        return error;
    }
}
