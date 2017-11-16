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

    /** The priorities for the junction rules. */
    private int[] rulesPriorities_ = null;

    /** The source nodes for the junction rules. */
    private Node[] rulesSourceNodes_ = null;

    /** The target nodes for the junction rules. */
    private Node[] rulesTargetNodes_ = null;




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

    /**
     * Adds a junction rule.
     *
     * @param startNode 	the start node
     * @param targetNode	the target node
     * @param priority		the priority (see {@link #getJunctionPriority(Node, Node)} for details)
     */
    public void addJunctionRule(Node startNode, Node targetNode, int priority){
        Node[] newArray;
        int[] newArray2;
        if(rulesSourceNodes_ == null){
            newArray = new Node[1];
            newArray[0] = startNode;
        } else {
            newArray = new Node[rulesSourceNodes_.length + 1];
            System.arraycopy(rulesSourceNodes_,0,newArray,0,rulesSourceNodes_.length);
            newArray[rulesSourceNodes_.length] = startNode;
        }
        rulesSourceNodes_ = newArray;

        if(rulesTargetNodes_ == null){
            newArray = new Node[1];
            newArray[0] = targetNode;
        } else {
            newArray = new Node[rulesTargetNodes_.length + 1];
            System.arraycopy(rulesTargetNodes_,0,newArray,0,rulesTargetNodes_.length);
            newArray[rulesTargetNodes_.length] = targetNode;
        }
        rulesTargetNodes_ = newArray;

        if(rulesPriorities_ == null){
            newArray2 = new int[1];
            newArray2[0] = priority;
        } else {
            newArray2 = new int[rulesPriorities_.length + 1];
            System.arraycopy(rulesPriorities_,0,newArray2,0,rulesPriorities_.length);
            newArray2[rulesPriorities_.length] = priority;
        }
        rulesPriorities_ = newArray2;
    }

    /**
     * Gets the priority for going over this node.
     *
     * @param startNode 	the node you're coming from
     * @param targetNode the node you're going to
     *
     * @return <code>1</code> if it's possible to go over without any notice, <code>2</code> if it's a right turnoff from a priority street,
     * <code>3</code> if it's a left turnoff from a priority street or <code>4</code> if it's just a normal street (forced to stop at junction)
     * with no need to look for vehicles at target street. <code>5</code> is the same as <code>4</code> but with a need to look for vehicles on
     * target street.
     */
    public int getJunctionPriority(Node startNode, Node targetNode){
        int i, length = rulesPriorities_.length;
        for(i = 0; i < length; ++i){
            if(rulesSourceNodes_[i] == startNode && rulesTargetNodes_[i] == targetNode) return rulesPriorities_[i];
        }
        return 5; 		// node not in list of possible combinations...should only happen in very rare instances
    }

    public Node getNode() {
        return node_;
    }
}
