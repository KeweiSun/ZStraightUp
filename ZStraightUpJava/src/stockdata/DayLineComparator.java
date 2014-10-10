package stockdata;

import java.util.Comparator;

public class DayLineComparator implements Comparator<DayLine>{
	@Override
	public int compare(DayLine line1, DayLine line2) {
		Float stock1Score = line1.probability;
		Float stock2Score = line2.probability;
		return stock2Score.compareTo(stock1Score);
	}
	

}

