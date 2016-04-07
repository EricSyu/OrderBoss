package middleproject.group.orderboss;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Eric on 2016/4/7.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private ArrayList<OrderMealItem> items;

    private OrderMealDB orderMealDB;

    public MyAdapter(ArrayList<OrderMealItem> items, OrderMealDB orderMealDB) {
        this.items = items;
        this.orderMealDB = orderMealDB;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_card_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView_tablenum.setText("桌號 : "+items.get(position).getTable()+" 桌");
        holder.textView_orderItem.setText(items.get(position).getOrderItem()+" $");
        holder.textView_price.setText("金額 : "+items.get(position).getPrice()+" 元");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onItemDismiss(int position) {

        if(items.get(position).getSend()==0){
            items.get(position).setSend(1);
            orderMealDB.update(items.get(position));
        }
        else if(items.get(position).getSend()==1){
            orderMealDB.delete(items.get(position).getId());
        }

        items.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView_tablenum,textView_orderItem, textView_price;

        public ViewHolder(View v) {
            super(v);

            textView_tablenum = (TextView) v.findViewById(R.id.textView5);
            textView_orderItem = (TextView) v.findViewById(R.id.textView7);
            textView_price = (TextView) v.findViewById(R.id.textView6);
        }

    }
}
