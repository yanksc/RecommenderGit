import java.awt.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.text.html.HTMLDocument.Iterator;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;


public class Validator{	
	DeliciousParser p = null;
	static final int OUTPUT_TOP_K = 1000;
	static final int BENIFIT = 50;
	static final double GAMMA = 0.3;
	int MAX_RANGE;
	double listSize=0;
	ArrayList<Item> popList, mixList, itemList;
	static int COLD_START_NUM = 10;
	public Validator(DeliciousParser ps){
		p = ps;
	}
	
	public void valid_range(int fromK, int toK) throws IOException{
		popList = getPopList(OUTPUT_TOP_K);
		
		String type;
		
//		type = "itemPopularity";
		type = "itemBased";
//		type = "userBased";
//		type = "userAccumBased";
//		type = "relationBased";
//		type = "transBased";
//		type = "mixBased";
		
		MAX_RANGE = toK;
		System.out.println("\t*****  user > "+ p.MIN_ITEM_FOR_USER + "\titem > "+ p.MIN_USER_FOR_ITEM +"  *****");
		System.out.println("\t*****  Cold Starter 1-"+ COLD_START_NUM + "  *****");
		System.out.println("\t*****Algorithm : " + type+ "*****");
		for(int i = fromK; i<=toK; i+=10){
			if(i==0) continue;
//			System.out.print("Top-k:" + i + "\t");
//			System.out.print(i+"\t");
			validate(type, i);
			
		}
		
	}
	
	public void validate( String type, int top_k ) throws IOException{
		double hitNum = 0, validNum = 0;
		double validuser=0;
		listSize =0;
		HashSet<Item> coverSet = new HashSet<Item>();
		HashSet<Item> coldSet = new HashSet<Item>();
		// validate all the user!
		for(User u : p.id2user.values()){
			if( u.itemList.size() <= COLD_START_NUM && u.itemList.size() !=0 && u.trustList.size() != 0){
//			if(  u.itemList.size() !=0 && u.trustList.size() != 0){	
//				System.out.println(u + "\ti:"+ u.itemList.size()+ "\ttrust:" + u.trustList.size()+ "\t reach:" + u.reachMap.size()+"\t corUser&trustUser:" + u.nearID2cor.size());
				validNum ++;
				if(u.ansList.size() == 0) {
					u.ansList = getAnsList(type, MAX_RANGE, u);
				}
				coldSet.add(u.lastItem);
				if(u.ansList == null ) break;
				if(isHitUser(u, top_k)) hitNum ++;
				coverSet.addAll(u.ansList);
//				listSize += (double)u.ansList.size() / (double)top_k;
//				System.out.println(u+ " "+ u.outreList.size() + " " + (double)relaTouch);
				relaTouch=0;
			}
		}
//		System.out.println(hitNum+"/"+validNum +"\t"+ (hitNum/validNum)*100 +"	sizeRate:" +listSize/validNum +"  coverSet: "+coverSet.size());
		System.out.println((hitNum/validNum)*100);
	
	}
	public ArrayList<Item> fillList(ArrayList<Item> fList, int top_k){
		int curIndex=0;
		while(fList.size() < top_k){
			fList.add(popList.get(curIndex++));
			fList = ensureList(fList);
		}
		return fList;
	}
	public ArrayList<Item> getAnsList( String type, int top_k , User u) throws IOException{
		ArrayList<Item> aList = null;
		if		(type.compareTo("itemPopularity")==0){
			aList = getPopList(top_k);
		}else if(type.compareTo("itemBased")==0){
//			return getItemList(top_k, u);
			aList = getItemList(top_k,u);
		}else if(type.compareTo("userBased")==0){
			aList = getUserList(top_k, u);
		}else if(type.compareTo("userAccumBased")==0){
			aList = getUserAccumList(top_k, u);	
		}else if(type.compareTo("relationBased")==0){
			aList = getRelationList(top_k, u);
		}else if(type.compareTo("mixBased")==0){
			aList = getMixBased(top_k, u);
		}else if(type.compareTo("transBased")==0){
			aList = getTransList(top_k, u);
		}
		else{
			System.err.println("Sorry! No Algorith specified!");
			return null;
		}
		
//		return  fillList( aList, top_k );
		return  aList;
		
	}
	
	public ArrayList<Item> getItemList(int k, User u) throws IOException{
		ArrayList<Item> guessList = new ArrayList<Item>();
		HashMap<Integer, Double> iID2score = new HashMap<Integer, Double>();
		ItemIDComparator bvc =  new ItemIDComparator(iID2score);
		TreeMap<Integer,Double> sorted_i2s = new TreeMap<Integer,Double>(bvc);
		
		int curIndex=0;
		double sim =  0.0;
		for(Item i : u.itemList){
			for(Integer nearID : i.nearID2cor.keySet()){
				sim = i.nearID2cor.get(nearID);
				addValue2map(iID2score, nearID, sim);
			}
		}
	    sorted_i2s.putAll(iID2score);
		for(Integer itemID : sorted_i2s.keySet()){
			Item it = p.getItem(itemID);
			if(u.itemList.contains(it)) continue;
			guessList.add(it);
			guessList = ensureList(guessList);
			if(guessList.size() >= k) break;
		}
		return guessList;
	}
	

	public ArrayList<Item> getUserList(int k, User u){
		ArrayList<Item> guessList = new ArrayList<Item>();
		HashMap<Integer, Double> uID2score = new HashMap<Integer, Double>(u.nearID2cor);
		ItemIDComparator bvc =  new ItemIDComparator(uID2score);
		TreeMap<Integer,Double> sorted_u2s = new TreeMap<Integer,Double>(bvc);
		User nearU;
		sorted_u2s.putAll(uID2score);
		
//		System.out.println(u + "\tnear:" + sorted_u2s.size());
		for(Integer uID : sorted_u2s.keySet()){
			nearU = p.getUser(uID);
			for(Item i : nearU.itemList){
				if(!guessList.contains(i) && !u.itemList.contains(i)){
//				if(!guessList.contains(i)){
					guessList.add(i);
				}
				if(guessList.size() > k) break;
			}
			if(guessList.size() > k) break;
		}
		return guessList;
	}
	
	public ArrayList<Item> getUserAccumList(int k, User u){
		ArrayList<Item> guessList = new ArrayList<Item>();
		HashMap<Integer, Double> uID2score = new HashMap<Integer, Double>(u.nearID2cor);
		HashMap<Integer, Double> iID2score = new HashMap<Integer, Double>();
		
		ItemIDComparator bvc =  new ItemIDComparator(uID2score);
		TreeMap<Integer,Double> sorted_u2s = new TreeMap<Integer,Double>(bvc);
		
		sorted_u2s.putAll(uID2score);
		User nearU;
		double tagwTotal = 0.0;
		for(Integer uID : u.nearID2cor.keySet()){
			nearU = p.getUser(uID);			
//			for(Integer tagID : nearU.tagID2freq.keySet()){
//				Tag tag = p.id2tag.get(tagID);
//				double tagWeight = nearU.tagID2freq.get(tagID);
//			}
			for(Item i : nearU.itemList){
				addValue2map(iID2score, i.itemID, u.nearID2cor.get(uID));
			}
		}
		
		// sort item rank list, output top-k list 
		bvc =  new ItemIDComparator(iID2score);
		TreeMap<Integer,Double> itemRankList = new TreeMap<Integer,Double>(bvc);
		itemRankList.putAll(iID2score);
//		System.out.println( iID2score.size() + "\t"+ sorted_item.size());
		
		for(Integer itemID : itemRankList.keySet()){
			Item it = p.getItem(itemID);
			if(u.itemList.contains(it)) continue;
			guessList.add(it);
			guessList = ensureList(guessList);
			if(guessList.size() >= k) break;
		}
		return guessList;
	}

	
	// ************************         ******************
	int relaTouch = 0;
	public ArrayList<Item> getRelationList(int k, User u){
		ArrayList<Item> guessList = new ArrayList<Item>();
		Collections.sort(u.outreList);
		HashMap<Integer, Double> iID2score = new HashMap<Integer, Double>();
		ItemIDComparator bvc =  new ItemIDComparator(iID2score);
		
		for(User nearU : u.trustList){
			relaTouch += nearU.itemList.size();
			for(Item i : nearU.itemList){
				addValue2map(iID2score, i.itemID, 1.0);
			}
		}
		bvc =  new ItemIDComparator(iID2score);
		TreeMap<Integer,Double> itemRankList = new TreeMap<Integer,Double>(bvc);
		itemRankList.putAll(iID2score);
		
		for(Integer itemID : itemRankList.keySet()){
			Item it = p.getItem(itemID);
			if(u.itemList.contains(it)) continue;
			guessList.add(it);
			guessList = ensureList(guessList);
			if(guessList.size() >= k) break;
		}
		
		return guessList;
	}
	
	public ArrayList<Item> getMixBased(int k, User u) throws IOException{
		HashSet<Item> guessList = new HashSet<Item>();
		ArrayList<Item> gList=getMixList(u);
		int curIndex = 0;
		for(int i = 0; i < k ; i++){
			if( i >= gList.size()) break;
			guessList.add(	gList.get(i)	);
		}

		return new ArrayList(guessList);
	}
	public ArrayList<Item> getMixList(User u) throws IOException{
		ArrayList<Item> aList = getItemList(1000,u);
//		ArrayList<Item> bList = getRelationList(k+200,u);
		ArrayList<Item> bList = getUserList(1000,u);
		ArrayList<Item> gList = getIntersect(aList, bList);
		mixList = gList;
		return gList;
	}

	public ArrayList<Item> getTransList(int k, User u) throws IOException{
		ArrayList<Item> guessList = new ArrayList<Item>();
		HashMap<Integer, Double> iID2score = new HashMap<Integer, Double>();
		ItemIDComparator bvc =  new ItemIDComparator(iID2score);
		TreeMap<Integer,Double> sorted_i2s = new TreeMap<Integer,Double>(bvc);
		
		int curIndex=0;
		double sim =  0.0;
		for(Item i : u.itemList){
			for(Integer nearID : i.nearID2trans.keySet()){
				sim = i.nearID2trans.get(nearID);
				if(u.reachMap.containsKey(i)) sim = sim*u.reachMap.get(i);
				addValue2map(iID2score, nearID, sim);
			}
		}
	    sorted_i2s.putAll(iID2score);
		for(Integer itemID : sorted_i2s.keySet()){
			Item it = p.getItem(itemID);
			guessList.add(it);
			guessList = ensureList(guessList);
			if(guessList.size() >= k) break;
		}
		return guessList;
	}
	
	

	
	public ArrayList<Item> getPopList(int k) throws IOException{
		String fileline;
		int iID;
		BufferedReader freader = new BufferedReader(new FileReader(p.itemPopPath));
		ArrayList<Item> guessList = new ArrayList<Item>();
//		ArrayList<Integer> itemList = new ArrayList<Integer>();
		while( guessList.size() < k){
			if( (fileline=freader.readLine())!=null){
				iID = Integer.parseInt(fileline.split("\t")[0]);
				guessList.add(p.id2item.get(iID));
			}
		}
		return guessList;
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
	
	public boolean isHitUser(User u, int k){
		ArrayList<Item> subList =null;
		if(u.ansList.size() < k){
			subList = u.ansList;
			listSize += (double)u.ansList.size() / (double)k;
		}else{
			subList = new ArrayList<Item>(u.ansList.subList(0, k-1));
			listSize += 1;
		}
		if(subList.contains(u.lastItem))
			return true;
		return false;
		
	}
	
	public ArrayList<Item> getIntersect(ArrayList<Item> aSet, ArrayList<Item> bSet){
		ArrayList<Item> commonSet = new ArrayList<Item>();
		for(Item aElem : aSet){
			if(bSet.contains(aElem)){
				commonSet.add(aElem);
			}
		}
		return commonSet;
	}
	
	
	public ArrayList<Item> ensureList(ArrayList<Item> list) { 
        HashSet<Item> hashSet = new HashSet<Item>(); 
        ArrayList<Item> newlist = new ArrayList<Item>(); 
         
        for (Item element : list) {  
            if (hashSet.add(element)) { 
                newlist.add(element); 
            } 
        } 
         
        list.clear(); 
        list.addAll(newlist); 
        return list; 
    } 
}

class ItemIDComparator implements Comparator<Integer> {
    Map<Integer, Double> base;
    public ItemIDComparator(Map<Integer, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(Integer a, Integer b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
//
//
//class RelationComparator implements Comparator<Integer> {
//    Map<Integer, BigInteger> base;
//    public RelationComparator(Map<Integer, BigInteger> base) {
//        this.base = base;
//    }
//    public int compare(Integer a, Integer b) {
//    	// from big to small
//        return -1*base.get(a).compareTo(base.get(b));
//    }
//}