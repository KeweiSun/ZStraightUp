package bqdata;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;

import stockdata.Config;
import stockdata.DayLine;
import stockdata.DayLineRegister;


public class BQUtil {
	public BQUtil(int offset){
		ArrayList<DayLine> daylineList = DayLineRegister.getInstance().getDayLinesByOffset(offset);
		daylineIncreaseTable = new Hashtable<String, Float>();
		for(DayLine dl: daylineList){
			daylineIncreaseTable.put(dl.stockid, dl.todayIncrease);
		}
		try {
			initializeBQIncreaseTable();
			initializeStockBQWeightTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
	}
	Hashtable<String, Float> daylineIncreaseTable;
	public Hashtable<String, BQInfo> bqInfoTable = new Hashtable<String, BQInfo>();
	public Hashtable<String, BQWeight> stockWeightTable = new Hashtable<String, BQWeight>();
	
	public String getStockBQWeight(String stockid){
		if(stockWeightTable.get(stockid)==null){
			return "No BQ";
		}
		float totalIncrease = stockWeightTable.get(stockid).bqTotalIncrease;
		float totalCount = stockWeightTable.get(stockid).bqCount;
		String details = stockWeightTable.get(stockid).bqDetails;
		return (Math.round(totalIncrease*100/totalCount)/100.0f)+" "+details;
	}
	public void initializeBQIncreaseTable() throws IOException{
		RandomAccessFile rf =new RandomAccessFile(Config.gnfilename, "r");
		 rf.seek(0x180);
		 int bqNumber = rf.readByte();
		 System.out.println(bqNumber);
		 
		 
		 for(int i=1;i<=bqNumber;i++){
			 byte[] bqName = new byte[9];
			 rf.read(bqName);
			 String bqNameString = new String(bqName).trim();
			 BQInfo banquai =new BQInfo();
			 
			 banquai.bqid = bqNameString;
			 
			 int stockNumber = rf.readShort();
			 if(stockNumber==0){
				 System.out.println("0 "+bqNameString);
			 }
			 
			 int bqlevel = rf.readShort();
			 byte[] stockNames = new byte[7*400];
			 rf.read(stockNames);
			 String StockNameString = new String(stockNames);
			 String[] stockArray = StockNameString.trim().split("\0");
			 banquai.stockCount = stockArray.length;
			 for(String stockId:stockArray){
				 String tempStockId = new String(stockId);
				 if(stockId.startsWith("0")||stockId.startsWith("3")){
					 tempStockId = "sz"+tempStockId;
				 }else{
					 tempStockId = "sh"+tempStockId;
				 }
					 
				 float todayIncrease = 0;
				 if(this.daylineIncreaseTable.get(tempStockId)!=null){
					 todayIncrease = this.daylineIncreaseTable.get(tempStockId);
				 }
				 banquai.stockTotalIncrease+=todayIncrease;//////
			 }
			 bqInfoTable.put(banquai.bqid, banquai);
		 }
		 Collection<BQInfo> bqlist = bqInfoTable.values();
		 ArrayList<BQInfo> bqresultList = new ArrayList<BQInfo>();
		 bqresultList.addAll(bqlist);
		 Collections.sort(bqresultList, new BQComparator());
		 for(BQInfo bq: bqresultList){
			 System.out.println(bq);
		 }
		 rf.close();
		 
	}
	
	public void initializeStockBQWeightTable() throws IOException{
		 RandomAccessFile rf =new RandomAccessFile(Config.gnfilename, "r");
		 rf.seek(0x180);
		 int bqNumber = rf.readByte();
		 for(int i=1;i<=bqNumber;i++){
			 byte[] bqName = new byte[9];
			 rf.read(bqName);
			 String bqNameString = new String(bqName).trim();
			 
			 int stockNumber = rf.readShort();
			 if(stockNumber==0){
				 System.out.println("0 "+bqNameString);
			 }
			 int bqlevel = rf.readShort();
			 byte[] stockNames = new byte[7*400];
			 rf.read(stockNames);
			 String StockNameString = new String(stockNames);
			 String[] stockArray = StockNameString.trim().split("\0");
			 for(String stockId:stockArray){
				 String tempStockId = new String(stockId);
				 if(stockId.startsWith("0")||stockId.startsWith("3")){
					 tempStockId = "sz"+tempStockId;
				 }else{
					 tempStockId = "sh"+tempStockId;
				 }
				 
				BQWeight bqWeightInfo = stockWeightTable.get(tempStockId);
				 
				if(bqWeightInfo==null){
					BQWeight bqWeight = new BQWeight();
					stockWeightTable.put(tempStockId, bqWeight);
					bqWeight.bqDetails = "";
				}
				
				BQWeight bqWeight = stockWeightTable.get(tempStockId);
				bqWeight.bqCount = bqWeight.bqCount+1;
				float increase = Math.round(bqInfoTable.get(bqNameString).stockTotalIncrease*100/bqInfoTable.get(bqNameString).stockCount)/100.0f;
				bqWeight.bqTotalIncrease = bqWeight.bqTotalIncrease+increase;
				bqWeight.bqDetails = bqWeight.bqDetails+bqNameString+" "+increase+",";
			 }
		 }
		 rf.close();
	}
	
	
	
	public static void main(String[] args){
		try {
			BQUtil util = new BQUtil(1);
			//util.getGN();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
