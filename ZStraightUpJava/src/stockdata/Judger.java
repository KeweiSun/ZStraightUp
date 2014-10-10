package stockdata;

import java.util.HashMap;
import java.util.Set;

public class Judger {
	HashMap<String, JudgerUnit> judgerRuler = new HashMap<String, JudgerUnit>();
	public Judger(HashMap<String, JudgerUnit> judgerRuler){
		this.judgerRuler = judgerRuler;
	}
	
	public boolean accept(DayProfile profile){
		Set<String> keys = profile.getProfileKeys();
		for(String key:keys){
			JudgerUnit ruler = judgerRuler.get(key);
			float value = profile.getNumberByKey(key);
			if(value < ruler.lower){
				return false;
			}else if(value>ruler.upper){
				return false;
			}
		}
		return true;
	}

}
