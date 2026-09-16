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
		boolean transparent = p.has("transparent");
		boolean subsurface = p.has("subsurface");

		int shapeData = (p.getIntOrDefault("shapeCategory",0)<<4) + p.getIntOrDefault("shapeSubcategory",0);

		if(shapeData==0 && !transparent)
			shapeData=1;

		int color = p.getIntOrDefault("color",0);
		if(color==0 && subsurface)
			color=47;

		return (shapeData<<6) | color;
	}
}
