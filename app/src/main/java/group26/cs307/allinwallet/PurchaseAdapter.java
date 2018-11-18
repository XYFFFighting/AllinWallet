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
    private PurchaseClickListener listener;

    public PurchaseAdapter(List<PurchaseItem> purchaseItemList, PurchaseClickListener listener) {
        this.purchaseItemList = purchaseItemList;
        this.listener = listener;
    }

    @Override
    @NonNull
    public PurchaseAdapter.PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                 int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.purchase_row_item, parent, false);

        return new PurchaseViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        holder.getCategoryTextView().setText(purchaseItemList.get(position).getCategory());
        holder.getTitleTextView().setText(purchaseItemList.get(position).getTitle());
        holder.getAmountTextView().setText(purchaseItemList.get(position).getAmountString()+MainPage.currencySign);
        holder.getDateTextView().setText(purchaseItemList.get(position).getExtendedDateString());
    }

    @Override
    public int getItemCount() {
        return purchaseItemList.size();
    }

    public static class PurchaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView categoryTextView;
        private TextView titleTextView;
        private TextView amountTextView;
        private TextView dateTextView;
        private PurchaseClickListener listener;

        public PurchaseViewHolder(View v, PurchaseClickListener listener) {
            super(v);
            categoryTextView = (TextView) v.findViewById(R.id.category);
            titleTextView = (TextView) v.findViewById(R.id.title);
            amountTextView = (TextView) v.findViewById(R.id.amount);
            dateTextView = (TextView) v.findViewById(R.id.date);
            this.listener = listener;
            v.setOnClickListener(this);
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

        public TextView getDateTextView() {
            return dateTextView;
        }

        @Override
        public void onClick(View view) {
            listener.purchaseListClicked(view, getLayoutPosition());
        }
    }
}