import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("unused")
public class PropertyHolder {
	public static final String inheritableNull = "InheritableNullValue!!!!!!!";
	HashMap<Blockstate,HashMap<String,String>> blockstatePropertiesMap;
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

	public void put(String propertyName, String value, Blockstate state){
		ensureState(state);
		blockstatePropertiesMap.get(state).put(propertyName,value);
	}

	public String rawGet(String property, Blockstate blockstate){
		if(blockstatePropertiesMap==null)
			return null;
		HashMap<String,String> stateProperties = blockstatePropertiesMap.get(blockstate);
		if(stateProperties==null)
			return null;

		return stateProperties.get(property);
	}

	public String get(String property, Blockstate blockstate){
		String prop = rawGet(property, blockstate);

		if(prop==null && !blockstate.isEmpty()) {
			prop = rawGet(property, Blockstate.EmptyState);
		}

		if(prop==null && parents != null) {
			for (Blockstate ancestor : new Blockstate[]{Blockstate.EmptyState, blockstate}) {
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

	private void addAllStatesToList(ArrayList<Blockstate> ret){
		if(blockstatePropertiesMap!=null){
			for(Blockstate state: blockstatePropertiesMap.keySet()){
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
		ArrayList<Blockstate> states = new ArrayList<>();
		addAllStatesToList(states);

		ArrayList<PropertyHolder> ret = new ArrayList<>();
		for(Blockstate state: states){
			PropertyHolder stateHolder = new PropertyHolder();
			stateHolder.name = "view of "+name+" from "+state;
			stateHolder.addParent(this,Blockstate.EmptyState);
			stateHolder.state=state;
			ret.add(stateHolder);
		}

		if(ret.isEmpty())
			ret.add(this);
		return ret;
	}
}
