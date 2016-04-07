package middleproject.group.orderboss;

/**
 * Created by WEI-ZHE on 2016/4/7.
 */
public class MenuDBItem {

    private long id;
    private String name;
    private int price;

    public MenuDBItem(){

    }

    public MenuDBItem(String n, int p){
        name = n;
        price = p;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
