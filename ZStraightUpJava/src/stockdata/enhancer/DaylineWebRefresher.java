package stockdata.enhancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import stockdata.Config;
import stockdata.DayLine;
import stockdata.DayLineRegister;

public class DaylineWebRefresher {


	private ArrayList<String> getUrlList(){
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> stockListArrayList = new ArrayList<String>();
		stockListArrayList.addAll(DayLineRegister.getInstance().stockDictionary.keySet());
		String[] stockListArray=new String[stockListArrayList.size()];
		stockListArrayList.toArray(stockListArray);
		
		String idListString = "";
		for(int i=0;i<stockListArray.length;i++){
			if(i>0&&(i%100==0||i==stockListArray.length-1)){
				String tempId = stockListArray[i];
				if(tempId.equalsIgnoreCase(Config.indexFilename)){
					tempId = "sh000001";
				}
				idListString = idListString+tempId;
				String url = "http://hq.sinajs.cn/list="+idListString;
				result.add(url);
				idListString = "";
			}else{
				String tempId = stockListArray[i];
				if(tempId.equalsIgnoreCase(Config.indexFilename)){
					tempId = "sh000001";
				}
				idListString = idListString+tempId+",";
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
     	 if(symbol.equalsIgnoreCase("sh000001")){
     	 	 System.out.println("in");
     		 symbol = Config.indexFilename;
     	 }
     	 dl.stockid = symbol;
     	 dl.stockname = data.split(",")[0];
         
     	 ArrayList<DayLine> dayLines = DayLineRegister.getInstance().stockDictionary.get(dl.stockid);
     	 int linesSize = dayLines.size();
     	 int dateIndex = dayLines.get(linesSize-1).dateIndex;
     	 if(dayLines.get(linesSize-1).date.equals(dl.date)){
     		 dl.dateIndex = dateIndex;
     		 dayLines.set(linesSize-1, dl);
     	 }else{
     		 dl.dateIndex = dateIndex+1;
     		 if(dl.dateIndex>DayLineRegister.lastDayIndex){
     			DayLineRegister.lastDayIndex = dl.dateIndex;
     			
     		 }
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

}
