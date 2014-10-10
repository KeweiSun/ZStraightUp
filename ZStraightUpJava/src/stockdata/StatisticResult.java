package stockdata;

public class StatisticResult {
	public float total;
	public float successRate;
	public float successDays;
	public StatisticResult(float total, float successRate, float successDays) {
		this.total = total;
		this.successRate = successRate;
		this.successDays = successDays;
	}
	public String toString(){
		return "Total: "+total+"  Rate: "+successRate+" Days: "+successDays;
	}

}
