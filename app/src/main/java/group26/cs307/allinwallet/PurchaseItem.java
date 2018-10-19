package group26.cs307.allinwallet;

import java.io.Serializable;

public class PurchaseItem implements Serializable {
    private String category;
    private String title;
    private double amount;

    public PurchaseItem(String category, String title, double amount) {
        this.category = category;
        this.title = title;
        this.amount = amount;
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

    @Override
    public String toString() {
        return "PurchaseItem{" +
                "category='" + category + '\'' +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                '}';
    }
}
