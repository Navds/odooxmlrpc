// ==============================================================
// 
// OdooXmlRpc: Wrapper for Odoo xmlrpc api using org.apache.xmlrpc.client
//
// @author	Navalona Ramanantoanina <n.ramanantoanina@consulting-mg.com>
// @date	March, 2019
//
// ==============================================================
package routines;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;

import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;

import java.net.URL;
import java.net.MalformedURLException;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;



public class OdooXmlRpc {

    private int uid = -1;
    private String password;
    private String database;
    
    private URL host;
    private static XmlRpcClient client = new XmlRpcClient();
    private static XmlRpcClientConfigImpl commonConfig = new XmlRpcClientConfigImpl();
    private static XmlRpcClientConfigImpl objectConfig = new XmlRpcClientConfigImpl();
	public static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getRootLogger();
	
	private static final int CONNECTION_TIMEOUT = 20000;
	private static final int RECEIVE_TIMEOUT = 60000;
	
	
    
    public OdooXmlRpc() {
    	commonConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        commonConfig.setReplyTimeout(RECEIVE_TIMEOUT);
        commonConfig.setEnabledForExtensions(true);
        objectConfig.setConnectionTimeout(CONNECTION_TIMEOUT);
        objectConfig.setReplyTimeout(RECEIVE_TIMEOUT);
        objectConfig.setEnabledForExtensions(true);
        
    	// Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
     
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Trust always
                }
     
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Trust always
                }
            }
        };
        
        try {
        
	        // Install the all-trusting trust manager
	        SSLContext sc = SSLContext.getInstance("SSL");
	        
	    	 // Create empty HostnameVerifier
	        HostnameVerifier hv = new HostnameVerifier() {
	                    public boolean verify(String arg0, SSLSession arg1) {
	                            return true;
	                    }
	        };
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception e) {LOGGER.info(e.getMessage());}
    }
    /**
     * If called, xmlrpc request/response will be printed to stdout
     */
    public void dumpRequest () {
    	
    	final XmlRpcTransportFactory transportFactory = new XmlRpcTransportFactory()
        {
            public XmlRpcTransport getTransport()
            {
            	
                return new MessageLoggingTransport(client);
            }
        };
        client.setTransportFactory(transportFactory);
    }
    
    public int getUid() { return this.uid; }
    public void setUid(int uid) { this.uid = uid; }    
    public boolean isConnected() { return this.uid != -1; }
    public String getDatabase() {return this.database;}
    public void setDatabase (String dbname) { this.database = dbname;}
    public String getPassword() { return this.password; }
    public void setPassword (String password) { this.password = password; }
    public URL getHost() { return this.host; }

    public void setHost(String host) {
        try {
            setHost(new URL(host));
        } catch (MalformedURLException e) {
        	LOGGER.info(String.format("[OdooXmlRpc.setHost] %s", e.getMessage()));
        };
    }
    public void setHost(URL newHost) { host = newHost; }

    /**
     * Initiate odoo session
     * @param host
     * @param database
     * @param user
     * @param password
     * @return boolean
     */
    public boolean login (String host, String database, String user, String password) {
    	LOGGER.info(String.format("Starting connection... to %s - %s as %s", host, database, user));
        if (host == null || host.isEmpty()) {
        	LOGGER.info("[OdooXmlRpc.login] Host should not be empty");
            return false;
        }

        try {
            this.host = new URL(host);
            commonConfig.setServerURL(new URL(this.host, "/xmlrpc/2/common"));            
            objectConfig.setServerURL(new URL(this.host, "/xmlrpc/2/object"));
            
            Object[] params = new Object[] {database, user, password};
            LOGGER.info("Sending request...");
            Object res = client.execute(commonConfig, "login", params);
            LOGGER.info("Response received.");
            this.password = password;
            this.database = database;
            
            if (res instanceof Boolean ) this.uid = ((Boolean) res) ? 1 : 0;
            else if (res instanceof Integer) this.uid = (int) res;
            //else throw new Exception("bad response");
        } catch (MalformedURLException urlException) {
            LOGGER.info("[OdooXmlRpc.login] Malformed url.");
            return false;
        } catch (XmlRpcException e) {
        	LOGGER.info("[OdooXmlRpc.login] XmlRpcException. Details" + e.getMessage());
        } catch (Exception e) {
        	LOGGER.info("[OdooXmlRpc.login] Exception. Details: " + e.getMessage());
            return false;
        }
        LOGGER.info("End of connection attempt");
        return this.uid != -1;
    }

    
    /*
     * Get details on Odoo version (debuging purpose)
     * @return version map 
     */
    public Map<String,Object> getVersion () {
        Map<String,Object> version = new HashMap<>();
        try {
            version = (HashMap<String,Object>) client.execute(commonConfig, "version", new Object[0]);
        } catch (Exception e) {
        	LOGGER.info(e.getMessage());
        }
        return version;
    }
    
    

    /**
     * Execute method with parameters
     * @param modelName		string
     * @param methodName	string
     * @param methodParams 	array
     * @return Object
     */
    public Object executeMethod (String modelName, String methodName, Object[] methodParams) {
        Object res = new Object();
        
        try {
            Object[] params = new Object[]{
                    this.database,
                    this.uid,
                    this.password,
                    modelName,
                    methodName,
                    methodParams
            };
            res = client.execute(objectConfig, "execute_kw", params);
            
        } catch (Exception e) {
        	LOGGER.info("[OdooXmlRpc.executeMethod] Exception during execution of " + modelName + "=>" + methodName + ". Details: " + e.getMessage());
        }
        return res;
    }
    
    /**
     * List field, type, help of an Odoo model
     * 
     * @param modelName	string
     * @return listRecords	map
     */
    @SuppressWarnings("rawtypes")
	public Map listRecords (String modelName) {
        Map result = new HashMap();
        try {
            client.setConfig(objectConfig);
            result = (Map<String, Map<String, Object>>) client.execute("execute_kw", asList(
                    this.database, this.uid, this.password,
                    modelName, "fields_get",
                    emptyList(),
                    new HashMap() {{
                        put("attributes", asList("string", "help", "type"));
                    }}
            ));

        } catch (Exception e) {
        	LOGGER.info("[OdooXmlRpc.listRecords] Exception when listing Records of " + modelName + ". Details: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Create record, return its id on success, -1 on failure
     * @param model	string	model name
     * @param data	map		a HashMap of model data
     * @return	id	integer
     */
    public Integer createRecord (String modelName, Map data) {
        Integer recordId = -1;
        try {
            client.setConfig(objectConfig);
            recordId = (Integer) client.execute("execute_kw", asList(
                    this.database, this.uid, this.password,
                    modelName, "create",
                    asList(
                           data
                    )
            ));

        } catch (Exception e) {
        	LOGGER.info("[OdooXmlRpc.createRecord] Exception when creating record in " + modelName + ". Details: " + e.getMessage());
        }
        return recordId;
    }
    
    /**
     * Update record
     * @param model	string	the model name
     * @param data	map		the record data (without id)
     * @param id	integer	the id of the existing record
     */
    public void updateRecord (String modelName, Map data, int id) {
        try {
            client.setConfig(objectConfig);
            client.execute("execute_kw", asList(
                    this.database, this.uid, this.password,
                    modelName, "write",
                    asList(
                            asList(id),
                            data
                    )
            ));

        } catch (Exception e) {
        	LOGGER.info("[OdooXmlRpc.updateRecord] Exception when updating record no" + String.valueOf(id) + " in " + modelName + ". Details: " + e.getMessage());
        }
    }

    public Map getRecordById (String modelName, int id, List fields) {
    	Map<String,Object> record = new HashMap<>();
    	try {
	        List filters = new ArrayList();
	        filters.add(Arrays.asList("id", "=", String.valueOf(id)));
	        List records = getRecords(modelName, fields, filters);
	        if (records.size() == 1) {
	        	record = (Map<String, Object>) records.get(0);
	        }
	    } catch (Exception e) {
	        LOGGER.info("[OdooXmlRpc.getRecordById] Exception when getting record no" + String.valueOf(id) + " in " + modelName + ". Details: " + e.getMessage());
    	}
        return record;
    }

    public List getRecords (String model, List fields) {
        return getRecords (model, fields, null);
    }
    public List getRecords (String model, List fields, List<List> filters) {
        List result = new ArrayList();
        if (fields == null) fields = Arrays.asList("id", "name");

        List AllFilters = new ArrayList<>();
        AllFilters.add(asList("id", ">", "0"));
        if (filters != null) {
            for (List filter : filters){
                AllFilters.add(filter);
            }
        }

        try {
            client.setConfig(objectConfig);
            List finalFields = fields;
            result = asList((Object[]) client.execute("execute_kw", asList(
                    this.database, this.uid, this.password,
                    model, "search_read",
                    asList(
                            AllFilters
                    ),
                    new HashMap() {{
                        put("fields", finalFields);
                    }}
            )));

        } catch (Exception e) {
        	LOGGER.info(String.format("[OdooXmlRpc.getRecords] Exception. Details: %s", e.getMessage()));
        }
        return result;
    }
}

