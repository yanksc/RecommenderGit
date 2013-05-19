import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;


public class User {
	int userID;
	HashSet<Item> itemList;
	HashMap<Item, Double> reachMap;
	ArrayList<Item> ansList;
	HashSet<TagAction> actionList;
	HashMap<Integer, Double> tagID2freq;
	HashSet<Rating> ratingList;
	ArrayList<Relation> outreList, inreList;
	ArrayList<User> trustList;
	HashSet<Item> predList;
	Double iPower;
	HashMap<User, Double> user2power;
	TreeMap<Integer, Double> nearID2cor;
	TagAction lastTag;
	Item lastItem;
	BigInteger lastTS;
	int lastTime;
	
	User(int uID){
		userID = uID;
		itemList = new HashSet<Item>();
		reachMap= new HashMap<Item, Double>();
		ansList = new ArrayList<Item>();
		predList = new HashSet<Item>();
		actionList = new HashSet<TagAction>() ;
		tagID2freq = new HashMap<Integer, Double>() ;
		ratingList = new HashSet<Rating>() ;
		outreList = new ArrayList<Relation>();
		inreList = new ArrayList<Relation>();
		trustList = new ArrayList<User>();
		user2power = new HashMap<User, Double>();
		nearID2cor = new TreeMap<Integer, Double>();
		iPower = 0.0;
	}
	
	public String toString(){
		return "u" + userID; 
	}
	@Override
	public boolean equals(Object obj){
		User u = (User) obj;
		return this.userID == u.userID;
	}
	
	public void addPower(User v, BigInteger ts){
		Double cur_p = 0.0;
		double c = ts.divide(new BigInteger("31536000000")).doubleValue();
		double p = Math.pow(0.8, c); 
		if(!user2power.containsKey(v)){
			user2power.put(v, p);
		}else{
			cur_p = user2power.get(v);
			user2power.put(v, cur_p+p);
		}
	}
	
	public void getPower(){
		for(Double d : user2power.values()){
			iPower += d;
		}
	}
}
