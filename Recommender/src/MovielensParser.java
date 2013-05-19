import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.TreeMap;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;


public class MovielensParser extends Parser {

	
	File readFile;
	String dataPath, originRatingPath, originTaggingPath, coldUserPath, tagPath;
	
	public MovielensParser(String dataSetPath) throws IOException{
		dataPath = dataSetPath;
		originRatingPath = "./" + dataPath +"/hetrec2011-movielens-2k/user_ratedmovies-timestamps.dat";
		originTaggingPath = "./" + dataPath + "/hetrec2011-movielens-2k/user_taggedmovies-timestamps.dat";
		tagPath= "./" + dataPath + "/hetrec2011-movielens-2k/tags.dat";
	}
	
	public void initial() throws IOException{
		double sTime = System.currentTimeMillis();
		System.out.print("Initializing" + " ... " );
		String fileline = null;
		int uID=0, iID=0, tID=0, vID, lastID = 0;
		BigInteger ts = null;
		ratingCorpus = new ArrayList<Rating>();
		id2user = new TreeMap<Integer, User>();
		id2item = new TreeMap<Integer, Item>();
		User u = null, v= null;
		Item i = null;
		
		BufferedReader freader = new BufferedReader(new FileReader(originRatingPath));
		fileline=freader.readLine();
		while( (fileline=freader.readLine())!=null ){
			uID = Integer.parseInt(fileline.split("\t")[0]);
			iID = Integer.parseInt(fileline.split("\t")[1]);
			tID = Integer.parseInt(fileline.split("\t")[2]);
			u = getUser(uID);
			i = getItem(iID);
			u.lastItem = i;
			u.lastTS = new BigInteger( fileline.split("\t")[3] );
		}
		
	}
	
}
