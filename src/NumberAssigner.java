public class NumberAssigner {
	//layout:
	//2 bits unused
	//8 bits shape
	//6 bits color
	static int assignNumber(PropertyHolder p){
		if(p.has("hardcodedID"))
			return p.getInt("hardcodedID");
		boolean transparent = p.has("transparent");
		boolean subsurface = p.has("subsurface");
		boolean cutout = p.has("cutout");

		int shapeData = (p.getIntOrDefault("shapeCategory",0)<<4) + p.getIntOrDefault("shapeSubcategory",0);

		if(shapeData==0 && !transparent)
			shapeData=1;

		int color = p.getIntOrDefault("color",0);
		if(color==0 && subsurface)
			color=47;

		return (shapeData<<6) | color | (cutout?0x4000:0);
	}
}
