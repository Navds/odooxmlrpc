package com.navds;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class OdooRPCPayload  
{
    /**
     * Build JSONRpc payload
     */
    private final static String RPC_VERSION = "2.0";
    private final static String RPC_METHOD = "call";
    private int id = 2;
    private Map<String,Object> context = new HashMap<String,Object>();
    private Map<String,Object> params = new HashMap<String,Object>();
    private Map<String,Object> payload = new HashMap<String,Object>();
    private List<?> args = new ArrayList<ArrayList<Object>>();
    private Map<String,Object> kwargs = new HashMap<String,Object>();
    private String model;
    private String method;

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return this.id;
    }
    public void setContext(Map<String,Object> context) {
        this.context = context;
        build(false);
    }
    public Map<String,Object> getContext() {
        return this.context;
    }
    public void setModel(String model) {
        this.model = model;
        build();
    }
    public void setMethod(String method) {
        this.method = method;
        build();
    }
    public void setArgs(List<? extends List<Object>> args) {
        this.args = args;
        build();
    }
    public void setKwargs(Map<String,Object> kwargs) {
        this.kwargs = kwargs;
        build();
    }

    public OdooRPCPayload () {
        build(false);
    }

    public OdooRPCPayload(String model, 
        String method, 
        List<?> args, 
        Map<String,Object> kwargs,
        Map<String,Object> context) {
            this();
            this.model = model;
            this.method = method;
            this.args = args;
            this.kwargs = kwargs;
            this.context = context;
            build();
    }

    public OdooRPCPayload(Map<String,Object> params) {
        this.params = params;
        Object context = params.get("context"); 
        if (context != null && context instanceof Map){
            this.context = (HashMap<String,Object>) context;
        }
        build(false);
    }

    private void build() {
        build(true);
    }
    private void build(boolean isStandardMethod) {
        if (isStandardMethod) {
            this.params.put("model", this.model);
            this.params.put("method", this.method);
            this.params.put("args", this.args);
            this.params.put("kwargs", this.kwargs);
        }
        this.params.put("context", this.context);
        this.payload.put("jsonrpc", RPC_VERSION);
        this.payload.put("method", RPC_METHOD);
        this.payload.put("id", this.id);
        this.payload.put("params", params);
    }

    public String toString() {
        return new JSONObject(this.payload).toString();
    }

    public String toString(int indent) {
        return new JSONObject(this.payload).toString(indent);
    }

    /**
     * Prepare login payload
     * @param login
     * @param password
     * @param db
     */
    public void prepareLogin(String login, String password, String db) {
        this.params.put("login", login);
        this.params.put("password", password);
        this.params.put("db", db);
        build(false);
    }
}
