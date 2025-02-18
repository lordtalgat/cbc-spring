package kz.alfabank;

import kz.alfabank.cbc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.json.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Endpoint
public class CBCEndpoint {
    final Logger logger = LoggerFactory.getLogger(CBCEndpoint.class);

    public enum Operation {
        MFSDebit,
        MFSRevers,
        ESPPCredit,
        ESPPRevers
    }

    private static final String NAMESPACE_URI = "http://kz/alfabank/cbc";

    private PostGreeAdapter postGreeAdapter;
    private EsppService esppService;
    private MfsService mfsService;
    private Balance balance;
    private String delayTime;

    @Autowired
    public CBCEndpoint(PostGreeAdapter postGreeAdapter, EsppService esppService, MfsService mfsService) {
        this.postGreeAdapter = postGreeAdapter;
        this.esppService = esppService;
        this.mfsService = mfsService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "checkPhoneESPPRequest")
    @ResponsePayload
    public CheckPhoneESPPResponse CheckPhone(@RequestPayload CheckPhoneESPPRequest rq) {
        CheckPhoneESPPResponse rp = esppService.CheckPhoneNumber(rq);
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "checkCallBDRequest")
    @ResponsePayload
    public CheckCallBDResponse CheckCallBD(@RequestPayload CheckCallBDRequest request) throws SQLException, ClassNotFoundException {
        CheckCallBDResponse response = new CheckCallBDResponse();
        response.setResult(postGreeAdapter.exec(request.getFunction(), request.getJson()));
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "makePaymentESPPRequest")
    @ResponsePayload
    public MakePaymentESPPResponse MakePaymentESPP(@RequestPayload MakePaymentESPPRequest rq) {
        MakePaymentESPPResponse rp = esppService.MakePayment(rq);
        if (rp.getPayId() > 0) {
            MakePaymentESPPResponse rp1 = esppService.ApprovePayment(String.valueOf(rp.getPayId()));
            return rp1;
        }
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "transactionCancelESPPRequest")
    @ResponsePayload
    public TransactionCancelESPPResponse TransactionCancelESPP(@RequestPayload TransactionCancelESPPRequest rq) {
        TransactionCancelESPPResponse rp = esppService.CancelTransaction(rq);
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "purchaseInitiationMFSRequest")
    @ResponsePayload
    public PurchaseInitiationMFSResponse PurchaseInitiationMFS(@RequestPayload PurchaseInitiationMFSRequest rq) {
        PurchaseInitiationMFSResponse rp = mfsService.PurchaseInitiation(rq);
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "confirmTransactionMFSRequest")
    @ResponsePayload
    public ConfirmTransactionMFSResponse ConfirmTransactionMFS(@RequestPayload ConfirmTransactionMFSRequest rq) {
        ConfirmTransactionMFSResponse rp = mfsService.ConfirmTransaction(rq);
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "transactionApprovalMFSRequest")
    @ResponsePayload
    public TransactionApprovalMFSResponse TransactionApprovalMFS(@RequestPayload TransactionApprovalMFSRequest rq) {
        TransactionApprovalMFSResponse rp = mfsService.TransactionApproval(rq);
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "chargeConfirmationMFSRequest")
    @ResponsePayload
    public ChargeConfirmationMFSResponse ChargeConfirmationMFS(@RequestPayload ChargeConfirmationMFSRequest rq) {
        ChargeConfirmationMFSResponse rp = mfsService.ChargeConfirmation(rq);
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "statusTransactionMFSRequest")
    @ResponsePayload
    public StatusTransactionMFSResponse StatusTransactionMFS(@RequestPayload StatusTransactionMFSRequest rq) {
        StatusTransactionMFSResponse rp = mfsService.StatusTransaction(rq);
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "refundTransactionMFSRequest")
    @ResponsePayload
    public RefundTransactionMFSResponse RefundTransactionMFS(@RequestPayload RefundTransactionMFSRequest rq) {
        RefundTransactionMFSResponse rp = mfsService.RefundTransaction(rq);
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "debitMFSCBCRequest")
    @ResponsePayload
    public DebitMFSCBCResponse DebitMFS(@RequestPayload DebitMFSCBCRequest rq) {
        Date cpsDate = convertStringDate(rq.getTransactionDate());
        if (!IsDelayed(cpsDate)) {//Delay
            return getErrorForDelay();
        }
        PurchaseInitiationMFSRequest rq0 = new PurchaseInitiationMFSRequest();
        rq0.setAmount(rq.getAmount());
        rq0.setMerchantDescription(rq.getMerchantDescription());
        rq0.setMerchantTxnId(rq.getMerchantTxnId());
        rq0.setPhoneNumber(rq.getPhoneNumber());
        rq0.setTransactionDate(rq.getTransactionDate());

        PurchaseInitiationMFSResponse rp = mfsService.PurchaseInitiation(rq0);
        logger.info(rp.getCode());
        ConfirmTransactionMFSRequest rq1 = new ConfirmTransactionMFSRequest();
        if (Integer.valueOf(rp.getCode()) < 202 && Integer.valueOf(rp.getCode()) > 200) {
            if (!IsDelayed(cpsDate)) {//Delay
                return getErrorForDelay();
            }
            rq1.setTransactionId(rp.getTransactionId());
            ConfirmTransactionMFSResponse rp1 = mfsService.ConfirmTransaction(rq1);
            if (Integer.valueOf(rp1.getCode()) < 201 && Integer.valueOf(rp.getCode()) > 199) {
                if (!IsDelayed(cpsDate)) {//Delay
                    return getErrorForDelay();
                }
                TransactionApprovalMFSRequest rq2 = new TransactionApprovalMFSRequest();
                rq2.setTransactionId(rp.getTransactionId());
                TransactionApprovalMFSResponse rp2 = mfsService.TransactionApproval(rq2);
                if (Integer.valueOf(rp2.getCode()) < 201 && Integer.valueOf(rp.getCode()) > 199) {
                    if (!IsDelayed(cpsDate)) {//Delay
                        return getErrorForDelay();
                    }
                    ChargeConfirmationMFSRequest rq3 = new ChargeConfirmationMFSRequest();
                    rq3.setTransactionId(rp.getTransactionId());
                    ChargeConfirmationMFSResponse rp3 = mfsService.ChargeConfirmation(rq3);
                    if (Integer.valueOf(rp3.getCode()) < 201 && Integer.valueOf(rp.getCode()) > 199) {
                        rp.setMessage("all steps are passed");
                        rp.setCode("0");
                    } else {
                        rp.setMessage(rp3.getMessage());
                        rp.setCode("-4");
                    }
                } else {
                    rp.setMessage(rp2.getMessage());
                    rp.setCode("-3");
                }
            } else {
                rp.setMessage(rp1.getMessage());
                rp.setCode("-2");
            }
        } else {
            rp.setCode("-1");
        }
        DebitMFSCBCResponse rp1 = new DebitMFSCBCResponse();
        rp1.setMessage(rp.getMessage());
        rp1.setCode(rp.getCode());
        rp1.setTransactionId(rp.getTransactionId());


        try {
            if (Integer.parseInt(rp.getCode()) < 0) {
                postGreeAdapter.writeLog(rp.getCode(), Operation.MFSDebit.toString(), rp.getMessage(), Serializer.serialize(rq), rq.getMerchantTxnId());
            }
        } catch (Exception e) {
            logger.info("ErrorLogin Error>>>>>>>>>>\n" + e);
        }


        return rp1;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "reversMFSCBCRequest")
    @ResponsePayload
    public ReversMFSCBCResponse ReversMFS(@RequestPayload ReversMFSCBCRequest rq) {
        ReversMFSCBCResponse rp = new ReversMFSCBCResponse();
        RefundTransactionMFSRequest rq1 = new RefundTransactionMFSRequest();
        rq1.setTransactionId(rq.getTransactionId());
        rq1.setAmount(rq.getAmount());
        RefundTransactionMFSResponse rp1 = mfsService.RefundTransaction(rq1);
        if (Integer.parseInt(rp1.getCode()) == 200) {
            TransactionApprovalMFSRequest rq2 = new TransactionApprovalMFSRequest();
            rq2.setTransactionId(rp1.getRefundTransactionId());
            TransactionApprovalMFSResponse rp2 = mfsService.TransactionApproval(rq2);
            if (Integer.parseInt(rp2.getCode()) == 200) {
                rp.setCode(rp2.getCode());
                rp.setMessage(rp2.getMessage());
                rp.setMerchantFee(rp1.getMerchantFee());
                rp.setRefundTransactionId(rp1.getRefundTransactionId());
                rp.setCustomerFee(rp1.getCustomerFee());
            } else {
                rp.setCode(rp2.getCode());
                rp.setMessage(rp2.getMessage());
                rp.setMerchantFee(0);
                rp.setRefundTransactionId("0");
                rp.setCustomerFee(0);
            }
        } else {
            rp.setCode(rp1.getCode());
            rp.setMessage(rp1.getMessage());
            rp.setMerchantFee(0);
            rp.setRefundTransactionId("0");
            rp.setCustomerFee(0);
        }

        try {
            if (Integer.parseInt(rp.getRefundTransactionId()) == 0) {
                postGreeAdapter.writeLog(rp.getCode(), Operation.MFSRevers.toString(), rp.getMessage(), Serializer.serialize(rq), rq.getTransactionId());
            }
        } catch (Exception e) {
            logger.info("ErrorLogin Error>>>>>>>>>>\n" + e);
        }


        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBalanceCBCRequest")
    @ResponsePayload
    public GetBalanceCBCResponse GetBalance(@RequestPayload GetBalanceCBCRequest rq) {
        GetBalanceCBCResponse rp = null;
        try {
            rp = balance.GetBalance(rq);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "hRequest")
    @ResponsePayload
    public HResponse Process(@RequestPayload HRequest rq) {
        logger.info(">>>\n" + Serializer.serialize(rq));
        HResponse hResponse = new HResponse();
        HResponse.Header header = new HResponse.Header();
        HResponse.Body body = null;
        try {
            String strRq = Serializer.serialize(rq);

            String login;
            String acc;
            String description;
            String postGressId;
            String status = "0";
            String val = "";
            String accdb = "";
            String ttype = "";
            String fee = "";
            switch (rq.getHeader().getChannel()) {
                case "BAL":
                    JsonReader jsonReader6 = Json.createReader(new StringReader(rq.getBody().getXdata()));
                    JsonObject jsonObject6 = jsonReader6.readObject();
                    jsonReader6.close();
                    login = jsonObject6.getString("login");
                    acc = jsonObject6.getString("acc");

                    header.setStatus(1);
                    header.setErrCode(0);
                    GetBalanceCBCRequest rq5 = new GetBalanceCBCRequest();
                    rq5.setOption(1);
                    rq5.setPhone(login);
                    GetBalanceCBCResponse rp5 = balance.GetBalance(rq5);
                    String b = String.valueOf(rp5.getBalance() * 100);
                    header.setMfsId(b);
                    hResponse.setHeader(header);
                    body = new HResponse.Body();
                    body.setPostgpId("1");
                    hResponse.setBody(body);
                    return hResponse;
                case "MST":
                    JsonReader jsonReader5 = Json.createReader(new StringReader(rq.getBody().getXdata()));
                    JsonObject jsonObject5 = jsonReader5.readObject();
                    jsonReader5.close();
                    login = jsonObject5.getString("login");
                    acc = jsonObject5.getString("acc");

                    logger.info("POst Gress Mini statement>>> " + acc);
                    postGressId = postGreeAdapter.exec("_getMiniStatement", acc);
                    logger.info("POst Gress Mini statement<<< " + acc);
                    header.setStatus(1);
                    header.setErrCode(0);
                    header.setMfsId("2");
                    hResponse.setHeader(header);
                    body = new HResponse.Body();
                    body.setPostgpId(postGressId);
                    hResponse.setBody(body);
                    return hResponse;
                case "CRD":
                    JsonReader jsonReader1 = Json.createReader(new StringReader(rq.getBody().getXdata()));
                    JsonObject jsonObject1 = jsonReader1.readObject();
                    jsonReader1.close();
                    logger.info("JSON <<<<< OK");
                    login = jsonObject1.getString("login");
                    acc = jsonObject1.getString("acc");
                    val = jsonObject1.getString("val");
                    accdb = jsonObject1.getString("accdb");
                    ttype = jsonObject1.getString("ttype");
                    logger.info("JSON <<<<" + login + " " + acc);
                    //TODO Post Gress Check Connection
                    CreditESPPCBCRequest rqESPP = new CreditESPPCBCRequest();
                    rqESPP.setAmount(rq.getBody().getAmount());
                    rqESPP.setPhoneNumber(login);
                    rqESPP.setSourceType(Integer.parseInt(ttype));
                    rqESPP.setTransactionDate(rq.getHeader().getExtDt());
                    CreditESPPCBCResponse rpESPP = CreditESPP(rqESPP);
                    if (Integer.parseInt(rpESPP.getCode()) == 22) {
                        postGressId = setTimeAndGetIdEspp(login, acc, rq.getHeader().getExtDt(), rq.getHeader().getExtId(), rq.getBody().getAmount(), 1, String.valueOf(rpESPP.getPayId()), rpESPP.getReceiptId(), val, accdb, ttype);
                        header.setStatus(1);
                        header.setErrCode(0);
                        header.setMfsId(String.valueOf(rpESPP.getPayId()));
                        hResponse.setHeader(header);
                        body = new HResponse.Body();
                        body.setPostgpId(postGressId);
                        hResponse.setBody(body);
                        return hResponse;
                    }
                    //TODO Save error in ESPP
                    return SetProcessErrorResponse(Integer.parseInt(rpESPP.getCode()), "ESPP");

                case "DBT":
                    JsonReader jsonReader = Json.createReader(new StringReader(rq.getBody().getXdata()));
                    JsonObject jsonObject = jsonReader.readObject();
                    jsonReader.close();
                    logger.info("JSON <<<<< OK");
                    login = jsonObject.getString("login");
                    acc = jsonObject.getString("acc");
                    val = jsonObject.getString("val");
                    description = jsonObject.getString("description");
                    ttype = jsonObject.getString("ttype");
                    fee = jsonObject.getString("fee");

                    if (Double.parseDouble(fee) == 0) {
                        return SetProcessErrorResponse(-1, "MFS", "vip.subscriber-info.telcocharges.error");
                    }

                    postGressId = setTimeAndGetId(login, acc, rq.getHeader().getExtDt(), rq.getHeader().getExtId(), rq.getBody().getAmount(), -1, val, ttype, fee);
                    if (postGressId.length() == 1) {
                        return SetProcessErrorResponse(-1, "MFS", "merchants.transaction.failed");
                    }

                    DebitMFSCBCRequest rqMFS = new DebitMFSCBCRequest();
                    rqMFS.setPhoneNumber(login);
                    rqMFS.setTransactionDate(rq.getHeader().getExtDt());
                    rqMFS.setMerchantTxnId(postGressId);
                    rqMFS.setMerchantDescription(description);
                    rqMFS.setAmount(rq.getBody().getAmount());
                    DebitMFSCBCResponse rpMFS = DebitMFS(rqMFS);

                    if (rpMFS.getCode() == "0") {
                        status = setMFSId(postGressId, rpMFS.getTransactionId());
                        header.setStatus(Integer.parseInt(status));
                        header.setErrCode(0);
                        header.setMfsId(rpMFS.getTransactionId());
                        hResponse.setHeader(header);
                        body = new HResponse.Body();
                        body.setPostgpId(postGressId);
                        hResponse.setBody(body);
                        return hResponse;
                    }
                    return SetProcessErrorResponse(Integer.parseInt(rpMFS.getCode()), "MFS", rpMFS.getMessage());
                case "RVS":
                    logger.info("Start RVS >>>>>>>>>>>>>>>>>>>");
                    JsonReader jsonReader2 = Json.createReader(new StringReader(rq.getBody().getXdata()));
                    JsonObject jsonObject2 = jsonReader2.readObject();
                    jsonReader2.close();

                    logger.info("JSON <<<<< OK");
                    login = jsonObject2.getString("login");
                    String reversalCode = jsonObject2.getString("reversalCode");
                    String approvalCode = jsonObject2.getString("approvalCode");
                    val = jsonObject2.getString("val");
                    ttype = jsonObject2.getString("ttype");

                    String strStatus = GetIdMfsByUniqId(approvalCode, rq.getHeader().getExtId());
                    JsonReader jsonReader3 = Json.createReader(new StringReader(strStatus));
                    JsonObject jsonObject3 = jsonReader3.readObject();
                    jsonReader3.close();

                    try {
                        String crdb = jsonObject3.getString("n_crdb");
                        int status2 = jsonObject3.getInt("status");
                        if (Integer.parseInt(crdb) == -1 && status2 == 1) {
                            hResponse = mfsRevers(approvalCode, rq, header, body, strStatus, ttype, val);
                            hResponse.getHeader().setErrCode(0);
                        } else if (status2 == 1) {
                            String loginto = jsonObject2.getString("login");
                            hResponse = esppRevers(approvalCode, rq, header, body, strStatus, loginto.substring(0, 10), ttype, val);
                            hResponse.getHeader().setErrCode(0);
                        } else {
                            header.setStatus(1);
                            header.setErrCode(0);
                            header.setMfsId("0");
                            hResponse.setHeader(header);
                            body = new HResponse.Body();
                            body.setPostgpId(approvalCode);
                            hResponse.setBody(body);
                            try {
                                if (status2 == 3) {
                                    String uniqId = jsonObject3.getString("uniq_id");
                                    String str = SaveReversPostGressMFS(uniqId, rq.getHeader().getExtDt(), rq.getHeader().getExtId(), rq.getBody().getAmount(), "", "", ttype, val, "");
                                }
                            } catch (Exception e) {
                                return hResponse;
                            }
                            return hResponse;
                        }
                    } catch (Exception e) {
                        header.setStatus(1);
                        header.setErrCode(0);
                        header.setMfsId("0");
                        hResponse.setHeader(header);
                        body = new HResponse.Body();
                        body.setPostgpId(approvalCode);
                        hResponse.setBody(body);
                        return hResponse;
                    }
                    return hResponse;
                default:
                    return SetProcessErrorResponse(1, "GBL2");
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            return SetProcessErrorResponse(1, "GBL1");
        }

        //return hResponse;
    }

    private HResponse SetProcessErrorResponse(int errorcode, String operation) {
        return SetProcessErrorResponse(errorcode, operation, "0");
    }

    private HResponse SetProcessErrorResponse(int errorcode, String operation, String error) {
        HResponse hResponse = new HResponse();
        HResponse.Header header = new HResponse.Header();
        HResponse.Body body = null;
        header.setStatus(1);
        header.setErrCode(GetErrorByOperation(errorcode, operation, error));
        logger.info("Error code=" + header.getErrCode());
        header.setMfsId(String.valueOf(errorcode));
        hResponse.setHeader(header);
        body = new HResponse.Body();
        body.setPostgpId("");
        hResponse.setBody(body);
        return hResponse;
    }

    private int GetErrorByOperation(int errorcode, String operation, String error) {
        logger.info("Enter to GetError=" + error + " Operation=" + operation);
        if (operation.equals("ESPP")) {
            if (errorcode < 0) {
                switch (errorcode) {
                    case -27: //Доступ с данного IP не предусмотрен
                        return 55;
                    case -102:
                    case -103:
                    case -104:
                    case -111:
                    case -112:
                    case -115:
                    case -117:
                    case -118:
                        return 68;
                    case -40:
                    case -12:
                        return 56;
                    case -51:
                    case -50:
                    case -49:
                    case -48:
                    case -18:
                    case -17:
                    case -16:
                    case -15:
                    case -14:
                        return 74;
                    case -45:
                    case -42:
                    case -41:
                    case -38:
                    case -35:
                    case -34:
                    case -32:
                    case -31:
                        return 82;
                    case -19:
                    case -13:
                        return 67;
                    case -11:
                    case -10:
                    case -3:
                    case -2:
                        return 54;
                    default:
                        return 54; //все прочие ошибки
                }
            } else {
                return 8; //все прочие ошибки
            }
        } else if (operation.equals("MFS")) {
            switch (error) {
                case "vip.subscriber-info.telcocharges.error":
                    return 58;
                case "users.pin.invalid":
                    return 53;
                case "merchants.msisdn.not-exist":
                case "user.status-invalid":
                case "transactions.does-not-exist":
                    return 56;
                case "merchants.syntax-error":
                    return 57;
                case "merchants.funds.not-enough":
                case "users.insufficient-balance":
                    return 59;
                case "merchants.amount.invalid":
                case "users.funding.amount-minimum-exceeded":
                case "merchants.amount.not-allowed":
                    return 67;
                case "merchants.serviceParameter.values-invalid":
                    return 68;
                case "vip.subscriber-info.error":
                case "vip.subscriber-info.tariffplan.mismatch":
                    return 69;
                case "merchantDescription.invalid.length":
                case "merchantDescription.invalid.characters":
                    return 73;
                case "server.request.operatation-does-not-exist":
                case "server.request.parsing-content-failed":
                case "server.request.method-incorrect":
                    return 74;
                case "merchants.service.not-exist":
                    return 75;
                case "merchants.transactiondate.invalid":
                case "merchants.transaction.not-exist":
                    return 82;
                case "users.pin.validation.maximum-attempts-exceeded":
                    return 83;
                case "merchants.transaction.failed":
                    return 15;
                case "merchants.transactionsource.not-exist":
                case "merchants.serviceParameter.name-not-exist":
                    return 49;
                case "transactions.already-approved":
                case "transactions.already-complete":
                    return 2;
                case "merchants.transaction.still-processing":
                    return 23;
                case "users.authentication.cannot-request-other-user-information":
                    return 26;
                case "users.otps.invalid":
                case "users.otps.expired":
                case "users.otps.validation.maximum-attempts-exceeded":
                    return 27;
            }
        } else if (operation.equals("WP")) {
            return 21;
        } else {
            return 54;
        }
        return 54;
    }


    public CreditESPPCBCResponse CreditESPP(CreditESPPCBCRequest rq) {
        Date cpsDate = convertStringDate(rq.getTransactionDate());
        if (!IsESPPDelayed(cpsDate)) {//Delay
            return getESPPErrorForDelay();
        }
        CreditESPPCBCResponse rp = new CreditESPPCBCResponse();
        MakePaymentESPPRequest rq1 = new MakePaymentESPPRequest();
        rq1.setPhoneNumber(rq.getPhoneNumber());
        rq1.setAmount(rq.getAmount());
        switch (rq.getSourceType()) {
            case 133:
                rq1.setSourceType(20);
                break;
            default:
                rq1.setSourceType(22);
        }
        MakePaymentESPPResponse rp1 = esppService.MakePayment(rq1);
        rp.setMessage(rp1.getMessage());
        rp.setCode(rp1.getCode());
        rp.setPayId(rp1.getPayId());
        rp.setReceiptId(rp1.getReceiptId());

        try {
            if (Integer.parseInt(rp1.getCode()) != 22) {
                String s = postGreeAdapter.writeLog(rp.getCode(), Operation.ESPPCredit.toString(), rp.getMessage(), Serializer.serialize(rq), rq.getPhoneNumber());
            }
        } catch (Exception e) {
            logger.info("ErrorLogin Error>>>>>>>>>>\n" + e);
        }

        return rp;
    }

    private Date convertStringDate(String newDateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+06:00");
        Date newDate = new Date();
        try {
            newDate = sdf.parse(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    private boolean IsESPPDelayed(Date cpsDate) {
        Date now = new Date();
        if (now.getTime() - cpsDate.getTime() > 5000) {
            return false;
        }
        return true;
    }

    private boolean IsDelayed(Date cpsDate) {
        Date now = new Date();
        if (now.getTime() - cpsDate.getTime() > Integer.parseInt(delayTime)) {
            return false;
        }
        return true;
    }

    private CreditESPPCBCResponse getESPPErrorForDelay() {
        CreditESPPCBCResponse rp = new CreditESPPCBCResponse();
        rp.setMessage("Delay time expired");
        rp.setCode("-12");
        rp.setPayId(0);
        rp.setReceiptId("0");
        return rp;
    }

    private DebitMFSCBCResponse getErrorForDelay() {
        DebitMFSCBCResponse rp1 = new DebitMFSCBCResponse();
        rp1.setMessage("merchants.transaction.not-exist");
        rp1.setCode("-101");
        rp1.setTransactionId("0");
        return rp1;
    }

    private String setMFSId(String uniqid, String mfsId) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("action", "TransCard")
                .add("uniq_id", uniqid)
                .add("mfs_id", mfsId)
                .add("cnlcode", "MFS")
                .add("result", "OK");
        JsonObject result = jsonObjectBuilder.build();
        StringWriter sw = new StringWriter();
        try (JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(result);
        }
        String cmd = sw.toString();
        logger.warn("POST GRESS >> \n" + cmd);
        String js = postGreeAdapter.exec("_beelineexec", cmd);
        logger.warn("POST GRESS << \n" + js);

        JsonReader jsonReader = Json.createReader(new StringReader(js));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        String transactionId = "";

        int status = jsonObject.getInt("status");
        if (status == 1) {
            return "1";
        } else {
            return "0";
        }
    }

    private String setTimeAndGetIdEspp(String login, String acc_no, String cpsdate, String cps_id, double amount, int crdb, String esppId, String espprId, String val, String accdb, String ttype) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("action", "TransCard")
                .add("login", login)
                .add("acc_no", acc_no)
                .add("crdb", crdb)
                .add("cpsdate", cpsdate)
                .add("cps_id", cps_id)
                .add("amount", amount)
                .add("espp_id", esppId)
                .add("espp_rid", espprId)
                .add("val", val)
                .add("accdb", accdb)
                .add("ttype", ttype)
                .add("result", "OK");
        JsonObject result = jsonObjectBuilder.build();
        StringWriter sw = new StringWriter();
        try (JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(result);
        }
        String cmd = sw.toString();
        logger.warn("POST GRESS >> \n" + cmd);
        String js = postGreeAdapter.exec("_beelineexec", cmd);
        logger.warn("POST GRESS << \n" + js);

        JsonReader jsonReader = Json.createReader(new StringReader(js));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        String transactionId = "";

        int status = jsonObject.getInt("status");
        if (status == 1) {
            transactionId = jsonObject.getString("uniq_id");
        }
        return transactionId;
    }

    private String setTimeAndGetId(String login, String acc_no, String cpsdate, String cps_id, double amount, int crdb, String val, String ttype, String fee) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("action", "TransCard")
                .add("login", login)
                .add("acc_no", acc_no)
                .add("crdb", crdb)
                .add("cpsdate", cpsdate)
                .add("cps_id", cps_id)
                .add("amount", amount)
                .add("cnlcode", "CPS")
                .add("status", "200")
                .add("val", val)
                .add("ttype", ttype)
                .add("fee", fee)
                .add("result", "OK");
        JsonObject result = jsonObjectBuilder.build();
        StringWriter sw = new StringWriter();
        try (JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(result);
        }
        String cmd = sw.toString();
        logger.warn("POST GRESS >> \n" + cmd);
        String js = postGreeAdapter.exec("_beelineexec", cmd);
        logger.warn("POST GRESS << \n" + js);

        JsonReader jsonReader = Json.createReader(new StringReader(js));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        String transactionId = "0";

        int status = jsonObject.getInt("status");
        if (status == 1) {
            transactionId = jsonObject.getString("uniq_id");
        }
        return transactionId;
    }

    private String GetIdMfsByUniqId(String uniqid, String cpsId) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("action", "GetTransID")
                .add("cps_id", cpsId)
                .add("uniq_id", uniqid);
        JsonObject result = jsonObjectBuilder.build();
        StringWriter sw = new StringWriter();
        try (JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(result);
        }
        String cmd = sw.toString();
        logger.warn("POST GRESS >> \n" + cmd);
        String js = postGreeAdapter.exec("_beelineexec", cmd);
        logger.warn("POST GRESS << \n" + js);
        return js;
    }

    //Сохранение Реверсной Транзакции
    private String SaveReversPostGressMFS(String uniqid, String cpsdate, String cpsId, double amount, String mfsId, String refundId, String ttype, String val, String agr_id) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("action", "TransCard")
                .add("uniq_id", uniqid)
                .add("crdb", -1)
                .add("reverse", 1)
                .add("cpsdate", cpsdate)
                .add("cps_id", cpsId)
                .add("amount", amount)
                .add("mfs_id", mfsId)
                .add("refund_id", refundId)
                .add("ttype", ttype)
                .add("val", val)
                .add("agr_id", agr_id)
                .add("result", "OK");
        JsonObject result = jsonObjectBuilder.build();
        StringWriter sw = new StringWriter();
        try (JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(result);
        }
        String cmd = sw.toString();
        logger.warn("POST GRESS >> \n" + cmd);
        String js = postGreeAdapter.exec("_beelineexec", cmd);
        logger.warn("POST GRESS << \n" + js);

        return js;
    }

    private String SaveReversPostGressESPP(String uniqid, String cpsdate, String cpsId, double amount, String esppId, String espprId, String ttype, String val) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("action", "TransCard")
                .add("uniq_id", uniqid)
                .add("crdb", 1)
                .add("reverse", 1)
                .add("cpsdate", cpsdate)
                .add("cps_id", cpsId)
                .add("amount", amount)
                .add("espp_id", esppId)
                .add("espp_rid", espprId)
                .add("ttype", ttype)
                .add("val", val)
                .add("result", "OK");
        JsonObject result = jsonObjectBuilder.build();
        StringWriter sw = new StringWriter();
        try (JsonWriter writer = Json.createWriter(sw)) {
            writer.writeObject(result);
        }
        String cmd = sw.toString();
        logger.warn("POST GRESS >> \n" + cmd);
        String js = postGreeAdapter.exec("_beelineexec", cmd);
        logger.warn("POST GRESS << \n" + js);

        return js;
    }

    private HResponse esppRevers(String approvalCode, HRequest rq, HResponse.Header header, HResponse.Body body, String strStatus, String login, String ttype, String val) {
        HResponse hResponse = new HResponse();
        JsonReader jsonReader = Json.createReader(new StringReader(strStatus));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        String esppId = jsonObject.getString("espp_id");
        String espprId = jsonObject.getString("espp_rid");
        TransactionCancelESPPRequest rqEspp = new TransactionCancelESPPRequest();
        rqEspp.setPhoneNumber(login);
        rqEspp.setAmount(rq.getBody().getAmount());
        rqEspp.setPayId(Integer.parseInt(esppId));
        rqEspp.setReceiptId(espprId);
        TransactionCancelESPPResponse rp = esppService.CancelTransaction(rqEspp);
        if (Integer.parseInt(rp.getCode()) == 80) {
            logger.info("SaveRevers >>>>> " + rp.getCode());
            String str2 = SaveReversPostGressESPP(approvalCode, rq.getHeader().getExtDt(), rq.getHeader().getExtId(), rq.getBody().getAmount(), esppId, espprId, ttype, val);

            JsonReader jsonReader1 = Json.createReader(new StringReader(str2));
            JsonObject jsonObject1 = jsonReader1.readObject();
            jsonReader1.close();

            int stat = jsonObject1.getInt("status");
            if (stat == 1) {
                header.setStatus(stat);
                header.setErrCode(0);
                header.setMfsId(esppId);
                hResponse.setHeader(header);
                body = new HResponse.Body();
                body.setPostgpId(approvalCode);
                hResponse.setBody(body);
                return hResponse;
            }
        }

        try {
            if (Integer.parseInt(rp.getCode()) != 80) {
                postGreeAdapter.writeLog(rp.getCode(), Operation.ESPPRevers.toString(), rp.getMessage(), Serializer.serialize(rqEspp), String.valueOf(rqEspp.getPayId()));
            }
        } catch (Exception e) {
            logger.info("ErrorLogin Error>>>>>>>>>>\n" + e);
        }

        return SetProcessErrorResponse(Integer.parseInt(rp.getCode()), "ESPP");
    }

    private HResponse mfsRevers(String approvalCode, HRequest rq, HResponse.Header header, HResponse.Body body, String strStatus, String ttype, String val) {
        HResponse hResponse = new HResponse();
        JsonReader jsonReader3 = Json.createReader(new StringReader(strStatus));
        JsonObject jsonObject3 = jsonReader3.readObject();
        jsonReader3.close();

        int status1 = jsonObject3.getInt("status");
        if (status1 == 1) {
            String mfs_id = jsonObject3.getString("mfs_id");
            String agr_id = jsonObject3.getString("agr_id");
            String uniq_id = jsonObject3.getString("uniq_id");
            if (uniq_id.length() > 1) {
                approvalCode = uniq_id;
            }
            ReversMFSCBCRequest reversMFSRq = new ReversMFSCBCRequest();
            reversMFSRq.setAmount(rq.getBody().getAmount());
            reversMFSRq.setTransactionId(mfs_id);
            ReversMFSCBCResponse reversMFSRp = ReversMFS(reversMFSRq);
            if (Integer.parseInt(reversMFSRp.getCode()) == 200) {
                logger.info("SaveRevers >>>>> " + reversMFSRp.getCode());
                String str2 = "";
                str2 = SaveReversPostGressMFS(approvalCode, rq.getHeader().getExtDt(), rq.getHeader().getExtId(), rq.getBody().getAmount(), mfs_id, reversMFSRp.getRefundTransactionId(), ttype, val, "");

                JsonReader jsonReader4 = Json.createReader(new StringReader(str2));
                JsonObject jsonObject4 = jsonReader4.readObject();
                jsonReader4.close();
                int status2 = jsonObject4.getInt("status");
                if (status2 == 1) {
                    String uniqId = jsonObject4.getString("uniq_id");
                    header.setStatus(status2);
                    header.setErrCode(0);
                    header.setMfsId(reversMFSRp.getRefundTransactionId());
                    hResponse.setHeader(header);
                    body = new HResponse.Body();
                    body.setPostgpId(uniqId);
                    hResponse.setBody(body);
                    return hResponse;
                }
            }
            return SetProcessErrorResponse(Integer.parseInt(reversMFSRp.getCode()), "MFS", reversMFSRp.getMessage());
        } else {
            return SetProcessErrorResponse(1, "GBL");
        }

    }

}
