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
 *         &lt;element name="phoneNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="amount" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="merchantTxnId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="merchantDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="transactionDate" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
    "phoneNumber",
    "amount",
    "merchantTxnId",
    "merchantDescription",
    "transactionDate"
})
@XmlRootElement(name = "purchaseInitiationMFSRequest")
public class PurchaseInitiationMFSRequest {

    @XmlElement(required = true)
    protected String phoneNumber;
    protected double amount;
    @XmlElement(required = true)
    protected String merchantTxnId;
    @XmlElement(required = true)
    protected String merchantDescription;
    @XmlElement(required = true)
    protected String transactionDate;
    @XmlAttribute(name = "systemcode")
    protected String systemcode;
    @XmlAttribute(name = "reqid")
    protected String reqid;
    @XmlAttribute(name = "debug")
    protected String debug;

    /**
     * Gets the value of the phoneNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the value of the phoneNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
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
     * Gets the value of the merchantTxnId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMerchantTxnId() {
        return merchantTxnId;
    }

    /**
     * Sets the value of the merchantTxnId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMerchantTxnId(String value) {
        this.merchantTxnId = value;
    }

    /**
     * Gets the value of the merchantDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMerchantDescription() {
        return merchantDescription;
    }

    /**
     * Sets the value of the merchantDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMerchantDescription(String value) {
        this.merchantDescription = value;
    }

    /**
     * Gets the value of the transactionDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets the value of the transactionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionDate(String value) {
        this.transactionDate = value;
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

}
