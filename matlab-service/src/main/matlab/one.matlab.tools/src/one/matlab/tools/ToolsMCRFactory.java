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

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class ToolsMCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "tools_A9667313299AB8696D705B204885430A";
    
    /** Component name */
    private static final String sComponentName = "tools";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(ToolsMCRFactory.class)
        );
    
    
    private ToolsMCRFactory()
    {
        // Never called.
    }
    
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            ToolsMCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{8,0,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
