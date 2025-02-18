//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.17 at 03:41:10 PM ALMT 
//


package kz.alfabank.cbc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Header">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="extId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="extDt" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="channel" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="lang" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="Body">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="trnType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="dscr" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="idnType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *                   &lt;element name="xdata" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="systemcode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="reqid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="debug" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "header",
    "body"
})
@XmlRootElement(name = "hRequest")
public class HRequest {

    @XmlElement(name = "Header", required = true)
    protected HRequest.Header header;
    @XmlElement(name = "Body", required = true)
    protected HRequest.Body body;
    @XmlAttribute(name = "systemcode")
    protected String systemcode;
    @XmlAttribute(name = "reqid")
    protected String reqid;
    @XmlAttribute(name = "debug")
    protected String debug;

    /**
     * Gets the value of the header property.
     * 
     * @return
     *     possible object is
     *     {@link HRequest.Header }
     *     
     */
    public HRequest.Header getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     * 
     * @param value
     *     allowed object is
     *     {@link HRequest.Header }
     *     
     */
    public void setHeader(HRequest.Header value) {
        this.header = value;
    }

    /**
     * Gets the value of the body property.
     * 
     * @return
     *     possible object is
     *     {@link HRequest.Body }
     *     
     */
    public HRequest.Body getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     * 
     * @param value
     *     allowed object is
     *     {@link HRequest.Body }
     *     
     */
    public void setBody(HRequest.Body value) {
        this.body = value;
    }

    /**
     * Gets the value of the systemcode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSystemcode() {
        return systemcode;
    }

    /**
     * Sets the value of the systemcode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSystemcode(String value) {
        this.systemcode = value;
    }

    /**
     * Gets the value of the reqid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReqid() {
        return reqid;
    }

    /**
     * Sets the value of the reqid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReqid(String value) {
        this.reqid = value;
    }

    /**
     * Gets the value of the debug property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDebug() {
        return debug;
    }

    /**
     * Sets the value of the debug property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDebug(String value) {
        this.debug = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="trnType" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="dscr" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="idnType" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}double"/>
     *         &lt;element name="xdata" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "trnType",
        "dscr",
        "idnType",
        "amount",
        "xdata"
    })
    public static class Body {

        @XmlElement(required = true)
        protected String trnType;
        @XmlElement(required = true)
        protected String dscr;
        @XmlElement(required = true)
        protected String idnType;
        protected double amount;
        @XmlElement(required = true)
        protected String xdata;

        /**
         * Gets the value of the trnType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getTrnType() {
            return trnType;
        }

        /**
         * Sets the value of the trnType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setTrnType(String value) {
            this.trnType = value;
        }

        /**
         * Gets the value of the dscr property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDscr() {
            return dscr;
        }

        /**
         * Sets the value of the dscr property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDscr(String value) {
            this.dscr = value;
        }

        /**
         * Gets the value of the idnType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getIdnType() {
            return idnType;
        }

        /**
         * Sets the value of the idnType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setIdnType(String value) {
            this.idnType = value;
        }

        /**
         * Gets the value of the amount property.
         * 
         */
        public double getAmount() {
            return amount;
        }

        /**
         * Sets the value of the amount property.
         * 
         */
        public void setAmount(double value) {
            this.amount = value;
        }

        /**
         * Gets the value of the xdata property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getXdata() {
            return xdata;
        }

        /**
         * Sets the value of the xdata property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setXdata(String value) {
            this.xdata = value;
        }

    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="extId" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="extDt" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="channel" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="lang" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "extId",
        "extDt",
        "channel",
        "lang"
    })
    public static class Header {

        @XmlElement(required = true)
        protected String extId;
        @XmlElement(required = true)
        protected String extDt;
        @XmlElement(required = true)
        protected String channel;
        @XmlElement(required = true)
        protected String lang;

        /**
         * Gets the value of the extId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtId() {
            return extId;
        }

        /**
         * Sets the value of the extId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtId(String value) {
            this.extId = value;
        }

        /**
         * Gets the value of the extDt property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getExtDt() {
            return extDt;
        }

        /**
         * Sets the value of the extDt property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setExtDt(String value) {
            this.extDt = value;
        }

        /**
         * Gets the value of the channel property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getChannel() {
            return channel;
        }

        /**
         * Sets the value of the channel property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setChannel(String value) {
            this.channel = value;
        }

        /**
         * Gets the value of the lang property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getLang() {
            return lang;
        }

        /**
         * Sets the value of the lang property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setLang(String value) {
            this.lang = value;
        }

    }

}
