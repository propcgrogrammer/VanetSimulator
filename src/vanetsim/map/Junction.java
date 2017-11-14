package vanetsim.map;

import vanetsim.debug.Debug;

public class Junction {


    /**
     * /////////////////////////////////////
     * //      instance variable
     * /////////////////////////////////////
     */

    /** The node this junction belongs to. */
    private final Node node_;

    /** An array with all priority street */
    private final Street[] priorityStreets_;




    /**
     * /////////////////////////////////////
     * //      method
     * /////////////////////////////////////
     */

    /**
     * Constructor
     *
     * @param node				the node associated with this junction
     * @param priorityStreets	the priority streets of this junction
     */
    public Junction(Node node, Street[] priorityStreets){

        Debug.whereru(this.getClass().getName(), Debug.ISLOGGED);
        Debug.callFunctionInfo(this.getClass().getName(), "Junction(Node node, Street[] priorityStreets)", Debug.ISLOGGED);

        node_ = node;
        priorityStreets_ = priorityStreets;

    }

    public Node getNode() {
        return node_;
    }
}
