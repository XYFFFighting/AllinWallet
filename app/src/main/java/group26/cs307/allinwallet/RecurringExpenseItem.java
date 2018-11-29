package group26.cs307.allinwallet;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

public class RecurringExpenseItem extends PurchaseItem implements Serializable {
    private int recurringDay;

    public RecurringExpenseItem(String category, String title, double amount, Date date, int
            recurringDay, String documentUID) {
        super(category, title, amount, date, documentUID);
        this.recurringDay = recurringDay;
    }

    @Override
    public String getTitle() {
        return String.format(Locale.getDefault(), "Recurring Expense: %s", title);
    }

    @Override
    public String getExtendedDateString() {
        return toRecurringDayString(recurringDay);
    }

    public int getRecurringDay() {
        return recurringDay;
    }

    public void setRecurringDay(int recurringDay) {
        this.recurringDay = recurringDay;
    }

    public static String toRecurringDayString(int recurringDay) {
        String ordinalSuffix;

        switch (recurringDay) {
            case 1:
            case 21:
                ordinalSuffix = "st";
                break;
            case 2:
            case 22:
                ordinalSuffix = "nd";
                break;
            case 3:
            case 23:
                ordinalSuffix = "rd";
                break;
            default:
                ordinalSuffix = "th";
                break;
        }

        return String.format(Locale.getDefault(), "%d%s day of each month",
                recurringDay, ordinalSuffix);
    }
}
