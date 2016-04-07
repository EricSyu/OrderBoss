package middleproject.group.orderboss;

/**
 * Created by WEI-ZHE on 2016/4/7.
 */
public class OrderMealItem {

    private long id;
    private int table;
    private String orderItem;
    private int price;
    private int send;

    public OrderMealItem(){

    }

    public OrderMealItem(int table, String orderItem, int price, int send) {
        this.table = table;
        this.orderItem = orderItem;
        this.price = price;
        this.send = send;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setSend(int send) {
        this.send = send;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public int getSend() {
        return send;
    }

    public long getId() {
        return id;
    }

    public void setTable(int table) {
        this.table = table;
    }

    public void setOrderItem(String orderItem) {
        this.orderItem = orderItem;
    }

    public int getTable() {
        return table;
    }

    public String getOrderItem() {
        return orderItem;
    }
}
