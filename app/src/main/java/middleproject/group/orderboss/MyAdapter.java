package middleproject.group.orderboss;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Eric on 2016/4/7.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    private ArrayList<OrderMealItem> items;

    private OrderMealDB orderMealDB;

    private Context context;

    public MyAdapter(ArrayList<OrderMealItem> items, OrderMealDB orderMealDB) {
        this.items = items;
        this.orderMealDB = orderMealDB;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_card_view, parent, false);
        this.context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.gl_orderItem.removeAllViews();

        if (items.get(position).getTable() > 0)
            holder.textView_tablenum.setText("桌號 : "+items.get(position).getTable()+" 桌");
        else if (items.get(position).getTable() < 0)
            holder.textView_tablenum.setText("外帶 : "+items.get(position).getTable()*(-1)+" 號");

        String[] orderItem = items.get(position).getOrderItem().split(",");
        for(String s : orderItem){
            TextView tv_item = new TextView(context);
            tv_item.setText(s);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            tv_item.setLayoutParams(lp);
            holder.gl_orderItem.addView(tv_item);
        }

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

            // add each of meal's sales number
            String[] orderMeal = items.get(position).getOrderItem().split(",");
            for (int i=0; i<orderMeal.length; i++){
                String mealName = orderMeal[i].split("x")[0];
                int orderNum = Integer.valueOf(orderMeal[i].split("x")[1]);

                MenuDB menuDB = new MenuDB(context);
                menuDB.incrementMealSalesNum(mealName, orderNum);
            }
        }
        else if(items.get(position).getSend()==1){
            items.get(position).setSend(2);     // 保存所有點餐紀錄
            orderMealDB.update(items.get(position));
        }

        items.remove(position);
        notifyItemRemoved(position);
    }

    public void setItems(ArrayList<OrderMealItem> items) {
        this.items = items;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView_tablenum, textView_price;
        public GridLayout gl_orderItem;

        public ViewHolder(View v) {
            super(v);

            textView_tablenum = (TextView) v.findViewById(R.id.textView5);
            gl_orderItem = (GridLayout) v.findViewById(R.id.grid1);
            textView_price = (TextView) v.findViewById(R.id.textView6);
        }

    }
}
