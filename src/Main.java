import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
	static HashMap<String, HashMap<String,String>> blockMap = new HashMap<>();
	static HashMap<Integer,HashSet<String>> assignedIDs = new HashMap<>();


	static String propertyName="";
	static String propertyValue="";

	static List<String> recognizedPrefixes = List.of(new String[]{"minecraft:"});

	static void ensureExistence(String block){
		if(blockMap.containsKey(block)) return;
		blockMap.put(block,new HashMap<>());
	}

	static void applyProperty(String block){
		if(propertyName.isEmpty() || block.isEmpty())
			return;

		boolean validPrefix = false;
		boolean blockTag = block.charAt(0)=='%';
		if(blockTag)
			block=block.substring(1);

		for(String prefix: recognizedPrefixes)
			validPrefix|=block.contains(prefix);


		if(!validPrefix)
			block=recognizedPrefixes.get(0)+block;

		if(blockTag)
			block="%"+block;

		ensureExistence(block);
		HashMap<String,String> properties = blockMap.get(block);
		for(String property: propertyName.split("[&]")) {
			property=property.strip();
			properties.put(property, propertyValue);
			System.out.println("Applied "+property+(propertyValue.isEmpty()?"":"=")+propertyValue+" to: "+block);
		}
	}

	static void processInput(Scanner s){
		while(s.hasNextLine()){
			String inputLine = s.nextLine().strip();
			for(String line: inputLine.split(";")){
	//			System.out.println(line);
				if(line.isEmpty()) continue;

				switch(line.charAt(0)){
					case '+' -> {
						propertyName = line.substring(1);
						propertyValue="";
					}
					case '=' -> {
						propertyValue = line.substring(1);
					}
					case '#' -> {}
					default -> {
						for(String block:line.split(" ")){
							applyProperty(block);
						}
					}
				}
			}
		}
	}

	static int assignNumber(HashMap<String,String> properties){
		if(properties.containsKey("HardcodedID"))
			return Integer.parseInt(properties.get("HardcodedID"));
		boolean Translucent = properties.containsKey("Translucent");
		boolean Transparent = properties.containsKey("Transparent");
		boolean Subsurface = properties.containsKey("Subsurface");
		boolean Emissive = properties.containsKey("Emissive");

		int m = (Emissive?4:0)+(Translucent?2:0)+((!(Translucent||Transparent))?1:0);
		String color = properties.getOrDefault("Color","0");
		return m*1000+(Subsurface?15:Integer.parseInt(color));
	}

	static void assignAllIDs(){
		for(String block:blockMap.keySet()){
			int id = assignNumber(blockMap.get(block));
			if(!assignedIDs.containsKey(id))
				assignedIDs.put(id,new HashSet<>());
			assignedIDs.get(id).add(block);
		}
	}

	static void printAssignments(){
		for(int id: assignedIDs.keySet().stream().sorted().toList()){
			StringBuilder entry = new StringBuilder("block." + id + " =");
			for(String block: assignedIDs.get(id)){
				entry.append(" ").append(block);
			}
			System.out.println(entry);
		}
	}

	public static void main(String[] args) {
		Scanner s;
		try {
			s = new Scanner(new File("BlockInfo.txt"));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		processInput(s);
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		assignAllIDs();
		printAssignments();
	}
}