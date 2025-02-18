package kz.alfabank;

import kz.alfabank.cbc.GetBalanceCBCRequest;
import kz.alfabank.cbc.GetBalanceCBCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Configuration
@Component
public class Balance {
    final Logger logger = LoggerFactory.getLogger(Balance.class);

    @Value("${balance.service.url}")
    private String Burl;

    @Value("${balance.service.hash}")
    private String hash;

    public GetBalanceCBCResponse GetBalance(GetBalanceCBCRequest rq) throws IOException {
        GetBalanceCBCResponse rp = new GetBalanceCBCResponse();
        rp.setCode("0");
        rp.setMessage("CORE BALANCE");
        rp.setAll("");
        URL url = new URL(Burl);
        String encoding = hash;
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        con.setRequestProperty("Authorization", "Basic " + encoding);
        con.setDoOutput(true);
        con.setDoInput(true);

        StringWriter sw = new StringWriter();

        sw.write("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:gf-subscriber-info-service:xsd\">");
        sw.write("<soapenv:Header/>");
        sw.write("<soapenv:Body>");
        sw.write("<urn:request ctn=\"" + rq.getPhone() + "\">");
        sw.write("<urn:requiredInfo>availableBalances</urn:requiredInfo>");
        sw.write("</urn:request>");
        sw.write("</soapenv:Body>");
        sw.write("</soapenv:Envelope>");

        StringBuilder sb = new StringBuilder();
        OutputStream os = con.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
        osw.write(sw.toString());
        osw.flush();
        osw.close();
        logger.info("\nGET BALANCE>>>>>>>>>>>>>>" + sw.toString());

        if (con.getResponseCode() > 399) {
            System.out.println("Failed : HTTP error code : " + con.getResponseCode());
            Reader reader = new InputStreamReader(con.getErrorStream());
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char) ch);
            }
            logger.info("\nGET BALANCE<<<<<<<<<<<<<<<" + sb.toString());

        }

        Reader reader = new InputStreamReader(con.getInputStream());
        while (true) {
            int ch = reader.read();
            if (ch == -1) {
                break;
            }
            sb.append((char) ch);

        }
        logger.info("\nGET BALANCE<<<<<<<<<<<<<<<" + sb.toString());

        StringBuilder all = new StringBuilder();
        all.append("{\"data\":[");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(sb.toString())));

            NodeList nodes = doc.getElementsByTagName("balance");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = Common.getInstance().getNodeValue("sisx:balanceName", element);
                    String id = Common.getInstance().getNodeValue("sisx:balanceID", element);
                    String amount = Common.getInstance().getNodeValue("sisx:balanceAmount", element);
                    all.append("{\"id\":" + id + ",");
                    all.append("\"name\":\"" + name + "\"" + ",");
                    all.append("\"amount\":\"" + amount + "\"},");
                    if (Integer.parseInt(id) == 1) {
                        rp.setBalance(Double.parseDouble(amount));
                        if (rq.getOption() == 1) {
                            return rp;
                        }
                    }
                }
            }
            all.delete(all.length() - 1, all.length());
            all.append("]}");
            rp.setAll(all.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rp;
    }
}
