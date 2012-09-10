/*
 * MATLAB Compiler: 4.18 (R2012b)
 * Date: Sun Sep 09 11:54:22 2012
 * Arguments: "-B" "macro_default" "-W" "java:one.matlab.tools,Convolution" "-T" 
 * "link:lib" "-d" 
 * "D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\one.matlab.tools\\src" "-N" 
 * "-p" "images" "-p" "distcomp" "-p" "stats" "-w" "enable:specified_file_mismatch" "-w" 
 * "enable:repeated_file" "-w" "enable:switch_ignored" "-w" "enable:missing_lib_sentinel" 
 * "-w" "enable:demo_license" "-v" 
 * "class{Convolution:D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\conv_cut.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\convolve.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\deconv_lucy.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\deconv_master.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\deconv_reg.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\deconv_wnr.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\findDeconvError.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\normalized.m}" 
 * "class{ParallelConvolution:D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\deleteJob.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\doConv.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\doDeconv.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\doDeconvLucy.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\doDeconvReg.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\doDeconvWnr.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\getJobStatus.m,D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\waitForJob.m}" 
 * "class{Distribution:D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\dist_gevpdf.m}" 
 */

package one.matlab.tools;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;
import java.util.*;

/**
 * The <code>Distribution</code> class provides a Java interface to the M-functions
 * from the files:
 * <pre>
 *  D:\\workspace\\tactic\\matlab-service\\src\\main\\matlab\\dist_gevpdf.m
 * </pre>
 * The {@link #dispose} method <b>must</b> be called on a <code>Distribution</code> 
 * instance when it is no longer needed to ensure that native resources allocated by this 
 * class are properly freed.
 * @version 0.0
 */
public class Distribution extends MWComponentInstance<Distribution>
{
    /**
     * Tracks all instances of this class to ensure their dispose method is
     * called on shutdown.
     */
    private static final Set<Disposable> sInstances = new HashSet<Disposable>();

    /**
     * Maintains information used in calling the <code>dist_gevpdf</code> M-function.
     */
    private static final MWFunctionSignature sDist_gevpdfSignature =
        new MWFunctionSignature(/* max outputs = */ 1,
                                /* has varargout = */ false,
                                /* function name = */ "dist_gevpdf",
                                /* max inputs = */ 4,
                                /* has varargin = */ false);

    /**
     * Shared initialization implementation - private
     */
    private Distribution (final MWMCR mcr) throws MWException
    {
        super(mcr);
        // add this to sInstances
        synchronized(Distribution.class) {
            sInstances.add(this);
        }
    }

    /**
     * Constructs a new instance of the <code>Distribution</code> class.
     */
    public Distribution() throws MWException
    {
        this(ToolsMCRFactory.newInstance());
    }
    
    private static MWComponentOptions getPathToComponentOptions(String path)
    {
        MWComponentOptions options = new MWComponentOptions(new MWCtfExtractLocation(path),
                                                            new MWCtfDirectorySource(path));
        return options;
    }
    
    /**
     * @deprecated Please use the constructor {@link #Distribution(MWComponentOptions componentOptions)}.
     * The <code>com.mathworks.toolbox.javabuilder.MWComponentOptions</code> class provides API to set the
     * path to the component.
     * @param pathToComponent Path to component directory.
     */
    public Distribution(String pathToComponent) throws MWException
    {
        this(ToolsMCRFactory.newInstance(getPathToComponentOptions(pathToComponent)));
    }
    
    /**
     * Constructs a new instance of the <code>Distribution</code> class. Use this 
     * constructor to specify the options required to instantiate this component.  The 
     * options will be specific to the instance of this component being created.
     * @param componentOptions Options specific to the component.
     */
    public Distribution(MWComponentOptions componentOptions) throws MWException
    {
        this(ToolsMCRFactory.newInstance(componentOptions));
    }
    
    /** Frees native resources associated with this object */
    public void dispose()
    {
        try {
            super.dispose();
        } finally {
            synchronized(Distribution.class) {
                sInstances.remove(this);
            }
        }
    }
  
    /**
     * Invokes the first m-function specified by MCC, with any arguments given on
     * the command line, and prints the result.
     */
    public static void main (String[] args)
    {
        try {
            MWMCR mcr = ToolsMCRFactory.newInstance();
            mcr.runMain( sDist_gevpdfSignature, args);
            mcr.dispose();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    /**
     * Calls dispose method for each outstanding instance of this class.
     */
    public static void disposeAllInstances()
    {
        synchronized(Distribution.class) {
            for (Disposable i : sInstances) i.dispose();
            sInstances.clear();
        }
    }

    /**
     * Provides the interface for calling the <code>dist_gevpdf</code> M-function 
     * where the first input, an instance of List, receives the output of the M-function and
     * the second input, also an instance of List, provides the input to the M-function.
     * @param lhs List in which to return outputs. Number of outputs (nargout) is
     * determined by allocated size of this List. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs List containing inputs. Number of inputs (nargin) is determined
     * by the allocated size of this List. Input arguments may be passed as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or
     * as arrays of any supported Java type. Arguments passed as Java types are
     * converted to MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void dist_gevpdf(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sDist_gevpdfSignature);
    }

    /**
     * Provides the interface for calling the <code>dist_gevpdf</code> M-function 
     * where the first input, an Object array, receives the output of the M-function and
     * the second input, also an Object array, provides the input to the M-function.
     * @param lhs array in which to return outputs. Number of outputs (nargout)
     * is determined by allocated size of this array. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs array containing inputs. Number of inputs (nargin) is
     * determined by the allocated size of this array. Input arguments may be
     * passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void dist_gevpdf(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sDist_gevpdfSignature);
    }

    /**
     * Provides the standard interface for calling the <code>dist_gevpdf</code>
     * M-function with 4 input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] dist_gevpdf(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sDist_gevpdfSignature), 
                    sDist_gevpdfSignature);
        return lhs;
    }
}
