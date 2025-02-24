import graph_element.Vertex;
import org.jgrapht.Graph;

import java.util.*;
// Exact Graph Edit Distance (GED) with Branch and Bound (BB) Depth First stratigy (DF)

public class Exact_GED_BB_DF {

    /**
     * The set OPEN of partial edit paths contains the search tree nodes
     * to be processed in the next steps.
     */

    class IndexValuePair {
        int index1;
        int index2;
        double value;

        public IndexValuePair(int index1, int index2, double value) {
            this.index1 = index1;
            this.index2 = index2;
            this.value = value;
        }
    }

    private Stack<Path> path_Stack; /* In the branch and Bound Depth first we use stack to manage the backtraking in the search tree,
	 									when we complete to process an intermediate node, we back to it's father.*/

    double upper_bound = Double.MAX_VALUE;

    private long amount_RunTime = 0;

    // Just for debugging purpose
    Path optimal_Path;

    int nb_all_path_added_to_open = 0; // just for debugging

    /**
     * Default Constructor
     */
    public Exact_GED_BB_DF() {
        path_Stack = new Stack<Path>();
    }

    public Exact_GED_BB_DF(long amount_RunTime) {

        path_Stack = new Stack<Path>();
        this.amount_RunTime = amount_RunTime;
    }

    /**
     * Compute the exact edit distance between two graphs.
     * The method implements A* algorithm
     *
     * @param g1 from graph
     * @param g2 to graph
     * @return returns the exact edit distance between the to graphs
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public double computeGED(Graph g1, Graph g2) {
        PriorityQueue<Path> pq = new PriorityQueue<Path>(1, new PathComparator());
        //upper_bound=100000000;
        //	PriorityQueue<Path> pq_best = new PriorityQueue<Path>(1, new PathComparator());
        // if both graph are empty, then the edit distance is "0"
        optimal_Path = null;
        if (g1.vertexSet().size() == 0 && g2.vertexSet().size() == 0) {
            return 0;
        }
        // if the graph g1 is empty (g2 is not), then
        // edit distance is = the cost of (insertion of all vertices of g2) + (insertion of all edges of g2)

        if (g1.vertexSet().size() == 0) {
            return (GED_Operations_Cost.getVertex_insertion_cost() * g2.vertexSet().size() + GED_Operations_Cost.getEdge_insertion_cost() * g2.edgeSet().size());
        }

        if (g1.vertexSet().size() > g2.vertexSet().size()) {
            Graph g3 = g1;
            g1 = g2;
            g2 = g3;
        }

        // if the graph g1 have vertices and edges; and g2 is empty, then
        // edit distance is = the cost of (deletion of all vertices of g1) + (deletion of all edges of g1)

        if (g2.vertexSet().size() == 0) {
            return (GED_Operations_Cost.getVertex_deletion_cost() * g1.vertexSet().size() + GED_Operations_Cost.getEdge_deletion_cost() * g1.edgeSet().size());
        }


        // local varibal, listV1: set of vertices of g1 ; listV2: set of vertices of g2
        ArrayList<Vertex> listV1 = new ArrayList<>(g1.vertexSet());
        ArrayList<Vertex> listV2 = new ArrayList<>(g2.vertexSet());

        //  upper_bound =1000;// h_function.compute(g1,g2,"max");
        //upper_bound=111;//60;//4300;
        // ---------------------- step 1 : Prepare initial paths (first level of the search tree)

        // Custom comparator to compare IndexValuePair objects based on their values
        Comparator<IndexValuePair> valueComparator = Comparator.comparingDouble(ivp -> ivp.value);

        // Create a PriorityQueue with the custom comparator
        PriorityQueue<IndexValuePair> prioritynext = new PriorityQueue<>(valueComparator);

        // Add elements to the PriorityQueue
        prioritynext.add(new IndexValuePair(0, 1, 5));
        prioritynext.add(new IndexValuePair(1, 1, 3));
        prioritynext.add(new IndexValuePair(2, 2, 7));

        // Poll elements from the PriorityQueue based on their values
        while (!prioritynext.isEmpty()) {
            IndexValuePair ivp = prioritynext.poll();
            //System.out.println("Index: " + ivp.index1 + ", Value: " + ivp.value);
        }
        // substitution
        for (Vertex w : listV2) {
            Edit_Operation vertex_substitution_operation = new Edit_Operation(listV1.get(0), w);

            Path path = new Path(g1, g2);
            path.add(vertex_substitution_operation);
            // Insert substitution (u1 --> w) into OPEN

            if (path.getG_cost_PLUS_h_cost() <= (((upper_bound + 1000)))) {
                //	path_Stack.push(path);
                pq.add(path);
                //OPEN.add(path);
                nb_all_path_added_to_open++;
            }
        }

        // deletion
        Edit_Operation vertex_deletion_operation = new Edit_Operation(listV1.get(0), null);
        Path path0 = new Path(g1, g2);
        path0.add(vertex_deletion_operation);
        // Insert deletion (u1 --> null) into OPEN

        if (path0.getG_cost_PLUS_h_cost() <= (((upper_bound + 1000)))) {
            //  path_Stack.push(path0);
            pq.add(path0);
            nb_all_path_added_to_open++;
        }

        Path[] pp;
        Path pMin;
        //while(true)
        int k = Main.k_best;//
        int bar = 1000;
        if (g1.vertexSet().size() < 22) bar = 40;//path_Stack.size();
        int it = 0;
        int nb1 = 0;
        Path[] pp1 = new Path[1000000];
        while (!pq.isEmpty()/*&&nb1<k*/) {
            nb1++;
            //   System.out.println(pq.peek().getG_cost_PLUS_h_cost());
            path_Stack.push(pq.poll());
        }
        while (!path_Stack.empty()) // a loop to process all the vertices in g1, wa treat all the sub-tree.
        {
            it++;
            prioritynext.clear();
            double max = 0;
            // Return pMin and delete all argmin_p{g(p)+h(p)} paths
            // get the node pMin from OPEN, this depend on the strategy implemented.
            int size = path_Stack.size();
            int ssi = 0;
            for (int i = 0; i < size; i++) {

                pMin = argMin();
                //  System.out.println(path_Stack.size()+"i:"+i);

                if (pMin.isCompleteEditPath()) {
                    if (optimal_Path == null) {
                        optimal_Path = new Path(pMin); // Make a copy for debugging purpose

                        upper_bound = pMin.getG_cost();
                        //  System.out.println("G_cost = "+optimal_Path.getG_cost()+" | g+h = "+optimal_Path.getG_cost_PLUS_h_cost()+" | upper_bound = "+upper_bound);
                    } else {
                        if (pMin.getG_cost() <= ((upper_bound + 1000))/*optimal_Path.getG_cost()*/) {
                            optimal_Path = new Path(pMin); // Make a copy for debugging purpose
                            upper_bound = pMin.getG_cost();
                            // System.out.println(optimal_Path.toString());
                            // System.out.println("G_cost = "+optimal_Path.getG_cost()+" | g+h = "+optimal_Path.getG_cost_PLUS_h_cost()+" | upper_bound = "+upper_bound);
                        }
                    }
                } else {
                    /// Let min_path = {u_1 --> v_i1,...,u_k --> v_ik}
                    List<Vertex> remainsV2 = pMin.getRemaining_unprocessed_vertex_g2();
                    int index_processed_vertices_g1 = pMin.getIndex_processed_vertices_g1();


                    if (index_processed_vertices_g1 < listV1.size()) {
                        // substitution
                        double dp_min = pMin.getG_cost_PLUS_h_cost();
                        int index_v2 = 0;
                        for (Vertex w1 : remainsV2) {
                            Path newP1 = new Path(pMin);

                            Edit_Operation newOp1 = new Edit_Operation(listV1.get(index_processed_vertices_g1), w1);
                            //	double cost=newP1.add_cost(newOp1);
                            newP1.add(newOp1);
                            // Add substitution to pMin, then add the new path to OPEN

                            // System.out.println("---------- Midele node : g+h = "+newP1.getG_cost_PLUS_h_cost()+" cost:"+cost);

                            if (newP1.getG_cost_PLUS_h_cost() <= (((upper_bound + 1000)))) {
                                //     path_Stack.push(newP1);
                                //if(pq.size()>k)
                                {
                                    //  Iterator pq_iterator = pq.iterator();
                                    //if(pq_iterator.)
                                    if (pq.size() > k && i > 0) {// if(pp1[ssi-1].getG_cost_PLUS_h_cost()>newP1.getG_cost_PLUS_h_cost())
                                        {
                                            pq.add(newP1);
                                        }
                                    } else {
                                        pq.add(newP1);
                                    }
                                    //System.out.println("|"+pq.size());


                                }
                                nb_all_path_added_to_open++;
                            }
                            //vertex_Cost = GED_Operations_Cost.getVertex_substitution_cost(newOp1.getFromVertex(),newOp1.getToVertex());
                            //	prioritynext.add(new IndexValuePair(i,index_v2, dp_min));
                            index_v2++;
                        }

                        // deletion
                        Path newP2 = new Path(pMin);
                        Edit_Operation newOp2 = new Edit_Operation(listV1.get(index_processed_vertices_g1), null);
                        newP2.add(newOp2);
                        // Add deletion to pMin, then add the new path to OPEN

                        /// System.out.println("---------- Midele node : g+h = "+newP2.getG_cost_PLUS_h_cost());

                        if (newP2.getG_cost_PLUS_h_cost() <= (((upper_bound + 1000)))) {
                            //  path_Stack.push(newP2);
                            if (pq.size() > k && i > 0) { //if(pp1[ssi-1].getG_cost_PLUS_h_cost()>newP2.getG_cost_PLUS_h_cost())
                                {
                                    pq.add(newP2);

                                }
                            } else {
                                pq.add(newP2);
                            }
                            nb_all_path_added_to_open++;

                        }
                    } else {
                        Path newP3 = new Path(pMin);
                        for (Vertex w2 : remainsV2) {
                            Edit_Operation newOp3 = new Edit_Operation(null, w2);
                            newP3.add(newOp3);
                        }
                        // Add insertion to pMin, then add the new path to OPEN

                        /// System.out.println("---------- Midele node : g+h = "+newP3.getG_cost_PLUS_h_cost());

                        if (newP3.getG_cost_PLUS_h_cost() <= (((upper_bound + 1000)))) {
                            //    path_Stack.push(newP3);
                            pq.add(newP3);
                            nb_all_path_added_to_open++;
                        }
                    }

                }


                if (i == 0) {
                    pq.toArray(pp1);
                    ssi = pq.size();
                    //System.out.println(pp1[ssi-1]);
                }

                //	if(pq.size()>10000)	System.out.println(pq.size()+"|"+pp1[00].getG_cost_PLUS_h_cost()+"|"+pp1[100].getG_cost_PLUS_h_cost());
            }
            //    System.out.println("pq-size:"+pq.size()+"path_Stack size:"+path_Stack.size());
            int nb = 0; //k=k+20+(int)0;new Path[0]
            //if(it>5)k=50;
            double v_last = 0;
            while (!pq.isEmpty() && nb < k) {
                nb++;
                v_last = pq.peek().getG_cost_PLUS_h_cost();
                //	System.out.print("|"+pq.peek().getG_cost_PLUS_h_cost());
                path_Stack.push(pq.poll());
            }
            //System.out.println("\n");
            if (Main.dinamique == 1 && path_Stack.size() > 0)
                while (!pq.isEmpty() && v_last == pq.peek().getG_cost_PLUS_h_cost() && nb < bar) {
                    nb++;
                    //System.out.println(it+"::"+nb+"::"+v_last+"::"+pq.peek().getG_cost_PLUS_h_cost());
                    path_Stack.add(0, pq.poll());
                }
            pq.clear();
        }
        if (optimal_Path != null) {
            System.out.print(optimal_Path.getG_cost());
        } else {
            System.out.print(10000);
        }
        return 0;//optimal_Path.getG_cost();
    }


    /**
     * Get pMin and delete all argmin_p{g(p)+h(p)} paths
     *
     * @return returns the path p with the minimum g(p)+h(p)
     */
    private Path argMin() {
        //Path p = OPEN.poll();
        Path p = path_Stack.pop();

        //while(p.getG_cost_PLUS_h_cost() == OPEN.peek().getG_cost_PLUS_h_cost()) // Double comparison !!! To be fixed!
        //	OPEN.poll();

        return p;
    }
}
