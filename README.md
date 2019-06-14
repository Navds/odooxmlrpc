# odooxmlrpc
Wrapper for Odoo java api for Xml-Rpc

## Connection

OdooXmlRpc odoo = new OdooXmlRpc();
boolean connected = odoo.login(odooHost, database, username, password);
