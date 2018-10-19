package group26.cs307.allinwallet;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.PurchaseViewHolder> {
    private static final String TAG = "PurchaseAdapter";
    private List<PurchaseItem> purchaseItemList;

    public static class PurchaseViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryTextView;
        private TextView titleTextView;
        private TextView amountTextView;

        public PurchaseViewHolder(View v) {
            super(v);
            categoryTextView = (TextView) v.findViewById(R.id.category);
            titleTextView = (TextView) v.findViewById(R.id.title);
            amountTextView = (TextView) v.findViewById(R.id.amount);
        }

        public TextView getCategoryTextView() {
            return categoryTextView;
        }

        public TextView getTitleTextView() {
            return titleTextView;
        }

        public TextView getAmountTextView() {
            return amountTextView;
        }
    }

    public PurchaseAdapter(List<PurchaseItem> purchaseItemList) {
        this.purchaseItemList = purchaseItemList;
    }

    @Override
    @NonNull
    public PurchaseAdapter.PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                 int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.purchase_row_item, parent, false);

        return new PurchaseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        holder.getCategoryTextView().setText(purchaseItemList.get(position).getCategory());
        holder.getTitleTextView().setText(purchaseItemList.get(position).getTitle());
        holder.getAmountTextView().setText(purchaseItemList.get(position).getAmountString());
    }

    @Override
    public int getItemCount() {
        return purchaseItemList.size();
    }
}