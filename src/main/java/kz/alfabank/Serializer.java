package kz.alfabank;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author ye.zhdanov
 */

public class Serializer {

    private static final Logger LOG = Logger.getLogger(Serializer.class.getName());
    private static final Map<Class,JAXBContext> _jaxbcx = new HashMap<Class, JAXBContext>();


    public static <T>T parse(Class<T> type, String xml) throws JAXBException {
        LOG.debug("Parsing from XML [" + xml + "].");
        if (!_jaxbcx.containsKey(type)) {
            _jaxbcx.put(type, JAXBContext.newInstance(type));
        }
        Unmarshaller jaxbUnmarshaller = _jaxbcx.get(type).createUnmarshaller();
        StringReader sr = new StringReader(xml);
        try {
            T msg = (T) jaxbUnmarshaller.unmarshal(sr);
            LOG.debug("Parsing complete with object.");
            return msg;
        } finally {
            sr.close();
        }
    }


    public static String serialize(Object msg) {
        LOG.debug("Serializing object.");
        StringWriter xml = new StringWriter();
        try {
            if (!_jaxbcx.containsKey(msg.getClass())) {
                _jaxbcx.put(msg.getClass(), JAXBContext.newInstance(msg.getClass()));
            }
            Marshaller jaxbMarshaller = _jaxbcx.get(msg.getClass()).createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            jaxbMarshaller.marshal(msg, xml);
            LOG.debug("Serialized to XML [" + xml.toString() +"].");
            return xml.toString();
        } catch (JAXBException ex) {
            String desc = "Serialize fail.";
            LOG.warn(desc, ex);
            throw new RuntimeException(desc,ex);
        } finally {
            try {
                xml.close();
            } catch (Throwable ex) {
            }
        }
    }
}