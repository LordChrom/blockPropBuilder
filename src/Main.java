import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

public class Main {
	//layout:
	// number%1000 is color, number/1000 is metadata
	// leaves 6.03 bits for metadata, also allows 0xffff to not need to be an encoding state, also lets me not change how my colors are set up.
	// metadata bits are currently:
	// 1 bit unused
	// 3 bits light type
	// 2 bits opacity type (0 is air, 1 is translucent, 2 is full opacity, 3 is shaped opacity)
	static int assignNumber(PropertyHolder p){
		if(p.has("hardcodedID"))
			return p.getInt("hardcodedID");
		boolean translucent = p.has("translucent");
		boolean transparent = p.has("transparent");
		boolean subsurface = p.has("subsurface");
		boolean shaped = p.has("shaped") ||  p.has("shapedNonVanilla");;
		boolean emissive = p.has("emissive");

		int lightType = emissive?2:0;

		if(p.has("lightType"))
			lightType = p.getInt("lightType");

		int shapeType;

		if(translucent)
			shapeType=1;
		else if(shaped)
			shapeType=3;
		else if(transparent)
			shapeType=0;
		else
			shapeType=2;


		int m = ((lightType&7)<<2) | (shapeType&3);
		String color = p.getOrDefault("color","0");
		return m*1000 + (subsurface?15:Integer.parseInt(color));
	}

	static HashMap<String, PropertyHolder> blockMap = new HashMap<>();
	static HashMap<Integer,HashSet<String>> assignedIDs = new HashMap<>();


	enum Operation {PropertySetting,GroupForming};
	static Operation currentOperation;
	static PropertyHolder currentGroup;
	static String propertyName="";
	static String propertyValue="";

//	private static final String groupPrefix = "INTERNAL_GROUP_PREFIX:";
	static List<String> recognizedPrefixes = List.of(new String[]{"minecraft:"});

	static void ensureExistence(String block){
		if(blockMap.containsKey(block)) return;
		PropertyHolder newHolder = new PropertyHolder();
		newHolder.name="block "+block;
		blockMap.put(block,newHolder);
	}

	static String ensurePrefixed(String block){
		if(blockMap.containsKey(block))
			return block;
		boolean validPrefix = false;
		char firstChar = block.charAt(0);
		boolean firstCharIsAlpha = Character.isAlphabetic(firstChar);
		if(!firstCharIsAlpha)
			block=block.substring(1);

		for(String prefix: recognizedPrefixes)
			validPrefix|=block.startsWith(prefix); //TODO make this not awful


		if(firstCharIsAlpha || (firstChar=='%')) {
			block = recognizedPrefixes.getFirst() + block;
		}

		if(!firstCharIsAlpha)
			block=firstChar+block;

		return block;
	}

	static void applyProperty(String block, String state){
		PropertyHolder properties = blockMap.get(block);
		for(String property: propertyName.split("[&]")) {
			property=property.strip();
			properties.put(property, propertyValue, state);

			String print;
			if(propertyValue==null)
				print = "Removed "+property+" from ";
			else
				print = "Applied "+property+(propertyValue.isEmpty()?"":"=")+propertyValue+" to ";

			print+="block: "+block;
			if(!state.isEmpty())
				print+=", blockstate: "+state;
			print+=".";
			System.out.println(print);
		}
	}

	static void applyGroup(String block, String state){
		PropertyHolder blockHolder = blockMap.get(block);
		blockHolder.addParent(currentGroup,state);
		blockHolder.ensureState(state);
	}

	static void applyOperation(String block){
		if(propertyName.isEmpty() || block.isEmpty())
			return;
		block = ensurePrefixed(block);
		String state = "";
		int blockstateChar = block.indexOf(':',block.indexOf(':')+1);
		if(blockstateChar>0){
			state=block.substring(blockstateChar);
			block=block.substring(0,blockstateChar);
		}

		ensureExistence(block);
		switch (currentOperation) {
			case PropertySetting -> applyProperty(block,state);
			case GroupForming -> applyGroup(block,state);
		}
	}

	static void processInput(Scanner s){
		while(s.hasNextLine()){
			String inputLine = s.nextLine().strip();
			for(String line: inputLine.split(";")){
				line = line.strip();
				if(line.isEmpty()) continue;

				String lineArgument = line.substring(1).strip();

				switch(line.charAt(0)){
					case '+' -> {
						currentOperation= Operation.PropertySetting;
						propertyName = lineArgument;
						propertyValue="";
					}
					case '-' -> {
						propertyName = lineArgument;
						propertyValue=null;
					}
					case '=' -> {
						propertyValue = lineArgument;
					}
					case '@' -> {
						currentOperation= Operation.GroupForming;
						propertyName=lineArgument;
						ensureExistence(lineArgument);
						currentGroup = blockMap.get(lineArgument);
						currentGroup.isGroup=true;
						currentGroup.name="Group "+lineArgument;
					}
					case '#' -> {}

					default -> {
						for(String block:line.split(" "))
							applyOperation(block);
					}
				}
			}
		}
	}

	static void assignAllIDs(){
		for(String block:blockMap.keySet()){
			PropertyHolder blockHolder = blockMap.get(block);
			if(blockHolder.isGroup)
				continue;
			for(PropertyHolder stateHolder: blockHolder.getAllStates()) {
				int id = assignNumber(stateHolder);
				if (!assignedIDs.containsKey(id))
					assignedIDs.put(id, new HashSet<>());
				assignedIDs.get(id).add(block+stateHolder.state);
			}
		}
	}

	static String serializeAssignments(){
		StringBuilder ret = new StringBuilder(
			"# File generated by script at https://github.com/LordChrom/blockPropBuilder\n" +
			"# Changes made here will be discarded"
		);

		for(int id: assignedIDs.keySet().stream().sorted().toList()){
			ret.append("\n");
			ret.append("block.").append(id).append(" =");
			for(String block: assignedIDs.get(id).stream().sorted().toList()){
				ret.append(" ").append(block);
			}
		}
		return ret.toString();
	}

	static void copyToClipboard(String text){
		StringSelection selection = new StringSelection(text);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection,selection);

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
		String blockPropData = serializeAssignments();
		copyToClipboard(blockPropData);
		System.out.println(blockPropData);
	}
}