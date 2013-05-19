import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class Parser {
	TreeMap<Integer, Item> id2item;
	TreeMap<Integer, User> id2user;
	ArrayList<Rating> ratingCorpus;
	static final int COLD_START_NUM = 10;
	
	
	
	public Parser(){
		id2user = new TreeMap<Integer, User>();
		id2item = new TreeMap<Integer, Item>();
	}
	
	
	public User getUser(Integer uID){
		User u = null;
		if(!id2user.keySet().contains(uID)){
			u = new User(uID);
			id2user.put(uID, u);
		}else{
			u = id2user.get(uID); 
		}
		return u;
	}
	public User findUser(Integer uID){
		if(id2user.keySet().contains(uID)){
			return  id2user.get(uID); 
		}
		else return null;
	}
	
	
	public Item getItem(Integer iID){
		Item i = null;
		if(!id2item.keySet().contains(iID)){
			i = new Item(iID);
			id2item.put(iID, i);
		}else{
			i = id2item.get(iID); 
		}
		return i;
	}
	public Item findItem(Integer iID){
		if(id2item.keySet().contains(iID)){
			return  id2item.get(iID); 
		}
		else return null;
	}
	public void addValue2map( Map<Integer, Double> m , Integer key , Double value){
		double temp;
		if(!m.containsKey(key)){
			m.put(key, value);
		}else{
			temp = m.get(key);
			m.put(key, value+temp);
		}
	}
	public double sigmoid(double t){
		return (double) 1.0 /( 1.0+Math.pow(Math.E, -2*t));
	}
	
}
