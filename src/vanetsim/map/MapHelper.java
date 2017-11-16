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
     * Gets the x and y coordinate difference of a parallel line on the right side (seen from first point to second point).
     *
     * @param x1 		the x coordinate of the first point
     * @param y1 		the y coordinate of the first point
     * @param x2 		the x coordinate of the second point
     * @param y2 		the y coordinate of the second point
     * @param distance	the distance
     * @param result	an array to return the coordinate differences of the parallel. The x coordinate difference will
     * 					be in <code>result[0]</code>, the y coordinate difference in <code>result[1]</code>.
     *
     * @return <code>true</code> if calculation was successful, else <code>false</code>
     */
    public static boolean getXYParallelRight(int x1, int y1, int x2, int y2, int distance, double[] result){
        if(result.length == 2){
            int dx = x2 - x1;
            int dy = y2 - y1;
            if(dx == 0){
                if(dy < 0){
                    result[0] = distance;
                    result[1] = 0;
                } else {
                    result[0] = -distance;
                    result[1] = 0;
                }
                return true;
            } else if (dy == 0){
                if(dx > 0){
                    result[0] = 0;
                    result[1] = distance;
                } else {
                    result[0] = 0;
                    result[1] = -distance;
                }
                return true;
            } else {
                //line parameter of this street (y = ax+b). b is unneeded here.
                double a = ((double)y1 - y2) / ((double)x1 - x2);

                //the line parameters for the normal
                double a2 = -1.0/a;
                double b2 = y1 - a2 * x1;

                //create a relatively long line with the normal's parameters
                double endX2 = x1 + 200000.0;
                double endY2 = a2 * endX2 + b2;

                //calculate the length of this created line
                double dx2 = endX2 - x1;
                double dy2 = endY2 - y1;
                double length2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

                //the difference we need can be calculated relative to the length of the created line
                double tmp = distance / length2;
                result[0] = dx2*tmp;
                result[1] = dy2*tmp;
                if(dy > 0){		//opposite direction
                    result[0] = -result[0];
                    result[1] = -result[1];
                }
                return true;
            }
        } else return false;
    }

    /**
     * Returns the nearest street to a given point. First all regions are calculated which are within <code>maxDistance</code>. Then, ALL
     * streets in these regions are checked if they are within this <code>maxDistance</code> and the best one is returned (if any exists).
     *
     * @param x 			the x coordinate of the given point
     * @param y 			the x coordinate of the given point
     * @param maxDistance	the maximum distance; use <code>Integer.MAX_VALUE</code> if you just want to get any nearest street but note
     * 						that this costs a lot of performance because ALL regions and ALL streets are checked!
     * @param distance 		an array used to return the distance between the nearest point and the point given. This should be a <code>double[1]</code> array!
     * @param nearestPoint 	an array used to return the x-coordinate (<code>nearestpoint[0]</code>) and y-coordinate (<code>nearestpoint[1]</code>)
     * 						on the street.
     *
     * @return the nearest street or <code>null</code> if none was found or an error occured
     */
    public static Street findNearestStreet(int x, int y, int maxDistance, double[] distance, int[] nearestPoint){
        Map map = Map.getInstance();
        Region[][] Regions = map.getRegions();
        if(Regions != null && nearestPoint.length >1 && distance.length > 0){
            int mapMinX, mapMinY, mapMaxX, mapMaxY, regionMinX, regionMinY, regionMaxX, regionMaxY;
            int[] tmpPoint = new int[2];
            Street[] streets;
            int i, j, k, size;
            Street bestStreet = null;
            double tmpDistance, bestDistance = Double.MAX_VALUE;
            long maxDistanceSquared = (long)maxDistance * maxDistance;

            // Minimum x coordinate to be considered
            long tmp = x - maxDistance;
            if (tmp < 0) mapMinX = 0;		// Map stores only positive coordinates
            else if(tmp < Integer.MAX_VALUE) mapMinX = (int) tmp;
            else mapMinX = Integer.MAX_VALUE;

            // Maximum x coordinate to be considered
            tmp = x + (long)maxDistance;
            if (tmp < 0) mapMaxX = 0;
            else if(tmp < Integer.MAX_VALUE) mapMaxX = (int) tmp;
            else mapMaxX = Integer.MAX_VALUE;

            // Minimum y coordinate to be considered
            tmp = y - maxDistance;
            if (tmp < 0) mapMinY = 0;
            else if(tmp < Integer.MAX_VALUE) mapMinY = (int) tmp;
            else mapMinY = Integer.MAX_VALUE;

            // Maximum y coordinate to be considered
            tmp = y + (long)maxDistance;
            if (tmp < 0) mapMaxY = 0;
            else if(tmp < Integer.MAX_VALUE) mapMaxY = (int) tmp;
            else mapMaxY = Integer.MAX_VALUE;

            // Get the regions to be considered
            Region tmpregion = map.getRegionOfPoint(mapMinX, mapMinY);
            regionMinX = tmpregion.getX();
            regionMinY = tmpregion.getY();

            tmpregion = map.getRegionOfPoint(mapMaxX, mapMaxY);
            regionMaxX = tmpregion.getX();
            regionMaxY = tmpregion.getY();

            // only iterate through those regions which are within the distance
            for(i = regionMinX; i <= regionMaxX; ++i){
                for(j = regionMinY; j <= regionMaxY; ++j){
                    streets = Regions[i][j].getStreets();
                    size = streets.length;
                    for(k = 0; k < size; ++k){
                        tmpDistance = calculateDistancePointToStreet(streets[k], x, y, false, tmpPoint);
                        if(tmpDistance < maxDistanceSquared && tmpDistance < bestDistance){
                            bestDistance = tmpDistance;
                            bestStreet = streets[k];
                            nearestPoint[0] = tmpPoint[0];
                            nearestPoint[1] = tmpPoint[1];
                        }
                    }
                }
            }
            distance[0] = bestDistance;
            return bestStreet;
        } else return null;
    }

    /**
     * Calculate the distance between a point and a street.
     *
     * @param street	the street given
     * @param x 		x coordinate of the point
     * @param y 		y coordinate of the point
     * @param sqrt		if set to <code>true</code>, the correct distance is returned; if set to <code>false</code> the square of the distance is returned
     * 					(little bit faster as it saves a call to <code>Math.sqrt()</code>; however, use only if you can handle this!)
     * @param result	an array holding the point on the street so that it can be returned. <code>result[0]</code> holds the x coordinate,
     * 					<code>result[1]</code> the y coordinate. Make sure the array has the correct size (2 elements)!
     *
     * @return the distance as a <code>double</code>. If nothing was found, <code>Double.MAX_VALUE</code> is returned.
     */
    public static double calculateDistancePointToStreet(Street street, int x, int y, boolean sqrt, int[] result){
        if(findNearestPointOnStreet(street, x, y, result)){
            // we got the nearest point on the line. Now calculate the distance between this nearest point and the given point
            long tmp1 = (long)result[0] - x;	//long because x could be smaller 0 and result[0] could be Integer.MAX_VALUE!
            long tmp2 = (long)result[1] - y;
            if(sqrt) return Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2); 	// Pythagorean theorem: a^2 + b^2 = c^2
            else return (tmp1 * tmp1 + tmp2 * tmp2);
        } else return Double.MAX_VALUE;
    }

    /**
     * Calculates the point ON a street which is nearest to a given point (for snapping or such things).
     * This code was inspired by <a href="http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/">
     * Paul Bourke's homepage</a>, especially the Delphi sourcecode (link last visited on 15.08.2008). See
     * there for the mathematical background of this calculation!
     *
     * @param street the street
     * @param x 		the x coordinate of the point
     * @param y 		the y coordinate of the point
     * @param result	an array for the result. <code>result[0]</code> holds the x coordinate, <code>result[1]</code> the y coordinate. Make sure
     * 					the array has the correct size (2 elements), otherwise you will not get a result!
     *
     * @return <code>true</code> if calculation was successful, else <code>false</code>
     */
    public static boolean findNearestPointOnStreet(Street street, int x, int y, int[] result){
        if(result.length == 2){
            int p1_x = street.getStartNode().getX();
            int p1_y = street.getStartNode().getY();
            int p2_x = street.getEndNode().getX();
            int p2_y = street.getEndNode().getY();
            long tmp1 = p2_x-p1_x;
            long tmp2 = p2_y-p1_y;
            long tmp3 = (tmp1*tmp1 + tmp2*tmp2);
            if(tmp3 != 0){
                double u = (((double)x-p1_x)*((double)p2_x-p1_x)+((double)y-p1_y)*((double)p2_y-p1_y))/tmp3;
                if (u >= 1.0){		//point is "outside" the line and nearest to the EndNode
                    result[0] = p2_x;
                    result[1] = p2_y;
                }
                else if (u <= 0.0){		//point is "outside" the line and nearest to the StartNode
                    result[0] = p1_x;
                    result[1] = p1_y;
                }
                else{
                    double tmp4 = p1_x + u * tmp1;
                    result[0] = (int) (tmp4 + 0.5);	//manual rounding
                    tmp4 = p1_y + u * tmp2;
                    result[1] = (int) (tmp4 + 0.5);
                }
            } else {	// not a real street...EndNode and StartNode have the same coordinates!
                result[0] = p1_x;
                result[1] = p1_y;
            }
            return true;
        } else return false;
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
