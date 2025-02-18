package kz.alfabank;

import kz.alfabank.cbc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.math.BigInteger;
import java.net.Proxy;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.json.*;

@Configuration
@Component
public class MfsService {
    final Logger logger = LoggerFactory.getLogger(MfsService.class);
    @Value("${mfs.service.url}")
    private String uri;

    @Value("${mfs.service.serviceId}")
    private String serviceId;

    @Value("${mfs.service.apiNotificationUrl}")
    private String apiNotificationUrl;

    @Value("${mfs.service.jksCerts}")
    private String jksCerts;

    @Value("${mfs.service.password}")
    private String jksPassword;

    @Value("${mfs.service.key1}")
    private String key1;

    @Value("${mfs.service.key2}")
    private String key2;

    private SSLContext getSsl() throws KeyStoreException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyManagementException {
        KeyStore clientStore = KeyStore.getInstance("JKS");
        clientStore.load(new FileInputStream(jksCerts), jksPassword.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        try {
            kmf.init(clientStore, key1.toCharArray());
        } catch (Exception e) {
            kmf.init(clientStore, key2.toCharArray());
        }
        KeyManager[] kms = kmf.getKeyManagers();
        logger.info("MFS >>> Key Menager Start \n");

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {
            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };
        logger.info("MFS >>> Trust Manager Start \n");
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(kms, trustAllCerts, new java.security.SecureRandom());
        return sc;
    }


    private static String GetErrorCodeMFS(String str) {
        JsonReader jsonReader = Json.createReader(new StringReader(str));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        return jsonObject.getString("errorKey");
    }


    public PurchaseInitiationMFSResponse PurchaseInitiation(PurchaseInitiationMFSRequest rq) {
        PurchaseInitiationMFSResponse rp = new PurchaseInitiationMFSResponse();
        HttpsURLConnection con = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(uri + "transactions");
            logger.info("MFS 1 url>>>> \n" + uri + "transactions");
            con = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            try {
                con.setSSLSocketFactory(getSsl().getSocketFactory());
            } catch (KeyStoreException e) {
                logger.error(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage());
            } catch (UnrecoverableKeyException e) {
                logger.error(e.getMessage());
            } catch (CertificateException e) {
                logger.error(e.getMessage());
            } catch (KeyManagementException e) {
                logger.error(e.getMessage());
            }
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Api-Timestamp", Gettime());
            con.setRequestProperty("Api-Notification-Url", apiNotificationUrl);
            con.setRequestProperty("Api-Request-Channel", "PARTNER");

            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add("type", "PURCHASE")
                    .add("recipientId", "")
                    .add("msisdn", rq.getPhoneNumber())
                    .add("currency", "KZT")
                    .add("amount", rq.getAmount())
                    .add("transactionSource", "TrustedMerchant")
                    .add("transactionDate", rq.getTransactionDate())
                    .add("merchantTxnId", rq.getMerchantTxnId())
                    .add("merchantDescription", rq.getMerchantDescription())
                    .add("serviceId", serviceId);
            //   .add("serviceParameters","");
            JsonObject result = jsonObjectBuilder.build();
            StringWriter sw = new StringWriter();
            try (JsonWriter writer = Json.createWriter(sw)) {
                writer.writeObject(result);
            }
            logger.info("MFS 1>>>> \n" + sw.toString());


            OutputStream os = con.getOutputStream();

            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(sw.toString());
            osw.flush();
            osw.close();

            rp.setCode(String.valueOf(con.getResponseCode()));
            if (con.getResponseCode() == 201) {
                rp.setMessage("Purchase Initiation успешно");
            }

            Reader reader = new InputStreamReader(con.getInputStream());
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char) ch);
            }
            logger.info("MFS 1<<< \n" + sb.toString());
            JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            BigInteger transactionId = jsonObject.getJsonNumber("transactionId").bigIntegerValue();
            rp.setConfirmationMode(jsonObject.getString("confirmationMode"));
            rp.setTransactionId(String.valueOf(transactionId));
            return rp;
        } catch (IOException e) {
            try {
                if (con.getResponseCode() > 399) {
                    Reader reader = new InputStreamReader(con.getErrorStream());
                    while (true) {
                        int ch = 0;
                        try {
                            ch = reader.read();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        if (ch == -1) {
                            break;
                        }
                        sb.append((char) ch);
                    }
                    logger.info("MFS 1 <<< \n" + sb.toString());
                    rp.setConfirmationMode("0");
                    rp.setTransactionId("0");
                    rp.setMessage(GetErrorCodeMFS(sb.toString()));
                    rp.setCode(String.valueOf(con.getResponseCode()));

                    return rp;
                }
            } catch (IOException e1) {
                rp.setConfirmationMode("0");
                rp.setTransactionId("0");
                rp.setMessage(e1.getMessage());
                rp.setCode("400");
                return rp;
            }
            logger.info(e.getMessage());
            rp.setConfirmationMode("0");
            rp.setTransactionId("0");
            rp.setMessage("error");
            rp.setCode("400");
        }
        return rp;
    }

    public ConfirmTransactionMFSResponse ConfirmTransaction(ConfirmTransactionMFSRequest rq) {
        ConfirmTransactionMFSResponse rp = new ConfirmTransactionMFSResponse();
        HttpsURLConnection con = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(uri + "transactions/" + rq.getTransactionId());

            con = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            try {
                con.setSSLSocketFactory(getSsl().getSocketFactory());
            } catch (KeyStoreException e) {
                logger.error(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage());
            } catch (UnrecoverableKeyException e) {
                logger.error(e.getMessage());
            } catch (CertificateException e) {
                logger.error(e.getMessage());
            } catch (KeyManagementException e) {
                logger.error(e.getMessage());
            }
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Api-Timestamp", Gettime());
            con.setRequestProperty("Api-Notification-Url", apiNotificationUrl);
            con.setRequestProperty("Api-Request-Channel", "PARTNER");

            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add("operation", "CONFIRM");
            JsonObject result = jsonObjectBuilder.build();
            StringWriter sw = new StringWriter();
            try (JsonWriter writer = Json.createWriter(sw)) {
                writer.writeObject(result);
            }
            logger.info("MFS 2 >>> \n" + sw.toString());

            OutputStream os = con.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(sw.toString());
            osw.flush();
            osw.close();

            Reader reader = new InputStreamReader(con.getInputStream());
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char) ch);

            }
            logger.info("MFS 2 <<< \n" + sb.toString());
            JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            JsonObject jsonObject1 = jsonObject.getJsonObject("feeAndCommissionDetails");
            rp.setMerchantFee(jsonObject1.getString("merchantFee"));
            rp.setCustomerFee(jsonObject1.getString("customerFee"));
            rp.setMerchantCommission(jsonObject1.getString("merchantCommission"));
            rp.setAmount(jsonObject1.getJsonNumber("amount").doubleValue());
            rp.setCurrency(jsonObject1.getString("currency"));

            rp.setCode(String.valueOf(con.getResponseCode()));
            if (con.getResponseCode() == 200) {
                rp.setMessage("confirm created");
            }
            return rp;
        } catch (IOException e) {
            try {
                if (con.getResponseCode() > 399) {
                    logger.info("MFS 2 <<< \n Failed : HTTP error code : " + con.getResponseCode());
                    Reader reader = new InputStreamReader(con.getErrorStream());
                    while (true) {
                        int ch = reader.read();
                        if (ch == -1) {
                            break;
                        }
                        sb.append((char) ch);
                    }
                    logger.info("MFS 2 <<< Error Stream \n" + sb.toString());
                    rp.setMessage(GetErrorCodeMFS(sb.toString()));
                    rp.setMerchantFee("0");
                    rp.setCustomerFee("0");
                    rp.setMerchantCommission("0");
                    rp.setAmount(0);
                    rp.setCurrency("kzt");
                    rp.setCode(String.valueOf(con.getResponseCode()));
                    return rp;
                }
            } catch (IOException e1) {
                rp.setMerchantFee("0");
                rp.setCustomerFee("0");
                rp.setMerchantCommission("0");
                rp.setAmount(0);
                rp.setCurrency("kzt");
                rp.setCode("404");
                logger.info(e1.getMessage());
                return rp;
            }
            logger.info(e.getMessage());
            rp.setMerchantFee("0");
            rp.setCustomerFee("0");
            rp.setMerchantCommission("0");
            rp.setAmount(0);
            rp.setCurrency("kzt");
            return rp;
        }
    }


    public TransactionApprovalMFSResponse TransactionApproval(TransactionApprovalMFSRequest rq) {
        TransactionApprovalMFSResponse rp = new TransactionApprovalMFSResponse();
        HttpsURLConnection con = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(uri + "transactions/" + rq.getTransactionId());

            con = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            try {
                con.setSSLSocketFactory(getSsl().getSocketFactory());
            } catch (KeyStoreException e) {
                logger.error(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage());
            } catch (UnrecoverableKeyException e) {
                logger.error(e.getMessage());
            } catch (CertificateException e) {
                logger.error(e.getMessage());
            } catch (KeyManagementException e) {
                logger.error(e.getMessage());
            }
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Api-Timestamp", Gettime());
            con.setRequestProperty("Api-Notification-Url", apiNotificationUrl);
            con.setRequestProperty("Api-Request-Channel", "PARTNER");

            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add("operation", "APPROVE");

            JsonObject result = jsonObjectBuilder.build();
            StringWriter sw = new StringWriter();
            try (JsonWriter writer = Json.createWriter(sw)) {
                writer.writeObject(result);
            }
            logger.info("MFS 3 >>> \n" + sw.toString());
            OutputStream os = con.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(sw.toString());
            osw.flush();
            osw.close();

            Reader reader = new InputStreamReader(con.getInputStream());
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char) ch);
            }
            logger.info("MFS 3 <<< \n " + sb.toString());
            rp.setCode(String.valueOf(con.getResponseCode()));
            if (con.getResponseCode() == 200) {
                rp.setMessage("Approve accepted");
            }
            return rp;
        } catch (IOException e) {
            try {
                if (con.getResponseCode() == 400 || con.getResponseCode() == 422) {
                    logger.info("MFS 3 >>> \n Failed : HTTP error code : " + con.getResponseCode());
                    Reader reader = new InputStreamReader(con.getErrorStream());
                    while (true) {
                        int ch = reader.read();
                        if (ch == -1) {
                            break;
                        }
                        sb.append((char) ch);
                    }
                    logger.info("MFS 3 <<< \n" + sb.toString());
                    rp.setMessage(GetErrorCodeMFS(sb.toString()));
                    rp.setCode(String.valueOf(con.getResponseCode()));
                    return rp;
                }
            } catch (IOException e1) {
                logger.info(e1.getMessage());
                rp.setMessage(e1.getMessage());
                rp.setCode("400");
            }
            logger.info(e.getMessage());
            rp.setMessage(e.getMessage());
            rp.setCode("0");
            return rp;
        }
    }


    public ChargeConfirmationMFSResponse ChargeConfirmation(ChargeConfirmationMFSRequest rq) {
        ChargeConfirmationMFSResponse rp = new ChargeConfirmationMFSResponse();
        HttpsURLConnection con = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(uri + "transactions/" + rq.getTransactionId());

            con = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            try {
                con.setSSLSocketFactory(getSsl().getSocketFactory());
            } catch (KeyStoreException e) {
                logger.error(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage());
            } catch (UnrecoverableKeyException e) {
                logger.error(e.getMessage());
            } catch (CertificateException e) {
                logger.error(e.getMessage());
            } catch (KeyManagementException e) {
                logger.error(e.getMessage());
            }
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Api-Timestamp", Gettime());
            con.setRequestProperty("Api-Notification-Url", apiNotificationUrl);
            con.setRequestProperty("Api-Request-Channel", "PARTNER");

            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add("operation", "CONFIRM_CHARGE");
            JsonObject result = jsonObjectBuilder.build();
            StringWriter sw = new StringWriter();
            try (JsonWriter writer = Json.createWriter(sw)) {
                writer.writeObject(result);
            }
            logger.info("MFS 4 >>> \n " + sw.toString());
            OutputStream os = con.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(sw.toString());
            osw.flush();
            osw.close();

            Reader reader = new InputStreamReader(con.getInputStream());
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char) ch);
            }
            logger.info("MFS 4 <<< \n" + sb.toString());
            rp.setCode(String.valueOf(con.getResponseCode()));
            if (con.getResponseCode() == 200) {
                rp.setMessage("Списание произвведено");
            }
            return rp;
        } catch (IOException e) {
            try {
                if (con.getResponseCode() == 400 || con.getResponseCode() == 422) {
                    logger.info("MFS 4 <<< \n Failed : HTTP error code : " + con.getResponseCode());
                    Reader reader = new InputStreamReader(con.getErrorStream());
                    while (true) {
                        int ch = reader.read();
                        if (ch == -1) {
                            break;
                        }
                        sb.append((char) ch);
                    }
                    logger.info("MFS 4 <<< \n" + sb.toString());
                    rp.setCode(String.valueOf(con.getResponseCode()));
                    rp.setMessage(GetErrorCodeMFS(sb.toString()));
                    return rp;
                }
            } catch (IOException e1) {
                rp.setCode("400");
                rp.setMessage(e1.getMessage());
                logger.error(e1.getMessage());
                return rp;
            }
            logger.error(e.getMessage());
            rp.setCode("400");
            rp.setMessage("error");
            return rp;
        }
    }


    public StatusTransactionMFSResponse StatusTransaction(StatusTransactionMFSRequest rq) {
        HttpsURLConnection con = null;
        StatusTransactionMFSResponse rp = new StatusTransactionMFSResponse();
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(uri + "transactions/" + rq.getTransactionId() + "?return=status");

            con = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            try {
                con.setSSLSocketFactory(getSsl().getSocketFactory());
            } catch (KeyStoreException e) {
                logger.error(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage());
            } catch (UnrecoverableKeyException e) {
                logger.error(e.getMessage());
            } catch (CertificateException e) {
                logger.error(e.getMessage());
            } catch (KeyManagementException e) {
                logger.error(e.getMessage());
            }
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Api-Timestamp", Gettime());
            con.setRequestProperty("Api-Notification-Url", apiNotificationUrl);
            con.setRequestProperty("Api-Request-Channel", "PARTNER");
            logger.info("MFS 5 >>> \n " + uri + "transactions/" + rq.getTransactionId() + "?return=status");
            Reader reader = new InputStreamReader(con.getInputStream());
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char) ch);
            }

            logger.info("MFS 5 <<< \n" + sb.toString());
            JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            String transactionStatus = jsonObject.getString("transactionStatus");
            String transactionErrorDescription = jsonObject.isNull("transactionErrorDescription") ? "" : jsonObject.getString("transactionErrorDescription");
            String transactionErrorCode = jsonObject.isNull("transactionErrorCode") ? "" : jsonObject.getString("transactionErrorCode");

            rp.setCode(String.valueOf(con.getResponseCode()));
            if (con.getResponseCode() == 200) {
                rp.setMessage(transactionStatus + " " + transactionErrorDescription + " " + transactionErrorCode);
            }
            return rp;
        } catch (IOException e) {
            try {
                if (con.getResponseCode() > 399) {
                    logger.info("MFS 5 <<< \n Failed : HTTP error code : " + con.getResponseCode());
                    Reader reader = new InputStreamReader(con.getErrorStream());
                    while (true) {
                        int ch = reader.read();
                        if (ch == -1) {
                            break;
                        }
                        sb.append((char) ch);
                    }
                    logger.info("MFS 5 <<< \n" + sb.toString());
                    rp.setMessage(GetErrorCodeMFS(sb.toString()));
                    rp.setCode(String.valueOf(con.getResponseCode()));
                    return rp;
                }
            } catch (IOException e1) {
                rp.setMessage(e1.getMessage());
                rp.setCode("400");
                logger.info(e1.getMessage());
                return rp;
            }
            logger.info(e.getMessage());
            rp.setMessage("error");
            rp.setCode("400");
            return rp;
        }
    }


    public RefundTransactionMFSResponse RefundTransaction(RefundTransactionMFSRequest rq) {
        RefundTransactionMFSResponse rp = new RefundTransactionMFSResponse();
        HttpsURLConnection con = null;
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(uri + "transactions/" + rq.getTransactionId());

            con = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            try {
                con.setSSLSocketFactory(getSsl().getSocketFactory());
            } catch (KeyStoreException e) {
                logger.error(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage());
            } catch (UnrecoverableKeyException e) {
                logger.error(e.getMessage());
            } catch (CertificateException e) {
                logger.error(e.getMessage());
            } catch (KeyManagementException e) {
                logger.error(e.getMessage());
            }
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Api-Timestamp", Gettime());
            con.setRequestProperty("Api-Notification-Url", apiNotificationUrl);
            con.setRequestProperty("Api-Request-Channel", "PARTNER");

            JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            jsonObjectBuilder.add("operation", "REFUND")
                    .add("refundAmount", rq.getAmount())
                    .add("currency", "KZT");
            JsonObject result = jsonObjectBuilder.build();
            StringWriter sw = new StringWriter();
            try (JsonWriter writer = Json.createWriter(sw)) {
                writer.writeObject(result);
            }
            logger.info("MFS 6 >>> \n " + sw.toString());
            OutputStream os = con.getOutputStream();

            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(sw.toString());
            osw.flush();
            osw.close();

            Reader reader = new InputStreamReader(con.getInputStream());
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char) ch);
            }
            logger.info("MFS 6 <<< \n" + sb.toString());
            JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            rp.setRefundTransactionId(jsonObject.getString("refundTransactionId"));
            rp.setCustomerFee(jsonObject.getJsonNumber("customerFee").doubleValue());
            rp.setMerchantFee(jsonObject.getJsonNumber("merchantFee").doubleValue());

            rp.setCode(String.valueOf(con.getResponseCode()));
            if (con.getResponseCode() == 200) {
                rp.setMessage("confirm accpeted");
            }
            return rp;
        } catch (IOException e) {
            try {
                if (con.getResponseCode() > 399) {
                    logger.info("MFS 6 <<< \n Failed : HTTP error code : " + con.getResponseCode());
                    Reader reader = new InputStreamReader(con.getErrorStream());
                    while (true) {
                        int ch = reader.read();
                        if (ch == -1) {
                            break;
                        }
                        sb.append((char) ch);
                    }
                    logger.info("MFS 6 <<< \n" + sb.toString());
                    rp.setMessage(GetErrorCodeMFS(sb.toString()));
                    rp.setCode(String.valueOf(con.getResponseCode()));
                    rp.setRefundTransactionId("0");
                    rp.setCustomerFee(0);
                    rp.setMerchantFee(0);
                    return rp;
                }
            } catch (IOException e1) {
                rp.setMessage(e1.getMessage());
                rp.setCode("404");
                rp.setRefundTransactionId("0");
                rp.setCustomerFee(0);
                rp.setMerchantFee(0);
                return rp;
            }
            logger.info(e.getMessage());
            rp.setMessage("error");
            rp.setCode("404");
            rp.setRefundTransactionId("0");
            rp.setCustomerFee(0);
            rp.setMerchantFee(0);
            return rp;
        }

    }

    private String Gettime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date()).toString();
    }
}
