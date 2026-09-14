import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Blockstate {
	private static final HashMap<String,Blockstate> allBlockstates  = new HashMap<>();
	public static final Blockstate EmptyState = new Blockstate("");

	private final HashMap<String,String> tagValues;

	//if this state has every value set by the pattern
	public boolean matches(Blockstate pattern){
		for(Map.Entry<String,String> patternEntry: pattern.tagValues.entrySet()){
			String personalValue = tagValues.get(patternEntry.getKey());
			if(!patternEntry.getValue().equals(personalValue))
				return false;
		}
		return true;
	}

	public String toString(){
		StringBuilder ret = new StringBuilder();
		for(Map.Entry<String,String> entry: tagValues.entrySet()){
			ret.append(':').append(entry.getKey()).append('=').append(entry.getValue());
		}
		if(!ret.isEmpty())
			ret.deleteCharAt(0);
		return ret.toString();
	}

	private void register(){
		String stringVal = toString();
		if(!allBlockstates.containsKey(stringVal))
			allBlockstates.put(stringVal,this);
	}

	private Blockstate(){
		this("");
	}

	private Blockstate(String stateString){
		String[] elements = stateString.split(":");
		tagValues = new HashMap<>(elements.length+(elements.length/2));
		for(String element: elements){
			String[] assignmentValues = element.split("=");
			if(assignmentValues.length==2){
				tagValues.put(assignmentValues[0],assignmentValues[1]);
			}
		}
	}

	public Blockstate deduplicate(){
		String stringVal = toString();
		return allBlockstates.get(stringVal);
	}

	public static Blockstate of(String s){
		if(s==null)
			return null;
		if(s.isEmpty())
			return EmptyState;
		Blockstate newState = new Blockstate(s);
		String strVal = newState.toString();

		Blockstate recordedState = allBlockstates.get(strVal);
		if(recordedState!=null)
			return recordedState;
		newState.register();
		return newState;
	}

	public Set<Entry<String,String>> getEntries(){
		return tagValues.entrySet();
	}

	public boolean isEmpty(){
		return this== EmptyState;
	}
}
