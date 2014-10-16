package stockdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;

import stockdata.enhancer.ChoosenEnhancer;
import stockdata.enhancer.DaylineEnhancer;
import stockdata.enhancer.DaylineWebRefresher;
import stockdata.enhancer.ProbabilityEnhancer;
import bqdata.BQUtil;

public class DayLineRegister {
	private static DayLineRegister dayLineRegister=null;
	public static HashMap<String, ArrayList<DayLine>> stockDictionary = new HashMap<String, ArrayList<DayLine>> ();
	public static Hashtable<String, Integer> dayIndexTable = new Hashtable<String, Integer>();
	public static int lastDayIndex = 0;
	
	public static ChoosenEnhancer choosenEnhancer  = new ChoosenEnhancer();
	public static DaylineEnhancer daylineEnhancer = new DaylineEnhancer();
	public static ProbabilityEnhancer probabilityEnhancer = new ProbabilityEnhancer();
	
	private DayLineRegister(){
		
	}
	public static int futureDays = 5;
	public static int startday = 30;
	static boolean withRefresh = true;
	
	
	private void initializeDayIndexTable() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(Config.shIndexFile));
				    String s = new String();
				    int count=0;
				    while((s = in.readLine())!= null){
				    	if(count<=1){
				    		count++;
				    		continue;
				    	}else{
				    		String[] lineArray = s.split("\t");
				    		if(lineArray.length<3){
				    			continue;
				    		}
				    		String date = lineArray[0];
				    		dayIndexTable.put(date,count-2);
				    		count++;
				    	}
				    }
				    this.lastDayIndex = count-3;
				    in.close();
				    
				    
		    } catch (IOException e) {
				e.printStackTrace();
			}
		return;
	}
	
	public static BQUtil bqUtil;
	public static DayLineRegister getInstance(){
		if(dayLineRegister ==null){
			 dayLineRegister = new DayLineRegister();
			 dayLineRegister.initializeDayIndexTable();
			 dayLineRegister.initializeDayLines();	
			 if(withRefresh){
					DaylineWebRefresher webrefresher = new DaylineWebRefresher();
					webrefresher.refreshDayLineDictionary();
					
			 }
			 daylineEnhancer.enhanceDaylines();
			 daylineEnhancer.enhanceFutureIncrease(futureDays); 
			 bqUtil = new BQUtil(0);
		}
		return dayLineRegister;
	}
	
	///////////////////////
	//Read Day Lines from Files 
	//////////////////////
	private int getDayIndex(String dateString){
		return dayIndexTable.get(dateString);
	}
	private ArrayList<DayLine> getDayLineListByFile(File dayLineFile){
		ArrayList<DayLine> result = new ArrayList<DayLine>();
		if(dayLineFile.isDirectory()){
			return result;
		}
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(dayLineFile));
				    String s = new String();
				    int count=0;
				    String stockName ="";
				    String stockid = "";
				    while((s = in.readLine())!= null){
				    	
				    	if(count<=1){
				    		if(count==0){
				    			String[] lineArray = s.split(" ");
				    			stockName = lineArray[1];
				    			stockid = lineArray[0];
				    		}
				    		count++;
				    		continue;
				    	}else{
				    		String[] lineArray = s.split("\t");
				    		if(lineArray.length<=3){
				    			continue;
				    		}
				    		DayLine dl = new DayLine();
				    		dl.open = Float.parseFloat(lineArray[1]);
				    		dl.close = Float.parseFloat(lineArray[4]);
				    		
				    		dl.high = Float.parseFloat(lineArray[2]);
				    		dl.low = Float.parseFloat(lineArray[3]);
				    		dl.vol = Float.parseFloat(lineArray[5]);
				    		String dateString = lineArray[0];
				    		dl.date = dateString;
				    		dl.dateIndex = this.getDayIndex(dateString);
				    		String tempStockId = "";
				    		if(stockid.startsWith("6")){
				    			tempStockId = "sh"+stockid;
				    		}else{
				    			tempStockId = "sz"+stockid;
				    		}
				    		dl.stockid = tempStockId;
				    		dl.stockname = stockName;
				    		result.add(dl);
				    		
				    	}
				    }
				    
				    in.close();
		    } catch (IOException e) {
				e.printStackTrace();
			}
		
		return result;
	}
	
	public void initializeDayLines(){
		File bsDir = new File(Config.dayLineBSDir);
		File[] fileList = bsDir.listFiles();
		for(File dayLineFile: fileList){
			ArrayList<DayLine> dList = getDayLineListByFile(dayLineFile);
			if(dList!=null &&dList.size()>0){
				String stockid = dList.get(0).stockid;
				
				System.out.print(".");
				this.stockDictionary.put(stockid, dList);
			}
		}
		System.out.println();
	}
	
	private void outputStringToFile(String content, String filename){
		try {
			PrintWriter out1 = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			out1.print(content);
			out1.close();
		} catch (EOFException e) {
			System.err.println("End of stream");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public ArrayList<DayLine> getDayLinesByOffset(int offset){
		ArrayList<DayLine> result = new ArrayList<DayLine>();
		Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			if(size<=1) continue;
			DayLine today = dList.get(size-1-offset);
			//System.out.println(today.stockid+"   "+today.stockname+"  "+today.dateIndex+"   "+(this.lastDayIndex-offset));
			if(today.dateIndex==this.lastDayIndex-offset){
				result.add(today);
			}
		}
		return result;
	}
	
	private void outputLastDayListToFile(){
		String todayList = "";
		ArrayList<DayLine> result = new ArrayList<DayLine>();
		
		/*Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			if(size<=1) continue;
			DayLine today = dList.get(size-1);
			result.add(today);
			if(today.isChoosen){
				probabilityEnhancer.enhanceDayLineProbability(today);
			}
		}*/
		Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			if(size<=1) continue;
			DayLine today = dList.get(size-1);
			if(today.isChoosen){
				result.add(today);
			}
		}
		Collections.sort(result, new DayLineComparator());
		
		for(DayLine today: result){
			
			if(today.isChoosen&&today.probability>=75){
				System.out.println();
				System.out.println(today);
				String tempString ="";
				if(today.stockid.startsWith("sh")){
					tempString = today.stockid.replace("sh", "1");
				}else{
					tempString = today.stockid.replace("sz", "0");
				}
				todayList=todayList+tempString+"\n";
			}
		}	
		this.outputStringToFile(todayList, Config.BlockFile);
	}
	
	
    ///////////////////////
	// Get first round evidence
	//////////////////////
	private StatisticResult getPosiNegiResult(){
		float posi = 0;
		float negi = 0;
		
		Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		float achieveDaysTotal = 0;
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=startday;i<size;i++){	
				DayLine today = dList.get(i);
				if(today.isChoosen && today.futureIncreaseCalculated){
					if(today.futureSecondIncrease>=1){
						posi=posi+1;
					}else{
						negi=negi+1;
					}
					achieveDaysTotal = achieveDaysTotal+today.achieveDays;
				}
				
			}
		}
		float successRate = posi*100/(posi+negi);
		float total = posi+negi;
		return new StatisticResult(total, successRate, achieveDaysTotal/total);
		
	}
	
	private StatisticResult generateEvidence(Judger judger){
		System.out.println();
		/*if(withRefresh){
			DaylineWebRefresher webrefresher = new DaylineWebRefresher();
			webrefresher.refreshDayLineDictionary();
			daylineEnhancer.enhanceDaylines();
			daylineEnhancer.enhanceFutureIncrease(futureDays);
		}*/
		choosenEnhancer.enhanceChoosen(judger, false);
		probabilityEnhancer.enhanceLastDayProbability();
		/*if(outputLastDayListToFile){
			this.outputLastDayListToFile();
		}*/
		StatisticResult result = this.getPosiNegiResult();
		if(result.successRate>=0 && result.total>0){
			System.out.println("\n"+result);
			return result;
		}
		return new StatisticResult(0,0,futureDays);
	}
	
	public void generateEvidence(){
		float max = 100000;
		float min = -100000;
		
		float todaySlope3=20;
		float todayVolPercentUpper = 30;
		//float variance = 0;
		float shadowprotion=30;
		
		HashMap<String, JudgerUnit> rulerList = new HashMap<String, JudgerUnit>();
		
		rulerList.put("slope3",new JudgerUnit(todaySlope3, max));
		rulerList.put("volpercent", new JudgerUnit(min,todayVolPercentUpper));
		rulerList.put("variance",new JudgerUnit(min, max));
		rulerList.put("shadowprotion",new JudgerUnit(shadowprotion, max));

		Judger judger = new Judger(rulerList);
		generateEvidence(judger);
		this.outputLastDayListToFile();
		
	}
	
	///////////////////////
	// main
	//////////////////////
	public static void main(String[] args){
		Scanner scan = new Scanner(System.in);
		long start = Calendar.getInstance().getTimeInMillis();
		 DayLineRegister dlr = DayLineRegister.getInstance();
		 dlr.generateEvidence();
		long end = Calendar.getInstance().getTimeInMillis();
		long passed = (end-start)/1000;
		System.out.println("Time: "+passed);
		scan.nextLine();
		 // dlr.test();
	}
	
	
	///////////////////////
	// test
	//////////////////////
	public void test(){
		 float todaySlope3=60;
		 float todayVolPercentUpper = 34;
		 float variance = 0;
		 float shadowprotion=0;
		 DayLineRegister dlr = DayLineRegister.getInstance();
		 //dlr.probabilityTable = dlr.generageProbabilityTable();
		 

		 for (todaySlope3 = 20; todaySlope3 <= 20; todaySlope3 += 10) {
			for (todayVolPercentUpper = 30; todayVolPercentUpper <= 30; todayVolPercentUpper += 3) {
				for (variance = 6; variance <= 6; variance += 0.3) {
					for (shadowprotion = 30; shadowprotion <= 30; shadowprotion += 10) {
				
						//System.out.println(shadowprotion);
						HashMap<String, JudgerUnit> rulerList = new HashMap<String, JudgerUnit>();
						float max = 100000;
						float min = -100000;
						rulerList.put("slope3",new JudgerUnit(todaySlope3, max));
						rulerList.put("volpercent", new JudgerUnit(min,todayVolPercentUpper));
						//rulerList.put("variance",new JudgerUnit(3, 6));
						rulerList.put("variance",new JudgerUnit(min, max));
						rulerList.put("shadowprotion",new JudgerUnit(shadowprotion, max));

						Judger judger = new Judger(rulerList);
						dlr.generateEvidence(judger);
					}
				}	
			}
		 }
		 
		
	}
}
