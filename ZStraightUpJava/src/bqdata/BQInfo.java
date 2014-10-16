package bqdata;


public class BQInfo {

	public String bqid;
	public int stockCount;
	public float stockTotalIncrease;
	
	public String toString(){
		return bqid+ "  "+stockCount+"    "+(stockTotalIncrease/stockCount);
	}
    
	
}
