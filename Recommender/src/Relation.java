import java.math.BigInteger;
import java.util.HashSet;


public class Relation implements Comparable{
	User fromU, toU;
	BigInteger timeStamp;
	double cor=0;
	HashSet<TagAction> InfRecord;
	public Relation(User u, User v, BigInteger ts){
		fromU = u;
		toU = v;
		timeStamp = ts;
		InfRecord = new HashSet<TagAction>();
	}
	public Relation(User u, User v){
		fromU = u;
		toU = v;
	}
		
	public String toString(){
		return fromU+"->"+toU+":"+cor;
	}
	

	public boolean equals(Object obj){
		Relation r = (Relation) obj;
		if(fromU.userID == r.fromU.userID && toU.userID == r.toU.userID){
			return true;
		}
		return false;
	}
//	public int compareTo(Object obj) {
//		Relation r = (Relation) obj;
//		return -1*this.timeStamp.compareTo(r.timeStamp);
//	}
	@Override
	public int compareTo(Object obj) {
		Relation r = (Relation) obj;
		if( this.InfRecord.size() >= r.InfRecord.size())
			return -1;
		else
			return 1;
	}
}
