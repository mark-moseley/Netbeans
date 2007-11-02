
package com.sun.stockquote;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.2-hudson-182-RC1
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "StockService", targetNamespace = "http://sun.com/stockquote.wsdl", wsdlLocation = "file:/C:/Users/Peter%20Liu/Documents/NetBeansProjects/StockQuoteService1/StockQuoteService1/src/conf/xml-resources/web-services/StockService/wsdl/StockService.wsdl")
public class StockService
    extends Service
{

    private final static URL STOCKSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(com.sun.stockquote.StockService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.sun.stockquote.StockService.class.getResource(".");
            url = new URL(baseUrl, "file:/C:/Users/Peter%20Liu/Documents/NetBeansProjects/StockQuoteService1/StockQuoteService1/src/conf/xml-resources/web-services/StockService/wsdl/StockService.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'file:/C:/Users/Peter%20Liu/Documents/NetBeansProjects/StockQuoteService1/StockQuoteService1/src/conf/xml-resources/web-services/StockService/wsdl/StockService.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        STOCKSERVICE_WSDL_LOCATION = url;
    }

    public StockService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public StockService() {
        super(STOCKSERVICE_WSDL_LOCATION, new QName("http://sun.com/stockquote.wsdl", "StockService"));
    }

    /**
     * 
     * @return
     *     returns StockQuotePortType
     */
    @WebEndpoint(name = "StockQuotePortTypePort")
    public StockQuotePortType getStockQuotePortTypePort() {
        return super.getPort(new QName("http://sun.com/stockquote.wsdl", "StockQuotePortTypePort"), StockQuotePortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns StockQuotePortType
     */
    @WebEndpoint(name = "StockQuotePortTypePort")
    public StockQuotePortType getStockQuotePortTypePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://sun.com/stockquote.wsdl", "StockQuotePortTypePort"), StockQuotePortType.class, features);
    }

}
