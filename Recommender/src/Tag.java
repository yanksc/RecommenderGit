import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;


public class Tag implements Comparable{
	int tagID;
	String tagWord;
	HashSet<TagAction> actionSet;
	HashMap<Integer, Double> userID2freq; 
	HashMap<Integer, Double> itemID2freq;
	int tagTime;
	Tag(int ID, String word){
		tagID = ID;
		tagWord = word;
		itemID2freq = new HashMap<Integer, Double>();
		userID2freq = new HashMap<Integer, Double>();
		actionSet = new HashSet<TagAction>();
		tagTime = 0;
	}
	
	public String toString(){
		 return tagID +"(" + userID2freq+ ")" + tagWord;
	}
	
	@Override
	public int compareTo(Object obj) {
		Tag t = (Tag) obj;
		if( tagTime >= t.tagTime)
			return -1;
		else
			return 1;
	}
}
