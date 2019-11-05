package com.navds;

import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Assert.*;
import org.junit.Ignore;

/**\
 * Unit test for OdooXmlRpc class
 */

public class OdooXmlRpcTest {
    private static OdooXmlRpc client;

    /**
     * setup
     */
    @BeforeClass
    @Ignore
    public static void setup() throws Exception {
       client = new OdooXmlRpc();
       String host = "http://localhost:8069";
       String database = "prod";
       String user = "admin";
       String password = "admin";
       client.dumpRequest(true);
       boolean connected = client.login(host, database, user, password);
       assertTrue("Odoo connection failure.", connected);
    }
    
    /**
     * Test connection
     */
    
    @Test
    @Ignore
    public void  assumeConnected() {
        
    }
}