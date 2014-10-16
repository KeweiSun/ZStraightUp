package bqdata;


import java.util.Comparator;
public class BQComparator implements Comparator<BQInfo>{

	
	@Override
	public int compare(BQInfo bq1, BQInfo bq2) {
		Float stock1Score = bq1.stockTotalIncrease/bq1.stockCount;
		Float stock2Score = bq2.stockTotalIncrease/bq2.stockCount;
		return stock2Score.compareTo(stock1Score);
	}

}

