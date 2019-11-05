package com.navds;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcStreamTransport;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransport;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.xml.sax.SAXException;
import org.json.XML;


/**
 * This is a custom XML-RPC transport which logs the outgoing and incoming
 * Depends on org.json
 * XML-RPC messages.
 */
public class MessageLoggingTransport extends XmlRpcSunHttpTransport
{
    private static final Logger log = Logger.getLogger(MessageLoggingTransport.class.getName());
    private boolean dumpJson = false;


    /**
     * Default constructor
     *
     * @see XmlRpcSunHttpTransport#XmlRpcSunHttpTransport(XmlRpcClient)
     * @param pClient
     */
    public MessageLoggingTransport(final XmlRpcClient pClient)
    {
        this(pClient, false);
    }

    public MessageLoggingTransport(final XmlRpcClient pClient, boolean dumpJson) {
        super(pClient);
        this.dumpJson = dumpJson;
    }


    /**
     * Dumps outgoing XML-RPC requests to the log
     */
    @Override
    protected void writeRequest(final XmlRpcStreamTransport.ReqWriter pWriter) throws IOException, XmlRpcException, SAXException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pWriter.write(baos);
        System.out.println("dumpJson " + dumpJson);
        String dump = this.dumpJson ? org.json.XML.toJSONObject(baos.toString()).toString(2) : baos.toString();
        log.info(dump);
        super.writeRequest(pWriter);
    }


    /**
     * Dumps incoming XML-RPC responses to the log
     */
    @Override
    protected Object readResponse(XmlRpcStreamRequestConfig pConfig, InputStream pStream) throws XmlRpcException
    {
        final StringBuffer sb = new StringBuffer();

        try
        {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(pStream));
            String line = reader.readLine();
            while(line != null)
            {
                sb.append(line);
                line = reader.readLine();
            }
        }
        catch(final IOException e)
        {
            log.log(Level.SEVERE, "While reading server response", e);
        }

        log.info(sb.toString());

        final ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
        return super.readResponse(pConfig, bais);
    }
}

