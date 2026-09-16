import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;


@SuppressWarnings("unused")
public class PropertyHolder {
	record PropertyAssignment(String value, long precedence) {
		public static PropertyAssignment best(PropertyAssignment a, PropertyAssignment b){
			if(b==null) return a;
			if(a==null) return b;
			return a.precedence>=b.precedence?a:b;
		}
	}

	private static final PropertyAssignment noAssignment = new PropertyAssignment(null,Long.MIN_VALUE);

	HashMap<Blockstate,HashMap<String,PropertyAssignment>> blockstatePropertiesMap;
	private HashMap<Blockstate,HashSet<PropertyHolder>> parents;
	public Blockstate state = Blockstate.of("");
	public boolean isGroup = false;
	public String name;


	public void ensureState(Blockstate state){
		if(blockstatePropertiesMap==null)
			blockstatePropertiesMap=new HashMap<>();
		if(!blockstatePropertiesMap.containsKey(state)){
			blockstatePropertiesMap.put(state,new HashMap<>());
		}
	}

	public void put(String propertyName, String value, Blockstate state, long precedence){
		ensureState(state);
		blockstatePropertiesMap.get(state).put(propertyName,new PropertyAssignment(value,precedence));
	}

	public PropertyAssignment rawGet(String property, Blockstate blockstate){
		PropertyAssignment best = noAssignment;

		if(blockstatePropertiesMap!=null)
			for(HashMap<String,PropertyAssignment> map: getPropertyAssignments(blockstate))
				best= PropertyAssignment.best(best, map.get(property));

		return best;
	}

	public PropertyAssignment getAssignment(String property, Blockstate blockstate){
		PropertyAssignment bestAssignment = rawGet(property, blockstate);

		for (PropertyHolder parent : getMatchingParents(blockstate)) {
			bestAssignment = PropertyAssignment.best(
					bestAssignment,
					parent.getAssignment(property, blockstate)
			);
		}

		return bestAssignment;
	}

	public String get(String property, Blockstate blockstate){
		return getAssignment(property, blockstate).value;
	}

	public String get(String property){
		return get(property,state);
	}


	public boolean has(String property){
		return get(property)!=null;
	}

	public String getOrDefault(String property, String fallback){
		String prop = get(property);
		if(prop==null)
			return fallback;
		return prop;
	}

	public int getIntOrDefault(String property, int fallback){
		String prop = get(property);
		if(prop==null)
			return fallback;
		prop=prop.replaceAll("[_ ,]","");
		try {
			int base = 10;
			if(prop.length()>=2)
				switch(prop.substring(0,2)){
					case "0b" -> base=2;
					case "0o" -> base=8;
					case "0x" -> base=16;
				}
			if(base!=10)
				prop=prop.substring(2);
			return Integer.parseInt(prop,base);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	public int getInt(String property){
		return getIntOrDefault(property,Integer.MIN_VALUE);
	}

	public void addParent(PropertyHolder p, Blockstate state){
		if(parents==null){
			parents = new HashMap<>();
			parents.put(Blockstate.EmptyState,new HashSet<>());
		}

		HashSet<PropertyHolder> stateParents = parents.get(state);
		//noinspection Java8MapApi
		if(stateParents==null){
			stateParents = new HashSet<>(2);
			parents.put(state,stateParents);
		}
		stateParents.add(p);
	}

	public ArrayList<PropertyHolder> getMatchingParents(Blockstate state){
		HashSet<PropertyHolder> ret = new HashSet<>();
		if(parents!=null){
			for(Map.Entry<Blockstate,HashSet<PropertyHolder>> parent : parents.entrySet()){
				if(state.matches(parent.getKey()))
					ret.addAll(parent.getValue());
			}
		}
		return new ArrayList<>(ret);
	}

	public ArrayList<HashMap<String,PropertyAssignment>> getPropertyAssignments(Blockstate state){
		ArrayList<HashMap<String,PropertyAssignment>> ret = new ArrayList<>();
		if(blockstatePropertiesMap==null)
			return ret;
		for(Map.Entry<Blockstate,HashMap<String,PropertyAssignment>> parent : blockstatePropertiesMap.entrySet()){
			if(state.matches(parent.getKey()))
				ret.add(parent.getValue());
		}
		return ret;
	}

	private void addAllStatesToList(HashMap<String,HashSet<String>> ret){
		if(blockstatePropertiesMap!=null){
			for(Blockstate state: blockstatePropertiesMap.keySet()){
				for(Entry<String,String> entry : state.getEntries()){
					if(!ret.containsKey(entry.getKey()))
						ret.put(entry.getKey(),new HashSet<>());

					ret.get(entry.getKey()).add(entry.getValue());
				}

				if(parents==null) continue;

				for(PropertyHolder parent: getMatchingParents(state))
					parent.addAllStatesToList(ret);
			}
		}
	}

	public ArrayList<PropertyHolder> getAllStates(){
		HashMap<String,HashSet<String>> stateFieldValues = new HashMap<>();
		addAllStatesToList(stateFieldValues);

		ArrayList<String> stateValueCombinations = new ArrayList<>();
		stateValueCombinations.add("");

		for(Entry<String,HashSet<String>> blockstateField: stateFieldValues.entrySet()){
			ArrayList<String> swap = new ArrayList<>();
			for(String fieldValue : blockstateField.getValue()){
				for(String stateCombo : stateValueCombinations){
					swap.add(stateCombo+":"+blockstateField.getKey()+"="+fieldValue);
				}
			}
			stateValueCombinations=swap;
		}


		ArrayList<PropertyHolder> ret = new ArrayList<>();
		for(String stateName: stateValueCombinations){
			PropertyHolder stateHolder = new PropertyHolder();
			stateHolder.name = "view of "+name+" from "+stateName;
			stateHolder.addParent(this,Blockstate.EmptyState);
			stateHolder.state=Blockstate.of(stateName);;
			ret.add(stateHolder);
		}

		if(ret.isEmpty())
			ret.add(this);
		return ret;
	}
}
