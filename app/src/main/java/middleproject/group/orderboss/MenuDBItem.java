package middleproject.group.orderboss;

/**
 * Created by WEI-ZHE on 2016/4/7.
 */
public class MenuDBItem {

    private long id;
    private String name;
    private int price;
    private String species;
    private int salesNum;
    private String pictureName;

    public MenuDBItem(){

    }

    public MenuDBItem(String name, int price, String species, int salesNum, String pictureName) {
        this.name = name;
        this.price = price;
        this.species = species;
        this.salesNum = salesNum;
        this.pictureName = pictureName;
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

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public int getSalesNum() {
        return salesNum;
    }

    public void setSalesNum(int salesNum) {
        this.salesNum = salesNum;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }
}
