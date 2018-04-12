import java.util.Comparator;

/**
 * Created by User on 08.04.2018.
 */
public class TreeComparator implements Comparator<Double>{

    @Override
    public int compare(Double item1, Double item2) {
       // return item1.compareTo(item2);
        if ((double)item1 == (double)item2){
            return 0;
        } else if (item1 > item2) {
            return -1;
        } else {
            return 1;
        }
    }
}
