package stockdata.enhancer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import stockdata.DayLine;
import stockdata.DayLineRegister;
import stockdata.Judger;
import stockdata.JudgerUnit;
import stockdata.StatisticResult;

public class ProbabilityEnhancer {

	private StatisticResult getProbabilityPosiNegiResult(){
		float posi = 0;
		float negi = 0;
		
		Collection<ArrayList<DayLine>> AllDayLines = DayLineRegister.getInstance().stockDictionary.values();
		float achieveDaysTotal = 0;
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			for(int i=DayLineRegister.startday;i<size;i++){	
				DayLine today = dList.get(i);
				if(today.isChoosen&&today.probabilityChoosen && today.futureIncreaseCalculated){
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
	
	private StatisticResult generateSpecProbability(Judger judger, int step){
		
		DayLineRegister.choosenEnhancer.enhanceChoosen(judger, true);
		//this.enhanceFutureIncrease(step);
		StatisticResult result = this.getProbabilityPosiNegiResult();
		if(result.successRate>=0 && result.total>0){
			//System.out.println(result);
			
			return result;
		}
		return new StatisticResult(0,0,step);
	}
	
	private StatisticResult getDayLineProbility(DayLine today, 
												float slopeRange, 
												float volpercentRange, 
												float varianceRange, 
												float protionRange, 
												float oiRange,
												float strengthRange,
												float overallStrengthRange) {
		JudgerUnit slope3Unit = new JudgerUnit(today.slope3-slopeRange, today.slope3+slopeRange);
		JudgerUnit percentUnit = new JudgerUnit(today.volpercent-volpercentRange, today.volpercent+volpercentRange);
		JudgerUnit varianceUnit = new JudgerUnit((float)today.variance-varianceRange, (float)today.variance+varianceRange);
		JudgerUnit shadowUnit = new JudgerUnit(today.shadowprotion-protionRange, today.shadowprotion+protionRange);
		//JudgerUnit overallIncreaseUnit = new JudgerUnit(today.overallIncrease-oiRange, today.overallIncrease+oiRange);
		JudgerUnit strengthUnit = new JudgerUnit(today.strength-strengthRange, today.strength+strengthRange);
		JudgerUnit overallStrengthUnit = new JudgerUnit(today.overallStrength-overallStrengthRange, today.overallStrength+overallStrengthRange);
		
		HashMap<String, JudgerUnit> rulerList = new HashMap<String, JudgerUnit>();
		rulerList.put("slope3",slope3Unit);
		rulerList.put("volpercent", percentUnit);
		rulerList.put("variance",varianceUnit);
		rulerList.put("shadowprotion",shadowUnit);
		rulerList.put("strength",strengthUnit);
		rulerList.put("overallStrength",overallStrengthUnit);
		rulerList.put("jump", new JudgerUnit(0.0001f, 100));
		
		Judger judger = new Judger(rulerList);
		StatisticResult result = this.generateSpecProbability(judger, 5);
		return result;
		
	}
	public void enhanceDayLineProbability(DayLine today){
		StatisticResult innerresult = getDayLineProbility(today,5.6f,5.6f,2.2f,5.6f, 0.3f, 1.0f, 0.56f);
		StatisticResult outerresult = getDayLineProbility(today,6.4f,6.4f,2.5f,6.4f, 0.4f, 1.15f, 0.64f);
		
		float total = outerresult.total;
		float probability = 0;
		float achieveDays = 0;
		if(innerresult.total+outerresult.total>0){
			probability = (innerresult.successRate*innerresult.total+outerresult.successRate*outerresult.total)/(innerresult.total+outerresult.total);
			achieveDays = (innerresult.successDays*innerresult.total+outerresult.successDays*outerresult.total)/(innerresult.total+outerresult.total);
		}
		StatisticResult probresult = new StatisticResult(total, probability, achieveDays);
		
		today.total = probresult.total;
		today.probability = probresult.successRate;
		today.innerProb = innerresult.successRate;
		today.outerProb = outerresult.successRate;
		today.achieveDays = probresult.successDays;
		today.coreRate = innerresult.total/(innerresult.total+outerresult.total);
		today.innerNum = innerresult.total;
		today.outerNum = outerresult.total;
		System.out.print("+");
	}
	public void enhanceLastDayProbability(){
		Collection<ArrayList<DayLine>> AllDayLines = DayLineRegister.getInstance().stockDictionary.values();
		for(ArrayList<DayLine> dList: AllDayLines){
			int size = dList.size();
			if(size<=1) continue;
			DayLine today = dList.get(size-1);
			if(today.isChoosen){
				enhanceDayLineProbability(today);
			}
		}	
	}

}
