package stockdata;

import java.util.Hashtable;
import java.util.Set;

public class DayProfile {
	Hashtable<String, Float> profile;

	public DayProfile(DayLine dayline) {
		profile = new Hashtable<String, Float>();
		
		profile.put("slope3", dayline.slope3);
		profile.put("volpercent", dayline.volpercent);
		profile.put("variance", (float)dayline.variance);
		profile.put("shadowprotion", dayline.shadowprotion);
		//profile.put("overallIncrease", dayline.overallIncrease);
		profile.put("strength", dayline.strength);
		profile.put("overallStrength", dayline.overallStrength);
		profile.put("jump", dayline.jump);
	}
	public DayProfile(Hashtable<String, Float> profile) {
		this.profile = profile;
	}
	public Set<String> getProfileKeys(){
		return profile.keySet();
	}
	public float getNumberByKey(String key){
		return profile.get(key);
	}

}
