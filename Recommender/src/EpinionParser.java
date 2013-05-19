import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;


public class EpinionParser {

	ArrayList<Rating> ratingCorpus;
	ArrayList<EpinionTrust> trustCorpus;
	TreeMap<Integer, User> id2user;
	TreeMap<Integer, User> id2colduser;
	TreeMap<Integer, Item> id2item;
	static final int MIN_USER_FOR_ITEM = 5;
	static final int MIN_ITEM_FOR_USER = 2;
	static final int COLD_START_NUM = 10;
	File readFile;
	String dataPath, originRatingPath, originTrustPath, coldUserPath;
	
	public EpinionParser(String dataSetPath) throws IOException{
		dataPath = dataSetPath;
		originRatingPath = "./" + dataPath +"/epinion_rating.dat";
		originTrustPath = "./" + dataPath + "/epinion_trust.txt";
		coldUserPath = "./" + dataPath + "/epinion_rating_user"+ COLD_START_NUM+".dat";
	}
	
	public void initial() throws IOException{
		double sTime = System.currentTimeMillis();
		System.out.print("Initializing Epinion... " );
		String fileline = null;
		int uID = 0, bID=0, tID=0, vID, lastID = 0, rValue, time = 0;
		BigInteger ts = null;
		 
		ratingCorpus = new ArrayList<Rating>();
		trustCorpus = new ArrayList<EpinionTrust>();
		id2user = new TreeMap<Integer, User>();
		id2colduser = new TreeMap<Integer, User>();
		id2item = new TreeMap<Integer, Item>();
		
		User u = null, v= null;
		Item b = null;
		readFile = new File(originRatingPath);
		
		BufferedReader freader = new BufferedReader(new FileReader(readFile));
		
		// scan the action corpus 
		fileline=freader.readLine();
		while( (fileline=freader.readLine())!=null ){
			try{
			uID = Integer.parseInt(fileline.split("\t")[0]);
			bID = Integer.valueOf(fileline.split("\t")[1]);
			rValue = Integer.parseInt(fileline.split("\t")[3]);
			time = Integer.parseInt(fileline.split("\t")[5]);
			} catch(NumberFormatException e) { 
	            System.out.println("輸入格式有誤"); 
	        } 
			u = getUser(uID);
			b = getItem(bID);
			u.lastItem = b;
			u.lastTime = time;
		}
		
		freader = new BufferedReader(new FileReader(readFile));
		fileline=freader.readLine();
		
		while( (fileline=freader.readLine())!=null ){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			bID = Integer.parseInt(fileline.split("\t")[1]);
			rValue = Integer.parseInt(fileline.split("\t")[3]);
			time = Integer.parseInt(fileline.split("\t")[5]);
			u = getUser(uID);
			b = getItem(bID);
			if(bID != u.lastItem.itemID){
				Rating r = new Rating(uID, bID, rValue, time);
				ratingCorpus.add(r);
				u.itemList.add(b);
				u.ratingList.add(r);
				b.userList.add(u);
			}
		}
		freader.close();
 
		// get relation between users

		sTime = System.currentTimeMillis()-sTime;
		System.out.println(" done by " + sTime);
	}
	public void initialTrust() throws IOException{
		BufferedReader freader = new BufferedReader(new FileReader(originTrustPath));
		String fileline = freader.readLine();
		int uID, bID=0, tID=0, vID, lastID = 0;
		User u = null, v= null;
		Item b = null;
		BigInteger ts = null;
		
		while( (fileline=freader.readLine())!=null){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			vID = Integer.parseInt(fileline.split("\t")[1]);
			ts = new BigInteger( fileline.split("\t")[2] );
			
			if(id2user.containsKey(uID) && id2user.containsKey(vID)){ 
				u = getUser(uID);
				v = getUser(vID);
				EpinionTrust et = new EpinionTrust(u, v, ts);
				trustCorpus.add(et);
				u.trustList.add(v);
				v.trustList.add(u);
			}
		}
		int rSize=0;
		int starter = 0;
		int lowFriend = 0;
		for(User us : id2user.values()){
			rSize += us.trustList.size();
			if(us.itemList.size()<10){
				starter++;
				id2colduser.put(us.userID, us);
				if(us.trustList.size()!=0)
					lowFriend++;
			}
		}
		freader.close();
		System.out.println(
				"[action:" + ratingCorpus.size() +
				" user:" + id2user.size() +
				" item:" + id2item.size() +
				" relation:" + rSize + "/2"+
				"]");
		System.out.println("starter  "+ starter);
		System.out.println("lowFriend  "+ lowFriend);
	}
	
	public void getColdInfo(){
		System.out.println("getColdInfo()::: ");
		double avgItem=0.0, avgTrust=0.0;
		for(User u: id2colduser.values()){
			avgItem += u.itemList.size();
			avgTrust += u.trustList.size();
		}
		System.out.println("cold user avg item size: \t" + avgItem/id2colduser.size() );
		System.out.println("cold user avg trust size:\t" + avgTrust/id2colduser.size() );
	}
	
	public void writeSubSet() throws IOException{	
		System.out.print("writeSubSet():::\tBuilding Subset cold start:" + COLD_START_NUM+ "...");
		
		double sTime = System.currentTimeMillis();
		String fileline = null;
		int uID, bID, tID, vID, lastID = 0, lastuID = 0;
		BigInteger ts;
		User u = null, v= null;
		Item b = null;
		BufferedReader freader = new BufferedReader(new FileReader(originRatingPath));
		BufferedWriter fwriter = new BufferedWriter(new FileWriter(coldUserPath));
		fwriter.write("userID	itemID	tagID	timestamp\n");
		fileline=freader.readLine();
		while( (fileline=freader.readLine())!=null){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			bID = Integer.parseInt(fileline.split("\t")[1]);

			u = getUser(uID);
			
			if(u.itemList.size() <= COLD_START_NUM){
				fwriter.write(fileline+"\n" );
			}
			lastuID = uID;
			lastID = bID;
		}
		fwriter.close();
		freader.close();
		sTime = System.currentTimeMillis()-sTime;
		System.out.println(" done by " + sTime );
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
}
