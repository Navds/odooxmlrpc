package com.navds;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class OdooJSONRpc {
    /**
     * OdooJSONRpc: A simple and intuitive java class to talk to odoo 
     * @author: Navalona Ramanantoanina <github/Navds>
     */
    private String odooUrl = "";
    private final static Log LOGGER = LogFactory.getLog(OdooJSONRpc.class);
    private boolean connected = false;
    private boolean dumpRequest = false;
    private RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60000).setConnectTimeout(60000)
            .build();
    private static CookieStore cookieStore = new BasicCookieStore();
    private static HttpClientContext httpContext = new HttpClientContext();
    private Map<String,Object> context = new HashMap<>();

    public static final String AUTH_URI = "/web/session/authenticate";
    public static final String SESSION_URI = "/web/session/get_session_info";
    public static final String MODULES_URI = "/web/session/modules";
    public static final String DBLIST_URI = "/web/database/list";
    public static final String CALLKW_URI = "/web/dataset/call_kw";
    public static final String SEARCHR_URI = "/web/dataset/search_read";

    /**
     * Dump sent request + received response
     */
    public void dumpRequest() {
        this.dumpRequest = true;
    }
    /**
     * Turn on/off request/response dump
     * @param flag
     */
    public void dumpRequest(boolean flag) {
        this.dumpRequest = flag;
    }
    public Map<String,Object> getContext() {
        return this.context;
    }
    public void setContext(Map<String,Object> context) {
        this.context = context;
    }
    public void setContext(String name, Object value) {
        if (this.context == null) this.context = new HashMap<>();
        this.context.put(name,value);
    }
    public OdooJSONRpc() {

    }
    public OdooJSONRpc(String url) {
        this.odooUrl = url;
       httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
    }

    /**
     * Authenticate and save session cookie
     * @param user
     * @param db
     * @param password
     */
    public boolean login(String user, String db, String password) {
        OdooRPCPayload payload = new OdooRPCPayload();
        payload.prepareLogin(user, password, db);
        OdooRpcResponse response = execute(AUTH_URI, payload);
        connected = response.isOkay();
        return connected;
    }

    /**
     * Check if connection active
     * @return true on success
     */
    public boolean isConnected() {
        return connected;
    }
    /**
     * Get list of databases
     * @return list of String
     */
    public List<String> getDatabaseList() {
        List<String> dbList = new ArrayList<>();
        OdooRPCPayload payload = new OdooRPCPayload();
        OdooRpcResponse response = execute(DBLIST_URI, payload);
        if (response.isOkay()) {
            dbList = (ArrayList<String>) response.getResultArray()
                .toList().stream()
                .map(String::valueOf).collect(Collectors.toList());
        }
        return dbList;
    }

    /**
     * Get list of modules
     * @return List of String (module name)
     */
    public List<String> getModuleList() {
        List<String> list = new ArrayList<>();
        OdooRPCPayload payload = new OdooRPCPayload();
        OdooRpcResponse response = execute(MODULES_URI, payload);
        if (response.isOkay()) {
            list = (ArrayList<String>) response.getResultArray()
                .toList().stream()
                .map(String::valueOf).collect(Collectors.toList());
        } else {
            LOGGER.warn(response.getBody());
        }
        return list;
    }

    /**
     * Get session info
     * @return json String
     */
    public String getSessionInfo() {
        String res = "";
        OdooRPCPayload payload = new OdooRPCPayload();
        OdooRpcResponse response = execute(SESSION_URI, payload);
        if (response.isOkay()) {
            res =  response.getResultObject().toString();
        } 
        return res;
    }

    /**
     * Return fields value of records having given ids
     * @param model
     * @param fields
     * @param ids
     * @return List of Map<field,value>
     */
    public List<Map> read(String model, List<String> fields, List<Integer> ids) {
        List<Map> result = new ArrayList<>();
        OdooRpcResponse response = callKw(model, "read", Arrays.asList(ids, fields));
        if (response.isOkay()){
            result = response.getResultArray()
                .toList()
                .stream()
                .map(o -> ((Map) o))
                .collect(Collectors.toList());
        } 
        return result;
    }

    /**
     * This will return all matches
     */
    public String searchRead(String model, List<String> fields, List<?> domains) {
        return searchRead(model, fields, domains, 0, 0, "");
    }
    /**
     * Search record using filter (domains) then return selected fields
     * @param fields List<String> 
     * @param domains List of List
     * @param offset 
     * @param limit
     * @param sort
     * @param context
     */
    public String searchRead(String model, 
        List<String> fields, 
        List<?> domains,
        int offset,
        int limit, 
        String sort
        ) {
            String result = "";
            Map<String,Object> params = new HashMap<>();
            params.put("model", model);
            params.put("fields", fields);
            params.put("domain", domains);
            params.put("offset", offset);
            params.put("limit", limit);
            params.put("sort", sort);
            params.put("context", this.context);
            OdooRPCPayload payload = new OdooRPCPayload(params);
            OdooRpcResponse response = execute(SEARCHR_URI, payload);
            if (response.isOkay()) {
                result = response.getResultObject().toString();
            } else {
                LOGGER.warn(response.getErrorMessage());
            }
            return result;
    }
    
    /**
     * Search record and return list of ids
     * @param model
     * @param domains
     * @return list of integer (ids)
     */
    public List<Integer> search(String model, List<?> domains) {
        return search(model, domains, 0, 0, "");
    }
    /**
     * Search record and return list of ids
     * @param domains list of filters
     * @param offset default 0
     * @param limit default 0
     * @param order default ""
     * @return list of integer (ids)
     */
    public List<Integer> search(String model, 
        List<?> domains, 
        int offset, 
        int limit,
        String order
        ) {
            List<Integer> result = new ArrayList<>();
            OdooRpcResponse response = callKw(model, 
                "search", 
                Arrays.asList(domains, offset, limit, order, false)
                );
            if (response.isOkay()){
                result = response.getResultArray()
                    .toList().stream()
                    .map(o -> (Integer) o).collect(Collectors.toList());
            } 
            return result;
    }

    /**
     * Return count of records matching given filter
     * @param model
     * @param domains List of list (filters)
     * @return count integer
     */
    public int searchCount(String model, List<?> domains) {
        int count = 0;
        OdooRpcResponse response = callKw(model, "search_count", Arrays.asList(domains));
        if (response.isOkay()) {
            count = response.getJSONObject().getInt("result");
        }
        return count;
    }

    /**
     * Export record fields based on resource id
     * @param model
     * @param ids List of resource id
     * @param fields List of fields to export (odoo csv header format)
     * @return
     */
    public String export(String model, List<Integer> ids, List<String> fields) {
        String result = "[]";
        OdooRpcResponse response = callKw(model, "export_data", Arrays.asList(ids, fields));
        if (response.isOkay()) {
            result = response.getResultObject().getJSONArray("datas").toString();
        }
        return result;
    }

    /**
     * Export record based on filter
     * @param model
     * @param domains list of list
     * @param fields list of fields to export (odoo csv header format)
     */
    public String exportFiltered(String model, List<?> domains, List<String> fields) {
        String result = "[]";
        List<Integer> ids = search(model, domains);
        if (!ids.isEmpty()) {
            result = export(model, ids, fields);
        }
        return result;
    }

    public String exportByXmlId(String model, List<String> xmlIds, List<String> fields) {
        return exportByXmlId("__import__", model, xmlIds, fields);
    }
    public String exportByXmlId(String module, String model, List<String> xmlIds, List<String> fields) {
        List<String> irModelDataNames = new ArrayList<>();
        xmlIds.forEach(xmlId -> {irModelDataNames.add(xmlId.replaceAll(module + "\\.",""));});
        String irModelData = searchRead("ir.model.data", 
            Arrays.asList("res_id"), 
            Arrays.asList(
                Arrays.asList("model","=",model),
                Arrays.asList("module","=",module),
                Arrays.asList("name","in",irModelDataNames.toArray())
            ));
        List<Integer> resIds = new ArrayList<>();
        try {
            new org.json.JSONObject(irModelData)
                .getJSONArray("records").forEach(r ->{
                    resIds.add(((org.json.JSONObject) r).getInt("res_id"));
                });
        } catch (Exception e) {
            LOGGER.error("JSON parsing error", e);
        }
        return export(model, resIds, fields);
    }

    /**
     * Import multiple records
     * @param model
     * @param data
     * @param fields
     */
    public List<Integer> bulkImport(String model, List<?> data, List<String> fields) {
        List<Integer> ids = new ArrayList<>();
        OdooRpcResponse response = callKw(model, "load", 
            Arrays.asList(
                fields,
                data
            )
        );
        if (response.isOkay() && response.getResultObject().has("ids")) {
           ids = response.getResultObject()
                .getJSONArray("ids")
                .toList().stream().mapToInt(id -> (Integer) id)
                .boxed()
                .collect(Collectors.toList());
        }
        return ids;
    }
    /**
     * Create a record and return a non zero id on success
     * @param model
     * @param data (map)
     * @return id
     */
    public int create(String model, Map<String,Object> data) {
        int id = 0;
        OdooRpcResponse response = callKw(model, "create", Arrays.asList(data));
        if (response.isOkay()) {
            id = response.getJSONObject().getInt("result");
        }
        return id;
    }

    /**
     * Update records
     * @param model
     * @param ids List of id of existing records
     * @param data A dictionary of fields,value to update
     */
    public boolean write(String model, List<Integer> ids, Map<String,Object> data) {
        OdooRpcResponse response = callKw(model, "write", Arrays.asList(ids, data));
        return response.isOkay();
    }

    /**
     * Delete record
     * @param model
     * @param ids list of existing ids
     */
    public boolean unlink(String model, List<Integer> ids) {
        OdooRpcResponse response = callKw(model, "unlink", Arrays.asList(ids));
        return response.isOkay();
    }


    /**
     * Execute method in model
     * @param model String
     * @param method String
     * @param args List of List
     */
    public OdooRpcResponse callKw(String model, String method, List<?> args) {
        return callKw(model, method, args, new HashMap<>());
    }

    /**
     * Execute method in model
     * @param model String
     * @param method String
     * @param args List of List
     * @param kwargs Map
     */
    public OdooRpcResponse callKw(String model, String method, List<?> args, Map<String,Object> kwargs) {
        OdooRPCPayload payload = new OdooRPCPayload(
            model, 
            method,
            args,
            kwargs,
            context
            );
        OdooRpcResponse response = execute(CALLKW_URI, payload);
        if (!response.isOkay()) {
            LOGGER.warn(response.getErrorMessage());
        }
        return response;
    }
    /**
     * Fire RPC request
     * @param uri
     * @param payload
     * @return OdooRpcResponse 
     */
    private OdooRpcResponse execute(String uri, OdooRPCPayload payload) {
        if (dumpRequest) LOGGER.info("POST " + odooUrl + uri + ": " + payload.toString(2));
        OdooRpcResponse response = new OdooRpcResponse();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            StringEntity requestEntity = new StringEntity(payload.toString());
            HttpPost httpPost = new HttpPost(odooUrl + uri);
            httpPost.setEntity(requestEntity);
            httpPost.setConfig(requestConfig);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            CloseableHttpResponse httpResponse = httpClient.execute(httpPost,httpContext);
            response = new OdooRpcResponse(httpResponse);

        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unparseable json payload", e);
        } catch (IOException e) {
            LOGGER.error("Http communication error", e);
        } 
        if (dumpRequest) {
            LOGGER.info(String.format("RESPONSE - Code %d, Body: %s", response.getStatus(), response.getBody()));
        }
        return response;
    }

}
