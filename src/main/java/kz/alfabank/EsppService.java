package kz.alfabank;

import kz.alfabank.cbc.*;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

/**
 * Created by u5442 on 17.10.2017.
 */
@Configuration
@Component
public class EsppService {
    final Logger logger = LoggerFactory.getLogger(EsppService.class);
    @Value("${espp.service.url}")
    private String esppUrl;

    @Value("${espp.service.username}")
    private String username;

    @Value("${espp.service.password}")
    private String password;

    public MakePaymentESPPResponse MakePayment(MakePaymentESPPRequest rq) {
        MakePaymentESPPResponse esppRP = new MakePaymentESPPResponse();
        try {
            String uri = esppUrl + "work.html?USERNAME=" + username + "&PASSWORD=" + password + "&ACT=0&SOURCE_TYPE=" + String.valueOf(rq.getSourceType()) + "&PAY_AMOUNT=" + String.valueOf(rq.getAmount()) + "&MSISDN=" + rq.getPhoneNumber();
            logger.info("ESPP>>  \n" + uri);
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            conn.disconnect();
            logger.info("ESPP<< \n" + sb.toString());


            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(sb.toString())));

                NodeList nodes = doc.getElementsByTagName("pay-response");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        int status = Integer.parseInt(Common.getInstance().getNodeValue("status_code", element));
                        esppRP.setCode(String.valueOf(status));
                        esppRP.setMessage(GetError(status));
                        String payId = Common.getInstance().getNodeValue("pay_id", element);
                        esppRP.setPayId(Integer.parseInt(payId));
                        esppRP.setReceiptId("0");
                    }
                }

            } catch (Exception e) {
                esppRP.setMessage(e.getMessage());
            }

        } catch (MalformedURLException e) {

            esppRP.setMessage(e.getMessage());

        } catch (IOException e) {

            esppRP.setMessage(e.getMessage());

        }
        return esppRP;
    }

    public MakePaymentESPPResponse ApprovePayment(String payId) {
        MakePaymentESPPResponse esppRP = new MakePaymentESPPResponse();
        try {
            //  System.setProperty("https.proxySet", "true");
            // System.setProperty("https.proxyHost", getProxyAdress());
            //   System.setProperty("https.proxyPort", getProxyPort());
            String uri = esppUrl + "work.html?USERNAME=" + username + "&PASSWORD=" + password + "&ACT=1&PAY_ID=" + payId;
            logger.info("ESPP>>  \n" + uri);
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            conn.disconnect();
            logger.info("ESPP<< \n" + sb.toString());


            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(sb.toString())));

                NodeList nodes = doc.getElementsByTagName("pay-response");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        int status = Integer.parseInt(Common.getInstance().getNodeValue("status_code", element));
                        esppRP.setCode(String.valueOf(status));
                        esppRP.setMessage(GetError(status));
                        String receipt = Common.getInstance().getNodeValue("receipt", element);
                        esppRP.setReceiptId(receipt);
                    }
                }

            } catch (Exception e) {
                esppRP.setMessage(e.getMessage());
            }

        } catch (MalformedURLException e) {

            esppRP.setMessage(e.getMessage());

        } catch (IOException e) {

            esppRP.setMessage(e.getMessage());

        }
        return esppRP;
    }

    public CheckPhoneESPPResponse CheckPhoneNumber(CheckPhoneESPPRequest rq) {
        CheckPhoneESPPResponse esppRP = new CheckPhoneESPPResponse();
        try {
            String uri = esppUrl + "work.html?USERNAME=" + username + "&PASSWORD=" + password + "&ACT=7&MSISDN=" + rq.getPhone() + "&PAY_AMOUNT=0";
            logger.info("ESPP>>  \n" + uri);
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            conn.disconnect();
            logger.info("ESPP<< \n" + sb.toString());


            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(sb.toString())));

                NodeList nodes = doc.getElementsByTagName("pay-response");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        int status = Integer.parseInt(Common.getInstance().getNodeValue("status_code", element));
                        esppRP.setCode(String.valueOf(status));
                        esppRP.setMessage(GetError(status));
                    }
                }

            } catch (Exception e) {
                esppRP.setMessage(e.getMessage());
            }

        } catch (MalformedURLException e) {

            esppRP.setMessage(e.getMessage());

        } catch (IOException e) {

            esppRP.setMessage(e.getMessage());

        }
        return esppRP;
    }

    public TransactionCancelESPPResponse CancelTransaction(TransactionCancelESPPRequest rq) {
        TransactionCancelESPPResponse esppRP = new TransactionCancelESPPResponse();
        try {
            String uri = esppUrl + "work.html?USERNAME=" + username + "&PASSWORD=" + password + "&ACT=6&PAY_AMOUNT=" + String.valueOf(rq.getAmount()) + "&MSISDN=" + rq.getPhoneNumber() + "&RECEIPT_NUM="
                    + rq.getReceiptId() + "&PAY_ID=" + rq.getPayId();
            URL url = new URL(uri);
            logger.info("ESPP>>  \n" + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            conn.disconnect();
            logger.info("ESPP<< \n" + sb.toString());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(sb.toString())));

                NodeList nodes = doc.getElementsByTagName("pay-response");
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        int status = Integer.parseInt(Common.getInstance().getNodeValue("status_code", element));
                        esppRP.setCode(String.valueOf(status));
                        esppRP.setMessage(GetError(status));
                    }
                }

            } catch (Exception e) {
                esppRP.setMessage(e.getMessage());
            }

        } catch (MalformedURLException e) {

            esppRP.setMessage(e.getMessage());

        } catch (IOException e) {

            esppRP.setMessage(e.getMessage());

        }
        return esppRP;
    }

    private String GetTranStat(int key) {
        switch (key) {
            case 101:
                return "Данные корректны. Платеж принят на исполнение.";
            case 102:
                return "Начата проверка возможности платежа.";
            case 110:
                return "Платеж подтвержден.";
            case 111:
                return "Платеж успешно завершен.";
            case 112:
                return "Некритичная ошибка тарификационной системы. Повтор проведения платежа.";
            case 114:
                return "Платеж отправлен на аннулирование.";
            case 115:
                return "Платеж аннулирован.";
            case 117:
                return "Бонусный платеж подтвержден для удаления";
            case 120:
                return "Платеж находится в обработке тарификационной системой.";
            case -102:
                return "Проверка в тарификационной системе неуспешна. Платеж невозможен.";
            case -103:
                return "Платеж отменен по таймауту.";
            case -104:
                return "Платеж отменен пользователем.";
            case -111:
                return "Критичная ошибка тарификационной системы. Платеж не выполнен.";
            case -112:
                return "Превышено количество попыток проведения платежа. Платеж отклонен.";
            case -115:
                return "Платеж не аннулирован.";
            case -117:
                return "Бонусный платеж не аннулирован";
            case -118:
                return "Количество попыток аннулирования бонусного платежа исчерпано";
            default:
                return "Не определенная ошибка";
        }
    }

    private static String GetError(int key) {
        switch (key) {
            case -90:
                return "Абонент принадлежит другому оператору (портирован)";
            case -84:
                return "Аннулирование в биллинге (Amdocs) не прошло";
            case -83:
                return "Аннулирование невозможно (баланс абонента меньше списываемой суммы)";
            case -82:
                return "Платеж уже отменен";
            case -81:
                return "Время возможности аннулирования платежа истекло";
            case -60:
                return "Отказано в смене пароля";
            case -51:
                return "Ошибка сценария";
            case -50:
                return "Ошибка выполнения запроса в БД";
            case -49:
                return "Сбой биллинговой системы";
            case -48:
                return "Превышено количество попыток платежа";
            case -46:
                return "Платеж зарегистрирован, но не проведен";
            case -45:
                return "Платеж не найден";
            case -42:
                return "Платеж на указанную сумму невозможен для данного абонента";
            case -41:
                return "Прием платежей для абонента невозможен";
            case -40:
                return "Абонент не найден";
            case -38:
                return "Превышено количество попыток проведения платежа";
            case -35:
                return "Квота платежей исчерпана";
            case -34:
                return "Прием платежей от партнера заблокирован";
            case -32:
                return "Неизвестный тип источника платежа";
            case -31:
                return "Сумма платежа вне диапазона, заданного для источника платежа";
            case -27:
                return "Транзакция не найдена";
            case -26:
                return "Операция недопустима для данной транзакции";
            case -25:
                return "Доступ запрещен";
            case -20:
                return "Создание транзакции невозможно";
            case -19:
                return "Неверный формат суммы платежа";
            case -18:
                return "Неверный формат даты платежа";
            case -17:
                return "Не задан пароль";
            case -16:
                return "Задан недопустимый параметр";
            case -15:
                return "Не задан идентификатор транзакции";
            case -14:
                return "Валюта не поддерживается";
            case -13:
                return "Не задана сумма платежа";
            case -12:
                return "Номер получателя платежа не задан или неверен";
            case -11:
                return "Действие не предусмотрено";
            case -10:
                return "Не задан номер чека";
            case -3:
                return "Неверный логин/пароль";
            case -2:
                return "Доступ с данного IP не предусмотрен";
            case 0:
                return "Успешно";
            case 20:
                return "Транзакция создана";
            case 21:
                return "Платеж возможен";
            case 22:
                return "Платеж подтвержден";
            case 23:
                return "Платеж отменен";
            case 45:
                return "Платеж проведен успешно";
            case 60:
                return "Пароль успешно изменен";
            case 70:
                return "Состояние транзакции определено";
            case 80:
                return "Аннулирование платежа успешно проведено";
            case 81:
                return "Транзакция отправлена на аннулирование в Amdocs";
            case 82:
                return "Файл для аннулирования от Amdocs получен";
            default:
                return "Не определено";
        }
    }
}
