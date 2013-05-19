import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;


public class Item {	
	int itemID;
	HashSet<User> userList;
	HashMap<Integer, Double> nearID2trans; // record the item correlation by interval
	TreeMap<Integer, Double> nearID2cor, nearID2comSize;
	String bookmarkURL;
	Item(int bID){
		itemID = bID;
		userList = new HashSet<User>();
		nearID2cor = new TreeMap<Integer, Double> ();
		nearID2comSize = new TreeMap<Integer, Double> ();
		nearID2trans = new HashMap<Integer, Double>();
	}
	
	public String toString(){
		return "i" + itemID+"by#"+userList.size(); 
	}
	@Override
	public boolean equals(Object obj){
		Item b = (Item) obj;
		return this.itemID == b.itemID;
	}
}