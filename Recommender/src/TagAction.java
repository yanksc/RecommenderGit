import java.math.BigInteger;


public class TagAction {
	int userID;
	int itemID;
	int tagID;
	int value;
	BigInteger timestamp;
	
	TagAction(int uID, int bID, int tID, BigInteger tstamp){
		userID = uID;
		itemID = bID;
		tagID = tID;
		timestamp = tstamp;
	}
	
	public String toString(){
		return "u" + userID +":i"+itemID+"@"+timestamp.toString(); 
	}
	@Override
	public boolean equals(Object obj){
		TagAction t = (TagAction) obj;
		return (userID == t.userID && itemID == t.itemID);
	}
}

