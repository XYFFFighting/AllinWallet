package group26.cs307.allinwallet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PurchaseItem implements Serializable {
    private String category;
    private String title;
    private double amount;
    private Date date;
    private String documentUID;
    private String location;

    public PurchaseItem(String category, String title, double amount, Date date, String documentUID) {
        this.category = category;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.documentUID = documentUID;
    }

    public PurchaseItem(String category, String title, double amount, Date date, String location, String documentUID) {
        this.category = category;
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.documentUID = documentUID;
        this.location = location;
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

    public String getLocation() {
        return location;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public String getAmountString() {
        return String.format(Locale.getDefault(), "%s%.2f", MainPage.currencySign, amount);
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public String getDateString() {
        return new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date);
    }

    public String getExtendedDateString() {
        return new SimpleDateFormat("E, MMM d, yyyy", Locale.getDefault()).format(date);
    }

    public void setDate(Date date) {
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
