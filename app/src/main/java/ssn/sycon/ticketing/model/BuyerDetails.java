package ssn.sycon.ticketing.model;

import java.io.Serializable;

import ssn.sycon.ticketing.enums.TicketName;

public class BuyerDetails implements Serializable {
    private String Name;
    private String Ticketname;
    private Double price;
    private String orderId;
    private String email;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTicketname() {
        return Ticketname;
    }

    public void setTicketname(String ticketname) {
        Ticketname = ticketname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    @Override
    public String toString() {
        return "BuyerDetails{" +
                "Name='" + Name + '\'' +
                ", Ticketname='" + Ticketname + '\'' +
                ", price=" + price +
                ", orderId='" + orderId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
