import graph_element.Edge;
import graph_element.Vertex;
import imoprt_exoprt_jgrapht_gxl.Import_JGraphT_From_GXL;
import org.jgrapht.UndirectedGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
public class Main {
    private static float cost_node_sub = 0;
    private static float cost_node_del_ins = 0;

    private static float cost_edge_sub = 0;
    private static float cost_edge_del_ins = 0;

    private static String file_g1 = null;
    private static String file_g2 = null;

    private static PrintWriter out = null;

    private static String separator = ";";
    public static int k_best;
    public static int dinamique;


    public static void  main(String[] args) {
        String pathname_result_output_file = null;
        String test_benchmark = null; // CMU, MUTA, GREC, PATH (acyclic, alkane, pah, mao)
        int amount_RunTime_S = 0;

        // Print the vertices and edges of the imported graph
        if (args.length >= 9) {
            try {
                cost_node_sub = Float.parseFloat(args[0]);
                cost_node_del_ins = Float.parseFloat(args[1]);
                cost_edge_sub = Float.parseFloat(args[2]);
                cost_edge_del_ins = Float.parseFloat(args[3]);

                file_g1 = args[4];
                file_g2 = args[5];

                pathname_result_output_file = args[6];

                test_benchmark = args[7];

                amount_RunTime_S = Integer.parseInt(args[8]);
                dinamique = Integer.parseInt(args[9]);
                if (dinamique == 0) {
                    k_best = Integer.parseInt(args[10]);
                } else {
                    k_best = 300;
                }

            } catch (NumberFormatException e) {
                System.err.println("Argument : " + e.getMessage() + " must be an integer.");
                System.exit(1);
            }
        }


        UndirectedGraph<Vertex, Edge> g1 = null;
        UndirectedGraph<Vertex, Edge> g2 = null;

        if (test_benchmark.equals("PATH")) {
            g1 = Import_JGraphT_From_GXL.import_simple_graph(file_g1);
            g2 = Import_JGraphT_From_GXL.import_simple_graph(file_g2);

        } else if (test_benchmark.equals("GREC")) {
            g1 = Import_JGraphT_From_GXL.import_simple_graph_from_GREC_GED(file_g1);
            g2 = Import_JGraphT_From_GXL.import_simple_graph_from_GREC_GED(file_g2);
        }else if (test_benchmark.equals("PROT")) {
            g1= Import_JGraphT_From_GXL.GxlReader(file_g1) ;
            g2= Import_JGraphT_From_GXL.GxlReader(file_g2) ;
           // g1 = Import_JGraphT_From_GXL.import_simple_graph_from_GREC_GED(file_g1);
             //  g2 = Import_JGraphT_From_GXL.import_simple_graph_from_GREC_GED(file_g2);
        } else if (test_benchmark.equals("MUTA")) {
           // g1= Import_JGraphT_From_GXL.GxlReader(file_g1) ;
            g1 = Import_JGraphT_From_GXL.import_simple_graph_from_MUTA_GED(file_g1);
             g2 = Import_JGraphT_From_GXL.import_simple_graph_from_MUTA_GED(file_g2);
        } else if (test_benchmark.equals("CMU")) {
            g1 = Import_JGraphT_From_GXL.import_simple_graph_from_CMU_GED(file_g1);
            g2 = Import_JGraphT_From_GXL.import_simple_graph_from_CMU_GED(file_g2);
        } else {
            g1 = Import_JGraphT_From_GXL.import_simple_graph_GED(file_g1);
            g2 = Import_JGraphT_From_GXL.import_simple_graph_GED(file_g2);
        }

        System.out.println(file_g1+ ""+ file_g2);
        if (g1 == null) {
            System.out.println("Can't Load the First Graph : " + file_g1);
            return;
        }
        if(g2==null)
        {   System.out.println("Can't Load the Second Graph : "+file_g2);
            return;
        }


        //Exact_GED_AStar exact_GED = new Exact_GED_AStar();
        Exact_GED_BB_DF exact_GED = new Exact_GED_BB_DF(amount_RunTime_S);
        GED_Operations_Cost.setAll_Operations_cost(cost_node_sub, cost_node_del_ins, cost_node_del_ins * 1, cost_edge_sub, cost_edge_del_ins * 1, cost_edge_del_ins, 1);
        GED_Operations_Cost.setIs_Weight(true);
        GED_Operations_Cost.setIs_Label(true);

        // ---------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------

       /*try (
                BufferedReader br = new BufferedReader(new FileReader(file_g2 + "train.cxl"));

                //  BufferedReader br2 = new BufferedReader(new FileReader(file_g2+"/test.cxl"));
        ) {
            String line, line2;
           /* while ((line2 = br2.readLine()) != null) {
                // Process the XML line he
                if(line2.length()>28) {  String bench1 = line2.substring(13, 30);
                    System.out.println(bench1);
                    exact_GED.optimal_Path=null;
                    exact_GED.upper_bound=100000000;
                    String gg1= file_g2+"/Mutagenicity/"+bench1;
                    //   System.out.println(gg2);
                    g1 = Import_JGraphT_From_GXL.import_simple_graph_from_MUTA_GED(gg1);*/
          /*  int i = 0;
            while ((line = br.readLine()) != null) {
                // Process the XML line he
                if (line.length() > 28)
               {
                    int ind1=line.indexOf('"');
                line.charAt(ind1);
              String line1=line.substring(ind1+1);
                int ind2= line1.indexOf('"');
              //  System.out.println(line1+""+ind1+" "+ind2);

                    String banch = line.substring(ind1+1, ind1+ind2+1);
                    String gg2 = file_g2 + banch;// "/Mutagenicity/" + banch;
                    //  System.err.println(banch+" "+i);
                    // i++;
                   if (test_benchmark.equals("MUTA")) {
                    g2 = Import_JGraphT_From_GXL.import_simple_graph_from_MUTA_GED(gg2);}
                   else if (test_benchmark.equals("PROT")) {
                    g2= Import_JGraphT_From_GXL.GxlReader(gg2) ;}
                   else{System.err.println("No reader supported for G2");}

                   JGraphTtoGPU g2_gpu = new JGraphTtoGPU();
                   float[] mat_weight= g2_gpu.edge_weight_Matrix(g2);
                    String[] vertices_label= g2_gpu.get_vertex_label_list(g2);
 */
                    //JGraphTtoGPU.send_graph_data_GPU(g2);
                    //JGraphTtoGPU.main(args);
                   /*for (int i0=0;i0< g2.vertexSet().size()*g2.vertexSet().size();i0++)
                   {
                       if(i0%g2.vertexSet().size()==0)System.out.println("");
                       System.out.print(","+mat_weight[i0]);
                   }*/



                    /*for (int i0=0;i0< g2.vertexSet().size()*g2.vertexSet().size();i0++)
                    {
                        if(i0%g2.vertexSet().size()==0)System.out.println("");
                        System.out.print(","+mat_label[i0]);
                    }*/



                  /*  for (int i0=0;i0< g2.vertexSet().size();i0++)
                    {
                        if(i0%g2.vertexSet().size()==0)System.out.println("");
                        System.out.print(","+vertices_label[i0]);
                    }*/

                    long startTime = System.nanoTime();
                   /* if (g1.vertexSet().size() > 200) {
                        k_best = 1;
                    } else if (g1.vertexSet().size() > 100) {
                        k_best = 10;
                    } else if (g1.vertexSet().size() > 50) {
                        k_best = 50;
                    } else {
                        k_best = 500;
                    }*/
                    double editDistance = exact_GED.computeGED(g1, g2);
                    System.out.println();
                    long endTime = System.nanoTime();

                    long elapsedTime = endTime - startTime;
               // } // Or do something else with the line
            //}
            //}
            //  }
     /*   } catch (IOException e) {
            e.printStackTrace();
        }*/


        // ---------------------------------------------------------------------------------------
        // ---------------------------------------------------------------------------------------

        /// String output= "test__"+new File(file_g1).getName()+"__"+new File(file_g2).getName();

     /*   try {
            File file = new File(pathname_result_output_file);

            if(file.getParentFile()!=null){
                file.getParentFile().mkdirs();
            }

         //   out = new PrintWriter(new FileWriter(file, true), true);

           // out.println(file_g1+separator+file_g2+separator + editDistance + separator + TimeUnit.NANOSECONDS.toSeconds(elapsedTime)+separator +TimeUnit.NANOSECONDS.toMillis(elapsedTime) + separator + exact_GED.nb_all_path_added_to_open + separator + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + separator + exact_GED.optimal_Path );

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (out != null)
                out.close();
        }*/

        /*
        System.out.println(exact_GED.optimal_Path.toString());

        System.out.println(editDistance);

        System.out.println("\n nb_all_path_added_to_open = "+exact_GED.nb_all_path_added_to_open);

        System.out.println("---------------------------------------- time : ");
        System.out.println("Total execution time: " +elapsedTime +" Nanos");
        System.out.println("Total execution time: " +TimeUnit.NANOSECONDS.toMicros(elapsedTime)+" Micros");
        System.out.println("Total execution time: " +TimeUnit.NANOSECONDS.toMillis(elapsedTime)+" Millis");
        */

        /*
        System.out.println("Total execution time: " +
                String.format("%d min, %d sec",
                        TimeUnit.NANOSECONDS.toHours(elapsedTime),
                        TimeUnit.NANOSECONDS.toSeconds(elapsedTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(elapsedTime))));
                                */


    }
}
