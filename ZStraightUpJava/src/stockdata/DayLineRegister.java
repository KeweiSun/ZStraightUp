package stockdata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class DayLineRegister {
	private static DayLineRegister dayLineRegister=null;
	public HashMap<String, ArrayList<DayLine>> stockDictionary = new HashMap<String, ArrayList<DayLine>> ();
	private DayLineRegister(){
		
	}
	static int futureDays = 5;
	int startday = 30;
	
	public static DayLineRegister getInstance(){
		if(dayLineRegister ==null){
			 dayLineRegister = new DayLineRegister();
			 dayLineRegister.initializeDayLines();
			 dayLineRegister.enhanceDaylines();
			 dayLineRegister.enhanceFutureIncrease(futureDays); 
		}
		return dayLineRegister;
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
	
	private static ArrayList<DayLine> getDayLineListByFile(File dayLineFile){
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
				    		dl.dateIndex = 0;
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
	
	public String loadFromFile(String filename){
		StringBuffer result = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
				    String s = new String();
				    while((s = in.readLine())!= null){
				    	//result = result+s;
				    	result.append(s);
				    }
				    in.close();
		    } catch (IOException e) {
				e.printStackTrace();
			}
		return result.toString();
	}
	
	public synchronized void appendBuffer(StringBuffer totalBuffer, String tdString){
		totalBuffer.append(tdString);
	}
	
	
	
	private ArrayList<String> getUrlList(){
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> stockListArrayList = new ArrayList<String>();
		stockListArrayList.addAll(stockDictionary.keySet());
		String[] stockListArray=new String[stockListArrayList.size()];
		stockListArrayList.toArray(stockListArray);
		
		String idListString = "";
		for(int i=0;i<stockListArray.length;i++){
			if(i>0&&(i%100==0||i==stockListArray.length-1)){
				idListString = idListString+stockListArray[i];
				String url = "http://hq.sinajs.cn/list="+idListString;
				result.add(url);
				idListString = "";
			}else{
				idListString = idListString+stockListArray[i]+",";
			}
		
		}
		
		return result;
	}
	private ArrayList<String> getContentFromUrl(String url) throws IOException{
		ArrayList<String> result = new ArrayList<String>();
		URL getUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        connection.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"gb2312"));
        String lines;
        while ((lines = reader.readLine()) != null){
           result.add(lines);
           
        } 		        
        reader.close();
        connection.disconnect();
        return result;
		
		
	}
	private void getCurrentDayLine(String lines){
		 String[] titledata = lines.split("=\"");
         String title = titledata[0];
         String data = titledata[1];
         if(data.split(",").length<3){
         	return;
         }
         String symbol = title.substring(title.lastIndexOf("_")+1);
         String date = data.split(",")[30];
         String time = data.split(",")[31];
         float open = Float.parseFloat(data.split(",")[1]);
         float close = Float.parseFloat(data.split(",")[3]);
         float high = Float.parseFloat(data.split(",")[4]);
         float low = Float.parseFloat(data.split(",")[5]);
         float vol = Float.parseFloat(data.split(",")[8]);
         float amount = Float.parseFloat(data.split(",")[9]);
         
         String year = date.split("-")[0];
         String month =date.split("-")[1];
         String day =date.split("-")[2];
         date =month+"/"+day+"/"+year;
         
         
         int hour = Integer.parseInt(time.split(":")[0]);
         int min = Integer.parseInt(time.split(":")[1]);
         
         double rate = this.getRate(hour, min);
         vol = (float)(vol*rate);
         amount = (float)(amount*rate);
         vol = new BigDecimal(vol).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
         amount = new BigDecimal(amount).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
         DayLine dl = new DayLine();
         dl.date = date;
     	 dl.dateIndex = 0;
     	 dl.open = open;
     	 dl.high = high;
     	 dl.low = low;
     	 dl.close = close;
     	 dl.amount = amount;
     	 dl.vol = vol;
     	 dl.stockid = symbol;
     	 dl.stockname = data.split(",")[0];
         
     	 ArrayList<DayLine> dayLines = stockDictionary.get(dl.stockid);
     	 int linesSize = dayLines.size();
     	 if(dayLines.get(linesSize-1).date.equals(dl.date)){
     		 System.out.println("in");
     		 dayLines.set(linesSize-1, dl);
     	 }else{
     		 dayLines.add(dl);
     	 }
	}
	private double getRate(int hour, int min){
		
		if(hour<12){
       	 hour = hour-9;
       	 min = min-30;
        }else{
       	 hour=hour-11;
        }
        int minpassed = hour*60+min;
        if(minpassed==0){
       	 minpassed = 1;
        }
        double rate = 60*4.0/minpassed;
        if(rate < 1){
       	 rate = 1;
        }
       return rate;
	}
	
	public void refreshDayLineDictionary() {	
		try {
			ArrayList<String> urlList = this.getUrlList();
			for(String url:urlList){
			        ArrayList<String> lineList;
					lineList = this.getContentFromUrl(url);
					System.out.print("-");
			        for(String line:lineList ){
			        	this.getCurrentDayLine(line);
			        }
			       
			}
			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void enhanceChoosen(Judger judger, boolean isProbabilityChoosen){
		
		Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=startday;i<size;i++){	
				DayLine today = dList.get(i);
				if(today.vol>0 && today.todayIncrease<9.89){
					DayProfile profile = new DayProfile(today);
					if(isProbabilityChoosen){
						today.probabilityChoosen = judger.accept(profile);
					}else{
						today.isChoosen = judger.accept(profile);
					}
					
				}
			}
		}	
	}
	
	
	
	
	private void enhanceFutureIncrease(int step){
		Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=startday;i<size;i++){	
				DayLine today = dList.get(i);
				if(i<size-step-1){
					float highest = -10000;
					float secondHighest = -10000;
					float days = step;
					for(int m=1;m<=step;m++){
						
						DayLine dayline = dList.get(i+m);
						float factor = dayline.high;
						if(factor  > highest){
							secondHighest = highest;
							highest =factor;		
						}else if(factor  > secondHighest){
							secondHighest = factor;
						}
						float tempIncrease = (highest-today.close)*100/today.close;
						if(tempIncrease>=2&&days==step){
							days = m;
						}
						
					}
					today.futureIncrease = (highest-today.close)*100/today.close;
					today.futureSecondIncrease = (secondHighest-today.close)*100/today.close;
					today.futureIncreaseCalculated = true;
					today.achieveDays = days;
				}
				
			}
		}	
	}
	
	private void outputLastDayListToFile(){
		String todayList = "";
		ArrayList<DayLine> result = new ArrayList<DayLine>();
		Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			if(size<=1) continue;
			DayLine today = dList.get(size-1);
			result.add(today);
			if(today.isChoosen){
				StatisticResult innerresult = getDayLineProbility(today,5,5,1,5);
				StatisticResult outerresult = getDayLineProbility(today,7,7,1.4f,7);
				
				float total = outerresult.total;
				float probability = (innerresult.successRate*innerresult.total+outerresult.successRate*outerresult.total)/(innerresult.total+outerresult.total);
				float achieveDays = (innerresult.successDays*innerresult.total+outerresult.successDays*outerresult.total)/(innerresult.total+outerresult.total);
				StatisticResult probresult = new StatisticResult(total, probability, achieveDays);
				
				today.total = probresult.total;
				today.probability = probresult.successRate;
				today.achieveDays = probresult.successDays;
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
	
	private StatisticResult getDayLineProbility(DayLine today, float slopeRange, float volpercentRange, float varianceRange, float protionRange) {
		JudgerUnit slope3Unit = new JudgerUnit(today.slope3-slopeRange, today.slope3+slopeRange);
		JudgerUnit percentUnit = new JudgerUnit(today.volpercent-volpercentRange, today.volpercent+volpercentRange);
		JudgerUnit varianceUnit = new JudgerUnit((float)today.variance-varianceRange, (float)today.variance+varianceRange);
		JudgerUnit shadowUnit = new JudgerUnit(today.shadowprotion-protionRange, today.shadowprotion+protionRange);
		
		HashMap<String, JudgerUnit> rulerList = new HashMap<String, JudgerUnit>();
		rulerList.put("slope3",slope3Unit);
		rulerList.put("volpercent", percentUnit);
		rulerList.put("variance",varianceUnit);
		rulerList.put("shadowprotion",shadowUnit);
		
		Judger judger = new Judger(rulerList);
		StatisticResult result = this.generateSpecProbability(judger, 5);
		return result;
		
	}
	

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
	private StatisticResult getProbabilityPosiNegiResult(){
		float posi = 0;
		float negi = 0;
		
		Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		float achieveDaysTotal = 0;
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=startday;i<size;i++){	
				DayLine today = dList.get(i);
				if(today.probabilityChoosen && today.futureIncreaseCalculated){
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
	
	public StatisticResult generateSpecProbability(Judger judger, int step){
		this.enhanceChoosen(judger, true);
		//this.enhanceFutureIncrease(step);
		StatisticResult result = this.getProbabilityPosiNegiResult();
		if(result.successRate>=0 && result.total>0){
			//System.out.println(result);
			System.out.print("+");
			return result;
		}
		return new StatisticResult(0,0,step);
	}
	
	public StatisticResult generateEvidence(boolean withRefresh, boolean fillInTable, Judger judger, int step, boolean outputLastDayListToFile){
		System.out.println();
		if(withRefresh){
			refreshDayLineDictionary();
			enhanceDaylines();
		}
		
		this.enhanceChoosen(judger, false);
		/*if(fillInTable){
			this.fillInStatisticTable();
		}*/
		this.enhanceFutureIncrease(step);
		if(outputLastDayListToFile){
			this.outputLastDayListToFile();
		}
		StatisticResult result = this.getPosiNegiResult();
		
		if(result.successRate>=0 && result.total>0){
			System.out.println(result);
			return result;
		}
		return new StatisticResult(0,0,step);
		
	}
	
	
	
	
	
	private void enhanceDaylines(){
		Collection<ArrayList<DayLine>> AllDayLines = this.stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=startday;i<size;i++){	
				DayLine today = dList.get(i);
				DayLine todayM1 = dList.get(i-1);
				DayLine todayM2 = dList.get(i-2);
				DayLine todayM3 = dList.get(i-3);
				
				today.todayIncrease= Math.round((today.close-todayM1.close)*1000/todayM1.close)/10.0f;
				today.slope3 = Math.round((today.close-todayM3.close)*1000/todayM2.close)/3;
				today.volpercent = 0;
				
				float shorttermvol = 0;
				float longtermvol = 0;
				for(int j=0;j<20;j++){
					float realvol = dList.get(i-j).vol;
					if(j<3){
						shorttermvol =  shorttermvol+realvol;
					}
					longtermvol  = longtermvol+realvol;
				}
				shorttermvol/=3;
				longtermvol/=20;
				today.volpercent = Math.round((shorttermvol-longtermvol)*100/(shorttermvol+longtermvol));
				
				
				ArrayList<DayLine> varianceList = new ArrayList<DayLine>();
				varianceList.add(today);
				varianceList.add(todayM1);
				varianceList.add(todayM2);
				
				today.variance = this.variance(varianceList);
				if(((today.high-today.close)+(today.open-today.low))==0){
					today.shadowprotion = 100;
				}else{
					today.shadowprotion = (today.high-today.close)*100/(today.high-today.low);
				}
				
				
				
				
				
			}
		}	
	}
	
	private double variance(ArrayList<DayLine> list){
		int size = list.size();
		float average = 0;
		for(DayLine dl: list){
			average = average +dl.todayIncrease;
		}
		average = average/size;
		float total =0;
		for(DayLine dl: list){
			total = total+ (dl.todayIncrease-average)*(dl.todayIncrease-average);
		}
		total  = total /size;
		return Math.sqrt(total);
	}
	
	public void generateEvidence(){
		float max = 100000;
		float min = -100000;
		Scanner scan = new Scanner(System.in);
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
		generateEvidence(true, true, judger, 5, true);
		scan.nextLine();
	}
	public static void main(String[] args){
		 DayLineRegister dlr = DayLineRegister.getInstance();
		 dlr.generateEvidence();
		 // dlr.test();
	}
	
	
	
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
						dlr.generateEvidence(true, true, judger, 5, true);
					}
				}	
			}
		 }
		 
		
	}
}
