import java.util.ArrayList;
import java.util.HashMap;

public class PropertyHolder {
	HashMap<String,HashMap<String,String>> blockstatePropertiesMap;
	private ArrayList<PropertyHolder> parents;
	public String state = "";


	public void put(String propertyName, String value, String blockstate){
		if(blockstatePropertiesMap==null)
			blockstatePropertiesMap=new HashMap<>();

		HashMap<String,String> stateProperties = blockstatePropertiesMap.get(blockstate);
		if(stateProperties==null){
			stateProperties= new HashMap<>();
			blockstatePropertiesMap.put(blockstate,stateProperties);
		}

		stateProperties.put(propertyName,value);
	}


	public void put(String propertyName, String value){
		put(propertyName,value,state);
	}

	public String rawGet(String property, String blockstate){
		if(blockstatePropertiesMap==null)
			return null;
		HashMap<String,String> stateProperties = blockstatePropertiesMap.get(blockstate);
		if(stateProperties==null)
			return null;
		String prop = stateProperties.get(property);
		if(prop==null)
			return null;

		return prop;
	}

	public String get(String property, String blockstate){
		String prop = rawGet(property, blockstate);
		if(prop!=null) return prop;

		if(!blockstate.isEmpty()) {
			prop = rawGet(property, "");
			if (prop != null) return prop;
		}

		if(parents !=null){
			for(PropertyHolder parent: parents){
				prop = parent.get(property,blockstate);
				if(prop!=null) return prop;
			}
		}

		return null;
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
		try {
			return Integer.parseInt(prop);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	public int getInt(String property){
		return getIntOrDefault(property,Integer.MIN_VALUE);
	}

	public void addParent(PropertyHolder p){
		if(parents==null){
			parents = new ArrayList<>(2);
		}
		parents.add(p);
	}

	private void addAllStatesToList(ArrayList<String> ret){
		if(blockstatePropertiesMap!=null){
			for(String state: blockstatePropertiesMap.keySet()){
				if(!state.isEmpty()){
					ret.add(state);
				}
			}
		}

		if(parents!=null){
			for(PropertyHolder parent: parents)
				addAllStatesToList(ret);
		}
	}

	public ArrayList<PropertyHolder> getAllStates(){
		ArrayList<String> stateNames = new ArrayList<>();
		addAllStatesToList(stateNames);

		ArrayList<PropertyHolder> ret = new ArrayList<>();
		for(String state: stateNames){
			PropertyHolder stateHolder = new PropertyHolder();
			stateHolder.addParent(this);
			stateHolder.state=state;
			ret.add(stateHolder);
		}

		if(ret.isEmpty())
			ret.add(this);
		return ret;
	}
}
