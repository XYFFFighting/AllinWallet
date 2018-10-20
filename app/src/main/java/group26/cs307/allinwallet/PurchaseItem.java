package group26.cs307.allinwallet;

import java.io.Serializable;

public class PurchaseItem implements Serializable {
    private String category;
    private String title;
    private double amount;
    private String date;
    private String documentUID;

    public PurchaseItem(String category, String title, double amount, String date, String documentUID) {
        this.category = category;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.documentUID = documentUID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public String getAmountString() {
        return Double.toString(amount);
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocumentUID() {
        return documentUID;
    }

    public void setDocumentUID(String documentUID) {
        this.documentUID = documentUID;
    }

    @Override
    public String toString() {
        return "PurchaseItem{" +
                "category='" + category + '\'' +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", documentUID='" + documentUID + '\'' +
                '}';
    }
}
