import java.util.SplittableRandom;

public class Filter {
   public   String area;
   public   String startDate;
   public   String endDate;
   public   int noOfPersons;
   public   int price;
   public int stars;

    public Filter(String area, String startDate, String endDate, int noOfPersons,int price,int stars){
        this.area=area;
        this.startDate=startDate;
        this.endDate=endDate;
        this.noOfPersons=noOfPersons;
        this.price=price;
        this.stars=stars;
    }
}
