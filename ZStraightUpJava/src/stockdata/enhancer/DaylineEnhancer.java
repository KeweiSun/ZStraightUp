package stockdata.enhancer;

import java.util.ArrayList;
import java.util.Collection;

import stockdata.DayLine;
import stockdata.DayLineRegister;

public class DaylineEnhancer {
	
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
	
	public void enhanceDaylines(){
		Collection<ArrayList<DayLine>> AllDayLines = DayLineRegister.getInstance().stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=DayLineRegister.startday;i<size;i++){	
				DayLine today = dList.get(i);
				DayLine todayM1 = dList.get(i-1);
				DayLine todayM2 = dList.get(i-2);
				DayLine todayM3 = dList.get(i-3);
				boolean continueFlag = false;
				if(today.close==0){
					continueFlag = true;
				}
				if(today.dateIndex-todayM3.dateIndex>5){
					continueFlag = true;
				}
				
				
				today.todayIncrease= Math.round((today.close-todayM1.close)*1000/todayM1.close)/10.0f;
				for(int j=i-3;j<=i;j++){
					if(dList.get(j).todayIncrease>11||dList.get(j).todayIncrease<-11){
						continueFlag = true;
					}
				}
				/*if(today.close==0||today.todayIncrease>11||today.todayIncrease<-11){
					if(today.close!=0){
						System.out.println(today+"  "+today.todayIncrease+"    "+today.dateIndex+"   "+todayM1.dateIndex);
						System.out.println();
					}
					continueFlag = true;
					
				}*/
				if(continueFlag){
					today.todayIncrease = 0;
					continue;
				}
				
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
	
	public void enhanceFutureIncrease(int step){
		Collection<ArrayList<DayLine>> AllDayLines = DayLineRegister.getInstance().stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=DayLineRegister.startday;i<size;i++){	
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

}
