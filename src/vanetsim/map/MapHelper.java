package vanetsim.map;

/**
 * This class holds some geometric functions needed for various calculations on the map. It's just a helper
 * class to make the map class smaller and as this class has no variables, all functions are declared static.
 */
public class MapHelper {

    /**
     * Checks for intersections on the maps and creates bridges if necessary (only for display purposes). The bridge is added to
     * the <code>lowerspeedStreet</code>.
     * This function takes quite a long time to process if the map is large.
     *
     * @param bridgeStreet 	the street which will be above <code>otherStreet</code> if both intersect
     * @param otherStreet 	the other street
     */
    public static void calculateBridges(Street bridgeStreet, Street otherStreet){
        /** 待新增 */
    }


    /**
     * Recalculates start and end points of a line so that the line is shorter or longer than before.
     *
     * @param startPoint	the start point. x coordinate is expected in <code>startPoint[0]</code>, y in <code>startPoint[1]</code>.
     * 						Will be used to return the result.
     * @param endPoint		the end point. x coordinate is expected in <code>endPoint[0]</code>, y in <code>endPoint[1]</code>.
     * 						Will be used to return the result.
     * @param correction	the amount of length correction. Use a positive value to makes the line shorter, a negative to make it longer. If
     * 						<code>correctStart</code> and <code>correctEnd</code> are both <code>true</code>, the total correction is double
     * 						of this value, if both are <code>false</code> no correction is done.
     * @param correctStart	if the <code>startPoint</code> shall be corrected.
     * @param correctEnd	if the <code>endPoint</code> shall be corrected.
     */
    public static void calculateResizedLine(int[] startPoint, int[] endPoint, double correction, boolean correctStart, boolean correctEnd){
        if(startPoint.length == 2 && endPoint.length == 2){
            if(startPoint[0] == endPoint[0]){	// horizontal line
                if(startPoint[1] > endPoint[1]){
                    startPoint[1] -= correction;
                    endPoint[1] += + correction;
                } else {
                    startPoint[1] += correction;
                    endPoint[1] -= correction;
                }
            } else if (startPoint[1] == endPoint[1]){	// vertical line
                if(startPoint[0] > endPoint[0]){
                    startPoint[0] -= correction;
                    endPoint[0] += correction;
                } else {
                    startPoint[0] += correction;
                    endPoint[0] -= correction;
                }
            } else {	// diagonal line. this will be called most of the time!
                // calculate line parameters: y = ax + b
                double a = ((double)startPoint[1] - endPoint[1]) / ((double)startPoint[0] - endPoint[0]);
                double b = startPoint[1] - a * startPoint[0];
                // Pythagorean theorem (d = delta): linelength^2 = dx^2 - dy^2
                // with line parameters:            linelength^2 = dx^2 + a^2*dx^2
                // after some mathematics this leads to:      dx = sqrt(linelength^2/(1+a^2)
                if(startPoint[0] > endPoint[0]) {
                    if(correctStart) startPoint[0] -= (int)Math.round(Math.sqrt(correction*correction/(1 + a*a)));
                    if(correctEnd) endPoint[0] += (int)Math.round(Math.sqrt(correction*correction/(1 + a*a)));
                } else {
                    if(correctStart) startPoint[0] += (int)Math.round(Math.sqrt(correction*correction/(1 + a*a)));
                    if(correctEnd) endPoint[0] -= (int)Math.round(Math.sqrt(correction*correction/(1 + a*a)));
                }
                if(correctStart) startPoint[1] = (int)Math.round(a*startPoint[0] + b);
                if(correctEnd) endPoint[1] = (int)Math.round(a*endPoint[0] + b);
            }
        }
    }


}
