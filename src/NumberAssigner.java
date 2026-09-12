public class NumberAssigner {
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
		boolean translucent = p.has("translucentColor");
		boolean transparent = p.has("transparent");
		boolean subsurface = p.has("subsurface");
		boolean shaped = p.has("shaped") ||  p.has("shapedNonVanilla");;
		boolean emissive = p.has("emissiveColor");

		int lightType = emissive?2:0;

		if(p.has("lightAnimation"))
			lightType = p.getInt("lightAnimation");

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
		int color = p.getIntOrDefault((translucent?"translucentColor":"emissiveColor"),0);
		return m*1000 + (subsurface?15:color);
	}
}
