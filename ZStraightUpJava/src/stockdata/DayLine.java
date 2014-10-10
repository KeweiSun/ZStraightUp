package stockdata;

import java.math.BigDecimal;

public class DayLine {
	public String date;
	public int dateIndex;
	public float open;
	public float high;
	public float low;
	public float close;
	public float amount;
	public float vol;
	public String stockid;
	public String stockname;
	
	public float slope3;
	public float volpercent;
	public float todayIncrease;
	public double variance;
	public float shadowprotion;
	
	public boolean isChoosen=false;
	public boolean probabilityChoosen = false;
	public boolean futureIncreaseCalculated = false;
	
	public float futureIncrease;
	public float futureSecondIncrease;
	public float probability;
	public float achieveDays;
	
	public float total;
	private float setPrecise(float num){
		BigDecimal bg = new BigDecimal(num);
        float f1 = bg.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue(); 
		return f1; 
	}
	public String toString(){
		
		return date+"  "+stockid+"  "+stockname+"      \t"+total+"\t"+setPrecise(probability)+"\t"+setPrecise(achieveDays)+"\tslope:"+setPrecise(slope3)+"\tvolpent:"+setPrecise(volpercent)+"\tvariance:"+setPrecise((float)variance)+"\tshadow:"+setPrecise(shadowprotion);
	}
	
}
