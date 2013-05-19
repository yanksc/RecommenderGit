import java.math.BigInteger;


public class Rating {
	int userID;
	int itemID;
	int tagID;
	int value;
	Integer timestamp;
	
	
	Rating(int uID, int bID, int v, int ts){
		userID = uID;
		itemID = bID;
		value = v;
		timestamp = ts;
	}
	
	public String toString(){
		return "u" + userID +":i"+itemID+":" + value + "@"+timestamp.toString(); 
	}
	@Override
	public boolean equals(Object obj){
		Rating r = (Rating) obj;
		return (userID == r.userID && itemID == r.itemID);
	}
}
