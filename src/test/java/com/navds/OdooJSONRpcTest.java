package com.navds;

import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Ignore;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

/**
 * Test class for OdooJSONRpcTest
 */
public class OdooJSONRpcTest
{
    private static String odooUrl = "http://localhost:8069";
    private static String odooDb = "prod";
    private static String odooUser = "admin";
    private static String odooPass = "admin";
    private static OdooJSONRpc odoo = new OdooJSONRpc();

    @BeforeClass
    public static void setup() {
        odoo = new OdooJSONRpc(odooUrl);
        boolean connected = odoo.login(odooUser, odooDb, odooPass);
        assumeTrue("Odoo connection failure", connected);

    }

    @Test
    public void assumeConnected() {
    }

    @Test
    public void showDatabaseList() {
        List<String> dbList = odoo.getDatabaseList();
        assumeTrue("No database found", !dbList.isEmpty());
    }

    @Test
    public void showModuleList() {
        List<String> list = odoo.getModuleList();
        assumeTrue("No module found", !list.isEmpty());
    }

    @Test
    public void checkSession() {
        String jsonString = odoo.getSessionInfo();
        // System.out.println(jsonString);
    }

    @Test
    public void assureSearchReadGivesResult() {
        List<?> domains = Arrays.asList(Arrays.asList("id",">",0));
        String jsonString = odoo.searchRead("res.partner", Arrays.asList("id","name"), domains);
        System.out.println(jsonString);
    }

    @Test
    public void assureSearchGivesResult() {
        List<?> domains = Arrays.asList(Arrays.asList("name","=","foo"));
        List<Integer> jsonString = odoo.search("res.partner",domains);
        System.out.println(jsonString);
    }

    @Test
    public void assureSearchCountGivesResult() {
        
        List<?> domains = Arrays.asList(Arrays.asList("id",">",0));
        int count = odoo.searchCount("res.partner", domains);
        System.out.println("count: " + count);
    }

    @Test
    public void assureReadGivesResult() {
        List<Integer> ids = Arrays.asList(1,2,3,4,5,3259);
        List<String> fields = Arrays.asList("id","name");
        // odoo.dumpRequest();
        List<Map> result = odoo.read("res.partner", fields, ids);
        System.out.println(result);
    }

    @Test
    public void assureCanCreate() {
        Map<String,Object> data = new HashMap<>();
        data.put("name", "razalghoul");
        data.put("email", "razal@ghoul.com");
        int id = odoo.create("res.partner", data);
        assumeTrue(id != 0);
        System.out.println(id);
    }

    @Test
    public void assureCanWrite() {
        Map<String,Object> data = new HashMap<>();
        data.put("name", "razalghoul2");
        data.put("email", "razal@ghoul2.com");
        // boolean success = odoo.write("res.partner",Arrays.asList(1235123), data);
        // assumeFalse(success);
        boolean success = odoo.write("res.partner",Arrays.asList(5615), data);
        assumeTrue(success);
    }

    @Test
    public void assureCanUnlink() {
        assumeTrue(odoo.unlink("res.partner", Arrays.asList(5615)));
    }

    @Test
    public void assureCanExport() {
        String result = odoo.export("res.partner", Arrays.asList(3249,3250), Arrays.asList("id","name","email"));
        System.out.println(result);
    }

    @Test
    public void assureCanExportFiltered(){
        String result = odoo.exportFiltered("res.partner", 
            Arrays.asList(Arrays.asList("name","ilike","%customer%")), 
            Arrays.asList("id","name","email","lang","company_id","company_id/id")
        );
        System.out.println(result);
    }

    @Test
    public void assureCanImport() {
        List<?> data = Arrays.asList(
            Arrays.asList("__import__.res.partner__cust1","customer 1","customer1@gmail.com"),
            Arrays.asList("__import__.res.partner__cust2","customer 2","customer2@gmail.com")
        );
        List<String> fields = Arrays.asList("id","name","email");
        List<Integer> ids = odoo.bulkImport("res.partner", data, fields);
        System.out.println(ids);
    }

    @Test
    public void assureCanExportByXmlId() {
        odoo.dumpRequest();
        String result = odoo.exportByXmlId("res.partner",
            Arrays.asList("__import__.res.partner__customer1","__import__.res.partner__100") , 
            Arrays.asList("id","name","email")
            );

        System.out.println(result);
    }

    @Test
    public void foobar() {
        System.out.println(new org.json.JSONArray(Arrays.asList("aw","some")).toString());
    }
}