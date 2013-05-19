import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class DeliciousParser extends Parser{
	DirectedGraph<User, Influence> infGraph;
	ArrayList<TagAction> actionCorpus;
	ArrayList<Relation> rCorpus;
	
	TreeMap<Integer, String> url2Purl;
	TreeMap<Integer, Tag> id2tag;
	NumberFormat nf = NumberFormat.getInstance();
	File readFile;
	String fileTag = "minus";
	static final int MIN_ITEM_FOR_USER = 1;
	static final int MIN_USER_FOR_ITEM = 1;
	static final double DayDecay = 2, beta = 0.01;
	static final BigInteger big0 = new BigInteger("0"), bigDay = new BigInteger("86400000"), bigYear = new BigInteger("31536000000");
	String userPopPath, userCorPath, itemCorPath, itemPopPath, sourcePath, userContactPath, subsetPath;
	String itemTransPath, testSetPath, originPath, bookmarkPath, degreeDistPath, detailPath;
	String dataPath, rawPath, tagPath;
	
	public DeliciousParser(String dataSetPath) throws IOException{
		dataPath = dataSetPath;
		rawPath =  "./" + dataPath + "/hetrec2011-delicious-2k/user_taggedbookmarks-timestamps.dat";
		tagPath =  "./" + dataPath + "/hetrec2011-delicious-2k/tags.dat";
		
		userPopPath = "./" + dataPath + "/Analysis/user_popularity.dat_user"+MIN_ITEM_FOR_USER+"_item"+MIN_USER_FOR_ITEM+".dat";
		userCorPath = "./" + dataPath + "/Analysis/user_correlation_user"+MIN_ITEM_FOR_USER+"_item"+MIN_USER_FOR_ITEM+".dat";
		itemCorPath = "./" + dataPath + "/Analysis/item_correlation_user"+MIN_ITEM_FOR_USER+"_item"+MIN_USER_FOR_ITEM+".dat";
//		itemCorPath = "./" + dataPath + "/Analysis/item_correlation_global.dat";
		itemPopPath = "./" + dataPath + "/Analysis/item_popularity_user"+MIN_ITEM_FOR_USER+"_item"+MIN_USER_FOR_ITEM+".dat";
		sourcePath = "./" + dataPath + 		"/pre-processed/subset_user"+MIN_ITEM_FOR_USER+"_item"+MIN_USER_FOR_ITEM+".dat";
		userContactPath = "./" + dataPath + "/hetrec2011-delicious-2k/user_contacts-timestamps.dat"; 
		subsetPath = "./" + dataPath + 		"/pre-processed/subset_user"+MIN_ITEM_FOR_USER+"_item"+MIN_USER_FOR_ITEM+".dat"; 
		itemTransPath = "./" + dataPath + 	"/Analysis/item_trans_user"+MIN_ITEM_FOR_USER+"_item"+MIN_USER_FOR_ITEM+".dat";
		testSetPath = "./" + dataPath + 	"/Test/test_user"+MIN_ITEM_FOR_USER+"_item"+MIN_USER_FOR_ITEM+".dat";
		originPath = "./" + dataPath + 		"/pre-processed/user_tagged_no_repeat.dat";
		bookmarkPath = "./" + dataPath + 	"/hetrec2011-delicious-2k/bookmarks.dat";
		degreeDistPath = "./" + dataPath + 	"/Analysis/user_degree_distribution.dat";
		detailPath =  "./" + dataPath + "/Analysis/details.dat";
		
		// if subset file not exists, read the original file to build subset!
		readFile = new File(subsetPath);
		if(!readFile.exists()){
			readFile =  new File(originPath);
			System.out.println("!!!WARNING!!! source File not exists !!! writing new file!!!WARNING!!! ");
			this.initial();
			this.writeSubSet();
			System.exit(1);
		}
	}
	
	public void reset(){
		ArrayList<File> fileList = new ArrayList<File>();
		
		File userPopFile = new File(userPopPath);
		File userCorFile = new File(userCorPath);
		File itemCorFile = new File(itemCorPath);
		File itemPopFile = new File(itemPopPath);
		File sourceFile = new File(sourcePath);
		
		fileList.add(userPopFile);
		fileList.add(userCorFile);
		fileList.add(itemCorFile);
		fileList.add(itemPopFile);
		fileList.add(sourceFile);
		
		for(File tf : fileList){
			if(tf.exists()) {
				System.out.print("delete "+ tf.getName() + " ...");
				tf.delete();
				if(!tf.exists()) System.out.println("... Success");
				else System.out.println(" Fail");
			}
		}		
	}
	
	public void initial() throws IOException{
		double sTime = System.currentTimeMillis();
		System.out.print("Initializing  user>" + MIN_ITEM_FOR_USER + "  item>"+ MIN_USER_FOR_ITEM + " ... " );
		String fileline = null;
		int uID, bID=0, tID=0, vID, lastID = 0;
		BigInteger ts = null;
		infGraph = new DirectedSparseMultigraph<User, Influence>(); 
		actionCorpus = new ArrayList<TagAction>();
		rCorpus = new ArrayList<Relation>();
		id2user = new TreeMap<Integer, User>();
		id2item = new TreeMap<Integer, Item>();
		id2tag = new  TreeMap<Integer, Tag>();
		url2Purl = new TreeMap<Integer, String>();
		User u = null, v= null;
		Item b = null;
		
		BufferedReader freader = new BufferedReader(new FileReader(readFile));
		
		// scan the action corpus 
		fileline=freader.readLine();
		while( (fileline=freader.readLine())!=null ){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			bID = Integer.parseInt(fileline.split("\t")[1]);
			tID = Integer.parseInt(fileline.split("\t")[2]);
			u = getUser(uID);
			b = getItem(bID);
			u.lastItem = b;
			u.lastTS = new BigInteger( fileline.split("\t")[3] );
		}
		// re-scan to exclude the last one and parse
		freader = new BufferedReader(new FileReader(readFile));
		fileline=freader.readLine();
		while( (fileline=freader.readLine())!=null ){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			bID = Integer.parseInt(fileline.split("\t")[1]);
			tID = Integer.parseInt(fileline.split("\t")[2]);
			ts = new BigInteger( fileline.split("\t")[3] );
			u = getUser(uID);
			b = getItem(bID);
			if(bID != u.lastItem.itemID){
				TagAction t = new TagAction(uID, bID, tID, ts);
				actionCorpus.add(t);
				u.itemList.add(b);
				u.actionList.add(t);
				b.userList.add(u);
			}
		}
		freader.close();
		HashSet<User> removeSet = new HashSet<User>(); 
		// get relation between users

		sTime = System.currentTimeMillis()-sTime;
		System.out.println(" done by " + sTime);
	}
	
	public void readRelation() throws IOException{
		BufferedReader freader = new BufferedReader(new FileReader(userContactPath));
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
				Relation r = new Relation(u, v, ts);
				r.cor = userCor(u,v);
				rCorpus.add(r);
				u.trustList.add(v);
				v.trustList.add(u);
				u.outreList.add(r);
				v.inreList.add(r);
			}
		}
		int rSize=0;
		for(User us : id2user.values()){
			rSize += us.inreList.size();
		}
		freader.close();
		System.out.println(
				"[action:" + actionCorpus.size() +
				" user:" + id2user.size() +
				" item:" + id2item.size() +
				" relation:" + rSize +
				"]");
	}
	
	public void parseTagging() throws IOException{
		double sTime = System.currentTimeMillis();
		System.out.print("Parsing tag values..." );
		BufferedReader freader = new BufferedReader(new FileReader(rawPath));
		int uID, bID=0, tID=0, vID, lastID = 0;
		BigInteger ts = null;
		User u = null, v= null;
		Item i = null;
		Tag tag = null;
		String tagWord, URL;
		// read the whole database 
		String fileline = freader.readLine(); // skip the first row
		// read the tag info word  into memory
		freader = new BufferedReader(new FileReader(tagPath));
		fileline = freader.readLine(); // skip the first row
		while( (fileline=freader.readLine())!=null ){
			tID = Integer.parseInt(fileline.split("\t")[0]);
			tagWord = fileline.split("\t")[1];			
			tag = new Tag(tID, tagWord);
			id2tag.put(tID, tag);
		}
		freader.close();
		
		// read the bookmark info word  into memory
		freader = new BufferedReader(new FileReader(bookmarkPath));
		fileline = freader.readLine(); // skip the first row
		while( (fileline=freader.readLine())!=null ){
			bID = Integer.parseInt(fileline.split("\t")[0]);
			URL = fileline.split("\t")[3];
			if(id2item.containsKey(bID)) i = getItem(bID);
			i.bookmarkURL = URL; 
		}
		freader.close();
		
		// sum of item2tag and user2tag maps
		for(TagAction action : actionCorpus){
			i = id2item.get(action.itemID);
			u = id2user.get(action.userID);
			tag = id2tag.get(action.tagID);
			
			addValue2map(u.tagID2freq, tag.tagID, 1.0);
			
			tag.actionSet.add(action);
			tag.tagTime++;
			addValue2map(tag.itemID2freq, i.itemID, 1.0);
			addValue2map(tag.userID2freq, u.userID, 1.0);
		}
		
		sTime = System.currentTimeMillis()-sTime;
		System.out.println(" done! by " + sTime);
	}
	
	
	public void printTaggingInfo(){
		/* 
		 * print each user's number of different tags. 	
		 */
		TagComparator tComp = new TagComparator(id2tag);
		TreeMap<Integer, Tag> id2t = new TreeMap<Integer, Tag>(tComp);
		id2t.putAll(id2tag);
		int i =0;
		for(Tag t : id2t.values()){
//			System.out.println(t);
			if(i++ > 100)
				break;
		}
		for(User u : id2user.values()){
			System.out.println(u + "\t" + u.tagID2freq.size() );
		}
	}
	
	public void writeTaggingDetail() throws IOException{
		String fileline;
		int uID, bID, tID;
		BigInteger ts;
		User u;
		Item i;
		Tag t;
		BufferedWriter fwriter = new BufferedWriter(new FileWriter(detailPath));
		BufferedReader freader = new BufferedReader(new FileReader(originPath));
		fwriter.write("userID	bookmarkID	tagID	timestamp\n");
		fileline=freader.readLine();
		while( (fileline=freader.readLine())!=null){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			bID = Integer.parseInt(fileline.split("\t")[1]);
			tID = Integer.parseInt(fileline.split("\t")[2]);
			ts = new BigInteger( fileline.split("\t")[3] );
			u = getUser(uID);
			i = getItem(bID);
			t = id2tag.get(tID);
			fwriter.write(uID+"\t"+bID+"\t"+tID+"\t"+i.bookmarkURL + "\t" + t.tagID+":"+t.tagWord + "\ttagUser:"+t.userID2freq.size() + "\n");
		}
		fwriter.close();
		freader.close();
	}
	
	public void writeDegreeDist()  throws IOException{
		double sTime = System.currentTimeMillis();
		System.out.print("Applying ALL-BUT-ONE... ");
		BufferedWriter fw = new BufferedWriter(new FileWriter(degreeDistPath));
		
		for(User u : id2user.values()){
			fw.write(u.userID + "\t" + u.trustList.size() +"\n");
		}

		fw.close();
		System.out.println("Done!");
		sTime = System.currentTimeMillis()-sTime;
		
	}
	
	public void getInfo(){
		double itemSum = 0.0;
		double tagAvg = 0.0;
		double booktagSum = 0.0;
		double booktagAvg = 0.0;
		for(User u : id2user.values()){
			itemSum += u.actionList.size();
			
		}
		for(Item b : id2item.values()){
			booktagSum += b.userList.size();
		}
		booktagAvg = booktagSum/id2item.size();
		tagAvg = itemSum/id2user.size();
//		System.out.println("tagAvg=" + tagAvg);
//		System.out.println("bookmark tagAvg=" + booktagAvg);
	}
	
	public void getItemBasedGraph() throws IOException{
		BufferedReader freader = new BufferedReader(new FileReader(itemCorPath));
		String fileline;
		int iID, jID;
		Item item_i, item_j;
		double sim=0.0, comSize = 0.0, simBySig;
		System.out.print("getItemBasedGraph():::\tReading item correlation to item graph...");
		while( (fileline=freader.readLine())!=null ){
			iID = Integer.parseInt(fileline.split("\t")[0]);
			jID = Integer.parseInt(fileline.split("\t")[1]);
			sim = Double.parseDouble(fileline.split("\t")[2]);
			comSize = Double.parseDouble(fileline.split("\t")[3]);
			item_i = getItem(iID);
			item_j = getItem(jID);
			
			sim = sim*sigmoid(comSize);
//			sim = comSize;
			item_i.nearID2cor.put(jID, sim);
			item_j.nearID2cor.put(iID, sim);
			
			item_i.nearID2comSize.put(jID, comSize);
			item_j.nearID2comSize.put(iID, comSize);
		}	
		System.out.println(" Done!");
	}
	
	
	public void getUserBasedGraph() throws IOException{
		BufferedReader freader = new BufferedReader(new FileReader(userCorPath));
		String fileline;
		
		int uID, vID;
		User u, v;
		double sim=0.0;
		System.out.print("getUserBasedGraph():::\tReading user correlation to user graph...");
		while( (fileline=freader.readLine())!=null ){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			vID = Integer.parseInt(fileline.split("\t")[1]);
			sim = Double.parseDouble(fileline.split("\t")[2]);
			
			u = getUser(uID);
			v = getUser(vID);
			u.nearID2cor.put(vID, sim);
			v.nearID2cor.put(uID, sim);
		}
		System.out.println(" Done!");
	}
	public void getUserReachList(){
		double reachTime=0.0;
		for(User u: id2user.values()){
			for(User nearU : u.trustList){
				for(Item i : nearU.itemList){
					if(!u.reachMap.containsKey(i)){
						u.reachMap.put(i, 1.0);
					}else{
						reachTime = u.reachMap.get(i);
						u.reachMap.put(i, reachTime+1);
					}
				}
			}
		}
	}

	public void relationInf() throws IOException{
		System.out.print("relationInf():::\tWriting social trust and influence file...");
		BufferedWriter fw = new BufferedWriter(new FileWriter("./delicious_data/Analysis/user_contacts-size-inf.dat"));
		BufferedWriter infw = new BufferedWriter(new FileWriter("./delicious_data/Analysis/user_inf_timestamps.dat"));
		HashSet<Item> commonList = null;
		Influence inf = null;
		double sTime = System.currentTimeMillis();
		double infCount = 0, infTotal=0, comTotal=0, reTotal=0, comSize =0;
		BigInteger afterTime, uvDiff;
		System.out.print("rCorpus: " + rCorpus.size());
		
		for(Relation r : rCorpus){
			infCount = 0;
			User u_fromU = r.fromU;
			User u_toU = r.toU;
			commonList = getCommonList(u_fromU,u_toU); 

			for(Item b : commonList){
				TagAction t_fromU = findTag(b, u_fromU);
				TagAction t_toU = findTag(b, u_toU);
				if(t_fromU==null || t_toU == null) break;
				afterTime = t_toU.timestamp.subtract(r.timeStamp);	// afterTime: time interval after Relation construction
				if( (inf = infGraph.findEdge(u_fromU, u_toU))==null){
					inf = new Influence(u_fromU, u_toU, afterTime);
					infGraph.addEdge(inf, u_fromU, u_toU, EdgeType.DIRECTED);
				}else{
					inf.interval.add(afterTime);
				}
				uvDiff = t_toU.timestamp.subtract(t_fromU.timestamp);
				// check if tagging is (1) after create relation to u  (2) v takes after u
				if( afterTime.compareTo(big0) > 0  && uvDiff.compareTo(big0) > 0 ){
					infCount++;
					u_fromU.addPower(u_toU, uvDiff);
					infw.write(u_fromU.userID +"\t" + u_toU.userID + "\t"+ b.itemID + "\t"+uvDiff+"\t" + uvDiff.divide(bigDay) +"\n");
				}
			}
			comSize = commonList.size();
			if(comSize > 0){
			fw.write(u_fromU.userID + "\t" + u_toU.userID + "\t"+ 
					comSize	 /(u_fromU.itemList.size() + u_toU.itemList.size()-commonList.size())+"\t"+
					infCount /(u_fromU.itemList.size() + u_toU.itemList.size()-commonList.size())+"\t"+
					u_fromU.itemList.size() + "\t"+ u_toU.itemList.size() + 
					"\tc:"+commonList.size() + "\tinfC:"+ infCount +"\t"+ commonList + "\n");
			}
			infTotal += infCount;
			comTotal += commonList.size();
			if(commonList.size() >0 ) reTotal++;
		}
		fw.close();
		infw.close();
		System.out.println(" done! by " + (System.currentTimeMillis()-sTime) +" ms." );
		System.out.println("\t\t\tTotal " + infTotal + " / " + comTotal +" tagging is influenced with relations: " + reTotal+"/"+rCorpus.size() );
		// infTotal : common tagging with social influence
		// comTotal : total common tagging count
		// reTotal  : total relation with social influence
	}
	
	public void printiPower(){
		int iCount = 0;
		for(User u : id2user.values()){
			u.getPower();
			if(u.iPower > 0){
				System.out.println(iCount++ +"\t"+ u.userID  +"\t" + u.user2power);
//				System.out.println(iCount++ +"\t"+ u.userID  +"\t" + u.iPower );
			}
		}
	}
	
	public void getItemTrans(){
		System.out.print("getTransList():::\tcalculating item sim by time... ");
		BigInteger timeDiff;
		int dayDiff = 0;
		Item i1, i2;
		double score = 0.0;
		int totalR = 0;
		for(User u : id2user.values()){
			for(TagAction t1 : u.actionList){
			for(TagAction t2 : u.actionList){
				if(t1.itemID < t2.itemID){
					i1 = getItem(t1.itemID);
					i2 = getItem(t2.itemID);
					timeDiff = t1.timestamp.subtract(t2.timestamp).abs();
					dayDiff = timeDiff.divide(bigDay).intValue();
					score = Math.pow(DayDecay, dayDiff);
					score = score * sigmoid(u.itemList.size());
					score = score / (i1.userList.size() + i2.userList.size() -itemComSize(i1,i2));
					
					addValue2map(i1.nearID2trans, i2.itemID, score);
					addValue2map(i2.nearID2trans, i1.itemID, score);
					totalR++;
				}
			}
			}
			
		}
		System.out.println(" Done! total link: " + totalR);
	}
	public void getItemTransForNow(){
		System.out.print("getItemTransForNow():::\tcalculating item sim by time... ");
		BigInteger timeDiff;
		int dayDiff = 0;
		Item i1, i2;
		double score = 0.0;
		int totalR = 0;
		for(User u : id2user.values()){
			for(TagAction t1 : u.actionList){
			for(TagAction t2 : u.actionList){
				if(t1.itemID < t2.itemID){
					i1 = getItem(t1.itemID);
					i2 = getItem(t2.itemID);
					timeDiff = t1.timestamp.subtract(u.lastTS).abs();
					dayDiff = timeDiff.divide(bigDay).intValue();
					score = Math.pow(DayDecay, dayDiff);
					score = score / (i1.userList.size() + i2.userList.size() -itemComSize(i1,i2));
					addValue2map(i2.nearID2trans, i1.itemID, score);
					
					timeDiff = t2.timestamp.subtract(u.lastTS).abs();
					dayDiff = timeDiff.divide(bigDay).intValue();
					score = Math.pow(DayDecay, dayDiff);
					score = score / (i1.userList.size() + i2.userList.size() - itemComSize(i1,i2) );
					addValue2map(i1.nearID2trans, i2.itemID, score);
					totalR++;
				}
			}
			}
			
		}
		System.out.println(" Done! total link: " + totalR);
	}
	
	
	public double writeItemCorrelation() throws IOException{
		double sTime = System.currentTimeMillis();
		double iCount = 0, subCount=0;
		double commonSize = 0.0, c = 0.0;
		double cSum = 0.0;
		double totalPair = id2item.size()*(id2item.size()-1);
		System.out.print("writeItemCorrelation()::: Get correlation between all items... ");
		File f = new File(itemCorPath);
		if(f.exists()){
			System.out.println("exists");
			return 0.0;
		}else{
			System.out.println("not exists");
		}
		System.out.println("start writing for " +id2item.size() + " items!!!");
		BufferedWriter fw = new BufferedWriter(new FileWriter(f));
		for(Item i : id2item.values()){
			for(Item j : id2item.values()){
				if( i.itemID < j.itemID){
					commonSize = itemComSize(i,j);
					c = commonSize/(i.userList.size() + j.userList.size() -commonSize);
					if(commonSize > 0.0){						
						fw.write(i.itemID+"\t"+j.itemID +"\t"+ c + "\t"+commonSize+"\n");
					}
					cSum += commonSize;
					iCount++;
				}
//				if( iCount%1000000 == 1 ) System.out.println(iCount/totalPair +" done!");
			}

		}
		
		fw.close();
		sTime = System.currentTimeMillis()-sTime;
		System.out.println(" done! by " + sTime +" ms." );
		return cSum/iCount;
	}
	
	public double writeUserCorrelation(boolean write) throws IOException{
		double sTime = System.currentTimeMillis();
		int uCount = 0, cCount=0;
		double c = 0.0;
		double cSum = 0.0, rSum =0.0, rCount =0.0, aCount=0;
		BufferedWriter fw = null;
		System.out.print("writeUserCorrelation()::: Get correlation between all users...");
		File f= new File(userCorPath);
		if(!f.exists())System.out.println("not exist!!! ");
		if(write) fw = new BufferedWriter(new FileWriter(f)); 
		for(User u : id2user.values()){
			for(User v : id2user.values()){
				if( v.userID > u.userID){
					c = userCor(u,v);
					if(u.trustList.contains(v)){
						c = c + beta;
					}
					// write all non-zero to file
					if(c > 0.0){
						cCount++;
						if( u.trustList.contains(v) ){
							if(write)	fw.write(u.userID+"\t"+v.userID +"\t"+ c + "\t"+ "1");
							rSum += c;
							rCount++;
						}
						else{
							if(write)	fw.write(u.userID+"\t"+v.userID +"\t"+ c);
						}
						if(write)	fw.write("\n");
						cSum += c;
						uCount++;
					}
				}
				aCount++;
			}
		}
		if(write)  {
			fw.close();
		}
		sTime = System.currentTimeMillis()-sTime;
		System.out.println(" done! by " + sTime +" ms." );
		nf.setMaximumFractionDigits( 5 );
		System.out.println("Avg Relation Cor: " + nf.format(rSum/rCount) +"  " + nf.format(rSum/(rCorpus.size()/2))+"\ttotal relation: "+ rCount +"/" + (rCorpus.size()/2) );
		System.out.println("Avg Users    Cor: " + nf.format(cSum/uCount) +"  " + nf.format(cSum/aCount) + "\ttotal with cor: "+ uCount+"/"+aCount);
		return cSum/uCount;
	}
	
	public HashSet<Item> getCommonList(User u, User v){
		HashSet<Item> commonList = new HashSet<Item>(); 
		for(Item b : u.itemList){
			if(v.itemList.contains(b))
				commonList.add(b);
		}
		return commonList;
	}

	
	public void writeSubSet() throws IOException{	
		System.out.print("writeSubSet():::\tBuilding Subset user" + MIN_ITEM_FOR_USER + " item" + MIN_USER_FOR_ITEM + "...");
		
		double sTime = System.currentTimeMillis();
		String fileline = null;
		int uID, bID, tID, vID, lastID = 0, lastuID = 0;
		BigInteger ts;
		User u = null, v= null;
		Item b = null;
		BufferedReader freader = new BufferedReader(new FileReader(originPath));
		BufferedWriter fwriter = new BufferedWriter(new FileWriter(subsetPath));
		fwriter.write("userID	bookmarkID	tagID	timestamp\n");
		fileline=freader.readLine();
		while( (fileline=freader.readLine())!=null){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			bID = Integer.parseInt(fileline.split("\t")[1]);
			tID = Integer.parseInt(fileline.split("\t")[2]);
			ts = new BigInteger( fileline.split("\t")[3] );
			u = getUser(uID);
			b = getItem(bID);
			if(b.userList.size() >= MIN_USER_FOR_ITEM && u.itemList.size() >= MIN_ITEM_FOR_USER){
				fwriter.write(fileline+"\n");
			}
			lastuID = uID;
			lastID = bID;
		}
		fwriter.close();
		freader.close();
		sTime = System.currentTimeMillis()-sTime;
		System.out.println(" done by " + sTime );
	}
	
	public void writeItemPopularity() throws IOException{
		File f = new File(itemPopPath);
		if(f.exists()) return ;
		BufferedWriter fw = new BufferedWriter(new FileWriter(f));
		ArrayList<Item> bookmarkSizeRank = new ArrayList<Item> ();
		for(Item b : id2item.values()){
			fw.write(b.itemID +"\t"+ b.userList.size() +"\n");
		}
		fw.close();
	}
	public void writeUserPopularity() throws IOException{
		File f = new File(userPopPath);
		if(f.exists()) return ;
		BufferedWriter fw = new BufferedWriter(new FileWriter(f));
		for(User u : id2user.values()){
			fw.write(u.userID +"\t"+ u.itemList.size() +"\n");
		}
		fw.close();
	}
	public void writeItemTrans() throws IOException{
		File f = new File(itemTransPath);
		BufferedWriter fw = new BufferedWriter(new FileWriter(f));
		for(Item b : id2item.values()){
			for(Integer iID : b.nearID2trans.keySet()){
				fw.write(b.itemID +"\t"+ iID+"\t"+b.nearID2trans.get(iID)+"\n");
//				fw.write(b.itemID +"\t"+ iID+"\t"+b.nearID2cor.get(iID)+"\n");
			}
		}
		fw.close();
	}
	
	public double userCor(User u, User v){
		ArrayList<Item> commonList = new ArrayList<Item>(); 
		for(Item b : u.itemList){
			if(v.itemList.contains(b))
				commonList.add(b);
		}
//		System.out.println(u.markList.size() + "\t" + v.markList.size() + "\t"+ commonList.size());

		return (double) commonList.size() / (double) (u.itemList.size() + v.itemList.size() - commonList.size() );
	}
	
	public double userCor(int id1, int id2){
		return userCor(id2user.get(id1), id2user.get(id2));
	}
	
	public double itemComSize(Item i, Item j){
		ArrayList<User> commonList = new ArrayList<User>();
		for(User u : i.userList){
			if(j.userList.contains(u))
				commonList.add(u);
		}
		
		return (double) commonList.size();
	}
	public double userComNeighbor(User u , User v){
		ArrayList<User> commonList = new ArrayList<User>();
		for(User us : v.trustList){
			if(u.trustList.contains(us))
				commonList.add(us);
		}
		
		return (double) commonList.size();
	}
	public void countPrincipal() throws IOException{
		BufferedReader freader = new BufferedReader(new FileReader(bookmarkPath));
		String fileline = null, pUrl;
		int bID;		
		HashSet<String> pUrlList = new HashSet<String>();
		fileline = freader.readLine();
		while( (fileline=freader.readLine())!=null){
			bID = Integer.parseInt(fileline.split("\t")[0]);
			System.out.println(bID);
			pUrl = fileline.split("\t")[5];
			pUrlList.add(pUrl);
		}
		System.out.println("p url size: " + pUrlList.size());
//		System.out.println(pUrlList);
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
	public TagAction findTag(Item b, User u){
		for(TagAction t : u.actionList){
			if(t.itemID == b.itemID)
				return t;
		}
		return null;
	}
	
	public boolean containBook(ArrayList<Item> list, Item bo){
		for(Item b : list){
			if(b.itemID == bo.itemID)
				return true;
		}
		return false;
	}
	
	
	
}

class TagComparator implements Comparator<Integer> {

    Map<Integer, Tag> base;

    TagComparator(Map<Integer, Tag> base) {
        this.base = base;
    }

    @Override
    public int compare(Integer a, Integer b) {
        if (base.get(a).tagTime >= base.get(b).tagTime) {
            return -1;
        } else {
            return 1;
        }
    }

}