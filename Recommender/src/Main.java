import java.io.IOException;


public class Main {

	public static void main(String[] args) throws IOException {
		
		testDelicious();
//		testEpinion();
//		testMovielens();
		
	}
	
	public static void testEpinion() throws IOException{
		EpinionParser eps = new EpinionParser("epinion_data");
		eps.initial();
		eps.initialTrust();
		eps.writeSubSet();
		eps.getColdInfo();
	}
	
	public static void testDelicious() throws IOException{
		DeliciousParser dps = new DeliciousParser("delicious_data");
//		dps.reset();		
		dps.initial();
		dps.readRelation();
		dps.parseTagging();
		dps.writeTaggingDetail();
//		
//		
//		dps.writeUserCorrelation(true);
//		dps.writeDegreeDist();
//		// ======================only run once=======================		//
//		dps.writeItemCorrelation();		// calculate item correlation 
//		dps.writeItemPopularity();	// write item correlation to file 
//		dps.writeUserPopularity();	// write user correlation (pre-calculated) to file		
//		// ======================run once end =======================		
		dps.getItemBasedGraph();		// read item similarity from file!
		dps.getUserBasedGraph();		// read user similarity from file!
//		dps.getUserReachList();
//		dps.relationInf();
//		dps.getItemTrans();
//		dps.getItemTransForNow();
//		dps.getInfo();
//		dps.writeItemTrans(); // for observation
		
		System.out.println("\n--------------start validation period!----------------");
		Validator v = new Validator(dps);
		v.valid_range(0, 100);
		System.out.println("\n--------------all the process is done!----------------");
		
		
		
	}
	public static void testMovielens() throws IOException{
		MovielensParser mps = new MovielensParser("movielens_data");
		
	}
	
	
}

