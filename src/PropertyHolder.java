import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("unused")
public class PropertyHolder {
	public static final String inheritableNull = "InheritableNullValue!!!!!!!";
	HashMap<String,HashMap<String,String>> blockstatePropertiesMap;
	private HashMap<String,HashSet<PropertyHolder>> parents;
	public String state = "";
	public boolean isGroup = false;
	public String name;


	public void ensureState(String blockstate){
		if(blockstatePropertiesMap==null)
			blockstatePropertiesMap=new HashMap<>();
		if(!blockstatePropertiesMap.containsKey(blockstate)){
			blockstatePropertiesMap.put(blockstate,new HashMap<>());
		}
	}

	public void put(String propertyName, String value, String blockstate){
		ensureState(blockstate);
		blockstatePropertiesMap.get(blockstate).put(propertyName,value);
	}

	public String rawGet(String property, String blockstate){
		if(blockstatePropertiesMap==null)
			return null;
		HashMap<String,String> stateProperties = blockstatePropertiesMap.get(blockstate);
		if(stateProperties==null)
			return null;

		return stateProperties.get(property);
	}

	public String get(String property, String blockstate){
		String prop = rawGet(property, blockstate);

		if(prop==null && !blockstate.isEmpty()) {
			prop = rawGet(property, "");
		}

		if(prop==null && parents != null) {
			for (String ancestor : new String[]{"", blockstate}) {
				HashSet<PropertyHolder> parentSet = parents.get(ancestor);
				if (parentSet != null)
					for (PropertyHolder parent : parentSet) {
						prop = parent.get(property, blockstate);
						if (prop != null)
							break;
					}
			}
		}

		if(prop==inheritableNull)
			return null;

		return prop;
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

	public void addParent(PropertyHolder p, String state){
		if(parents==null){
			parents = new HashMap<>();
			parents.put("",new HashSet<>());
		}

		HashSet<PropertyHolder> stateParents = parents.get(state);
		//noinspection Java8MapApi
		if(stateParents==null){
			stateParents = new HashSet<>(2);
			parents.put(state,stateParents);
		}
		stateParents.add(p);
	}

	private void addAllStatesToList(ArrayList<String> ret){
		if(blockstatePropertiesMap!=null){
			for(String state: blockstatePropertiesMap.keySet()){
				if(!state.isEmpty()){
					ret.add(state);
				}
				if(parents==null)
					continue;

				HashSet<PropertyHolder> stateParents = parents.get(state);
				if(stateParents!=null)
					for(PropertyHolder parent: stateParents)
						parent.addAllStatesToList(ret);
			}
		}
	}

	public ArrayList<PropertyHolder> getAllStates(){
		ArrayList<String> stateNames = new ArrayList<>();
		addAllStatesToList(stateNames);

		ArrayList<PropertyHolder> ret = new ArrayList<>();
		for(String state: stateNames){
			PropertyHolder stateHolder = new PropertyHolder();
			stateHolder.name = "view of "+name+" from "+state;
			stateHolder.addParent(this,"");
			stateHolder.state=state;
			ret.add(stateHolder);
		}

		if(ret.isEmpty())
			ret.add(this);
		return ret;
	}
}
