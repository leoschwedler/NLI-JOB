package it.divitech.nliticketapp.data.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentResponse
{
    @JsonProperty("ac")
    public String ac;

    @JsonProperty("acquirerName")
    public String acquirerName;

    @JsonProperty("actionCode")
    public String actionCode;

    @JsonProperty("additionalData")
    public Object additionalData;  // Tipo generico per dati aggiuntivi

    @JsonProperty("aid")
    public String aid;

    @JsonProperty("aip")
    public String aip;

    @JsonProperty("appl")
    public String appl;

    @JsonProperty("atc")
    public String atc;

    @JsonProperty("auc")
    public String auc;

    @JsonProperty("authHostCode")
    public String authHostCode;

    @JsonProperty("binPan")
    public String binPan;

    @JsonProperty("cardType")
    public String cardType;

    @JsonProperty("cid")
    public String cid;

    @JsonProperty("circuitName")
    public String circuitName;

    @JsonProperty("ctq")
    public String ctq;

    @JsonProperty("currencyFlag")
    public String currencyFlag;

    @JsonProperty("cvm")
    public String cvm;

    @JsonProperty("gtAmount")
    public String gtAmount;

    @JsonProperty("gtId")
    public String gtId;

    @JsonProperty("iac")
    public String iac;

    @JsonProperty("iad")
    public String iad;

    @JsonProperty("idAcquirer")
    public String idAcquirer;

    @JsonProperty("merchid")
    public String merchid;

    @JsonProperty("onlineOpNumber")
    public String onlineOpNumber;

    @JsonProperty("opEcho")
    public String opEcho;

    @JsonProperty("ops")
    public String ops;

    @JsonProperty("pan")
    public String pan;

    @JsonProperty("receipt")
    public String receipt;

    @JsonProperty("result")
    public String result;

    @JsonProperty("serviceName")
    public String serviceName;

    @JsonProperty("stan")
    public String stan;

    @JsonProperty("tac")
    public String tac;

    @JsonProperty("tcc")
    public String tcc;

    @JsonProperty("terminalDate")
    public String terminalDate;

    @JsonProperty("terminalId")
    public String terminalId;

    @JsonProperty("timestamp")
    public String timestamp;

    @JsonProperty("transactionMode")
    public String transactionMode;

    @JsonProperty("trcc")
    public String trcc;

    @JsonProperty("trxResultMessage")
    public String trxResultMessage;

    @JsonProperty("trxType")
    public String trxType;

    @JsonProperty("tsi")
    public String tsi;

    @JsonProperty("tt")
    public String tt;

    @JsonProperty("tvr")
    public String tvr;

    @JsonProperty("un")
    public String un;

    @JsonProperty("uuid")
    public String uuid;
}
