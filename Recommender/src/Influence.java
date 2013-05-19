import java.math.BigInteger;
import java.util.ArrayList;


public class Influence {
	ArrayList<BigInteger> interval;
	User fromU, toU;
	public Influence( User u, User v, BigInteger bi){
		fromU = u;
		toU = v;
		interval = new ArrayList<BigInteger>();
		interval.add(bi);
	}
	public String toString(){
		return fromU.userID+"->"+toU.userID + "@"+interval.toString();
	}
	public int infCount(){
		return interval.size();
	}
}
