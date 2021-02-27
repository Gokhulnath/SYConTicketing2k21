package ssn.sycon.ticketing.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ReferralData implements Serializable, Comparable<ReferralData> {
    private String name;
    private Integer code;
    private Integer referred;
    private ArrayList<BuyerDetails> buyerDetails;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getReferred() {
        return referred;
    }

    public void setReferred(Integer referred) {
        this.referred = referred;
    }


    public ArrayList<BuyerDetails> getBuyerDetails() {
        return buyerDetails;
    }

    public void setBuyerDetails(ArrayList<BuyerDetails> buyerDetails) {
        this.buyerDetails = buyerDetails;
    }

    @Override
    public String toString() {
        return "ReferralData{" +
                "name='" + name + '\'' +
                ", code=" + code +
                ", referred=" + referred +
                ", buyerDetails=" + buyerDetails +
                '}';
    }

    @Override
    public int compareTo(ReferralData o) {
        return this.getReferred().compareTo(o.getReferred());
    }
}
