package stockdata.enhancer;

import java.util.ArrayList;
import java.util.Collection;

import stockdata.DayLine;
import stockdata.DayLineRegister;
import stockdata.DayProfile;
import stockdata.Judger;

public class ChoosenEnhancer {
	public void enhanceChoosen(Judger judger, boolean isProbabilityChoosen){
			
		Collection<ArrayList<DayLine>> AllDayLines = DayLineRegister.getInstance().stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=DayLineRegister.startday;i<size;i++){	
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
	
}
