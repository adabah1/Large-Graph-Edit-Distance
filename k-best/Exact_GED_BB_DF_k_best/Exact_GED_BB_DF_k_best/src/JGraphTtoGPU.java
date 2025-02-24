import graph_element.Edge;
import graph_element.Vertex;
import jcuda.Pointer;
import jcuda.Sizeof;
import jcuda.driver.*;
import org.jgrapht.UndirectedGraph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static jcuda.driver.JCudaDriver.*;

public class JGraphTtoGPU {


    public float [] edge_weight_matrix;
   // public static String [] edge_label_matrix;
    public  float [] vertex_weight_vec;
    //public static String [] vertex_label_vec;
    public  CUdeviceptr device_adj_mat;
    public CUdeviceptr device_weight_vertex ;
    public void set_data(UndirectedGraph<Vertex, Edge> g){
        edge_weight_matrix= edge_weight_Matrix(g);
      //  edge_label_matrix= JGraphTtoGPU.edge_label_Matrix(g);
      //  vertex_label_vec= JGraphTtoGPU.get_vertex_label_list(g);
        vertex_weight_vec= get_Vertex_weight_list(g);
    }

    public void set_gpu_data(UndirectedGraph<Vertex, Edge> g){
        int nb_verticex = g.vertexSet().size();

        // Allocate device memory for the array
        device_adj_mat =  new CUdeviceptr();
        // Pointer device_label_mat = new Pointer();
        device_weight_vertex  = new CUdeviceptr();
        // Pointer device_lebel_vertex = new Pointer();
        cuMemAlloc(device_adj_mat, nb_verticex *nb_verticex* Sizeof.FLOAT);
        cuMemcpyHtoD(device_adj_mat, Pointer.to(this.edge_weight_matrix),nb_verticex * nb_verticex* Sizeof.FLOAT);
        cuMemAlloc(device_weight_vertex, nb_verticex * Sizeof.FLOAT);
        cuMemcpyHtoD(device_weight_vertex, Pointer.to(this.vertex_weight_vec), nb_verticex* Sizeof.FLOAT);
    }

    public  float[] edge_weight_Matrix(UndirectedGraph<Vertex, Edge> graph) {

        float [] mat = new float[(graph.vertexSet().size())*(graph.vertexSet().size())];
        Object[] v= graph.vertexSet().toArray();

        for (int i=0;i<graph.vertexSet().size();i++)
            for (int j=0;j<graph.vertexSet().size();j++)
            { Vertex vi= (Vertex) v[i]; Vertex vj= (Vertex) v[j];
                if(graph.containsEdge(vi,vj)==true){
                    Edge e= graph.getEdge(vi,vj);
                    if(e.getWeight()>=0) {
                        mat[i*(graph.vertexSet().size())+j]=e.getWeight();
                    }else {mat[i*(graph.vertexSet().size())+j]=0;}

                }else {mat[i*(graph.vertexSet().size())+j]=0;}
            }
        return  mat;
    }
    public   String[] edge_label_Matrix(UndirectedGraph<Vertex, Edge> graph) {
        String[] mat_label = new String[(graph.vertexSet().size()) * (graph.vertexSet().size())];
        Object[] v= graph.vertexSet().toArray();
        for (int i = 0; i < graph.vertexSet().size(); i++)
            for (int j = 0; j < graph.vertexSet().size(); j++) {
                Vertex vi = (Vertex) v[i];
                Vertex vj = (Vertex) v[j];
                if (graph.containsEdge(vi, vj) == true) {
                    Edge e = graph.getEdge(vi, vj);
                    if (e.getEdgeLabale() != null) {
                        mat_label[i * (graph.vertexSet().size() ) + j] = e.getEdgeLabale();
                    } else {
                        mat_label[i * (graph.vertexSet().size() ) + j] = "";
                    }
                } else {
                    mat_label[i * (graph.vertexSet().size() ) + j] = "";
                }


            }
        return mat_label;
    }


    public  String[] get_vertex_label_list(UndirectedGraph<Vertex, Edge> graph) {
        Object[] v= graph.vertexSet().toArray();
        String[] v_label = new String[graph.vertexSet().size()];
        for (int j = 0; j < graph.vertexSet().size(); j++) {
            Vertex vj = (Vertex) v[j];
            if (vj.getVertexLabel() != null) {
                v_label[j] = vj.getVertexLabel();

            } else {
                v_label[j] = "";
            }

        }
        return v_label;
    }
    public float[] get_Vertex_weight_list(UndirectedGraph<Vertex, Edge>graph ){
        Object[] v= graph.vertexSet().toArray();
        float[] v_weight = new float[graph.vertexSet().size()];
        for (int j=0;j<graph.vertexSet().size();j++) {
            Vertex vj = (Vertex) v[j];
            if (vj.getVertexLabel() != null) {
                v_weight[j] = vj.getVertex_weight();
            } else {
                v_weight[j] = 0;
            }
        }

        return  v_weight;
    }





    public void send_graph_data_GPU(UndirectedGraph<Vertex, Edge> graph) throws IOException {

        // Enable exceptions and omit all subsequent error checks
        JCudaDriver.setExceptionsEnabled(true);

        // Create the PTX file by calling the NVCC
        String ptxFileName = preparePtxFile("JCudaVectorAddKernel.cu");

        // Initialize the driver and create a context for the first device.
        cuInit(0);
        CUdevice device = new CUdevice();
        cuDeviceGet(device, 0);
        CUcontext context = new CUcontext();
        cuCtxCreate(context, 0, device);

        // Load the ptx file.
        CUmodule module = new CUmodule();
        cuModuleLoad(module, ptxFileName);

        // Obtain a function pointer to the "add" function.
        CUfunction function = new CUfunction();
        cuModuleGetFunction(function, module, "add");


        int nb_verticex = graph.vertexSet().size();

        set_data (graph );
        set_gpu_data(graph);

        // Allocate device output memory
        CUdeviceptr deviceOutput = new CUdeviceptr();
        cuMemAlloc(deviceOutput, nb_verticex* Sizeof.FLOAT);

        Pointer kernelParameters = Pointer.to(Pointer.to(new int[]{nb_verticex}),Pointer.to(device_weight_vertex),Pointer.to(device_adj_mat),Pointer.to(deviceOutput));

        // Call the kernel function.
        int blockSizeX = 1000;
        int gridSizeX = (int)Math.ceil((double)1000/ blockSizeX);
        cuLaunchKernel(function,
                gridSizeX,  1, 1,      // Grid dimension
                blockSizeX, 1, 1,      // Block dimension
                0, null,               // Shared memory size and stream
                kernelParameters, null // Kernel- and extra parameters
        );
        cuCtxSynchronize();

        // Allocate host output memory and copy the device output
        // to the host.
        float hostOutput[] = new float[nb_verticex];
        cuMemcpyDtoH(Pointer.to(hostOutput), deviceOutput,
                nb_verticex* Sizeof.FLOAT);

        // Verify the result
        boolean passed = true;
        for(int i = 0; i < nb_verticex; i++)
        {
            float expected = vertex_weight_vec[i];
            if (Math.abs(hostOutput[i] - expected) > 1e-5)
            {
                System.out.println(
                        "At index "+i+ " found "+hostOutput[i]+
                                " but expected "+expected);
                passed = false;
                break;
            }
        }
        System.out.println("Test "+(passed?"PASSED":"FAILED"));



        // Clean up
        cuMemFree(device_adj_mat);
        cuMemFree(device_weight_vertex);
        cuMemFree(deviceOutput);


    }


    /**
     * The extension of the given file name is replaced with "ptx".
     * If the file with the resulting name does not exist, it is
     * compiled from the given file using NVCC. The name of the
     * PTX file is returned.
     *
     * @param cuFileName The name of the .CU file
     * @return The name of the PTX file
     * @throws IOException If an I/O error occurs
     */
    private static String preparePtxFile(String cuFileName) throws IOException
    {
        int endIndex = cuFileName.lastIndexOf('.');
        if (endIndex == -1)
        {
            endIndex = cuFileName.length()-1;
        }
        String ptxFileName = cuFileName.substring(0, endIndex+1)+"ptx";
        File ptxFile = new File(ptxFileName);
        if (ptxFile.exists())
        {
            return ptxFileName;
        }

        File cuFile = new File(cuFileName);
        if (!cuFile.exists())
        {
            throw new IOException("Input file not found: "+cuFileName);
        }
        String modelString = "-m"+System.getProperty("sun.arch.data.model");
        String command =
                "nvcc " + modelString + " -ptx "+
                        cuFile.getPath()+" -o "+ptxFileName;

        System.out.println("Executing\n"+command);
        Process process = Runtime.getRuntime().exec(command);

        String errorMessage =
                new String(toByteArray(process.getErrorStream()));
        String outputMessage =
                new String(toByteArray(process.getInputStream()));
        int exitValue = 0;
        try
        {
            exitValue = process.waitFor();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "Interrupted while waiting for nvcc output", e);
        }

        if (exitValue != 0)
        {
            System.out.println("nvcc process exitValue "+exitValue);
            System.out.println("errorMessage:\n"+errorMessage);
            System.out.println("outputMessage:\n"+outputMessage);
            throw new IOException(
                    "Could not create .ptx file: "+errorMessage);
        }

        System.out.println("Finished creating PTX file");
        return ptxFileName;
    }

    /**
     * Fully reads the given InputStream and returns it as a byte array
     *
     * @param inputStream The input stream to read
     * @return The byte array containing the data from the input stream
     * @throws IOException If an I/O error occurs
     */
    private static byte[] toByteArray(InputStream inputStream)
            throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buffer[] = new byte[8192];
        while (true)
        {
            int read = inputStream.read(buffer);
            if (read == -1)
            {
                break;
            }
            baos.write(buffer, 0, read);
        }
        return baos.toByteArray();
    }


}
