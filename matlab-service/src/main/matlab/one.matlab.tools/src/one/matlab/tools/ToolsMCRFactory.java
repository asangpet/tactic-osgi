/*
 * MATLAB Compiler: 4.18 (R2012b)
 * Date: Sun Jun 24 19:41:54 2012
 * Arguments: "-B" "macro_default" "-W" "java:one.matlab.tools,Convolution" "-T" 
 * "link:lib" "-d" "C:\\Users\\asangpet\\Documents\\MATLAB\\one.matlab.tools\\src" "-N" 
 * "-p" "images" "-p" "distcomp" "-w" "enable:specified_file_mismatch" "-w" 
 * "enable:repeated_file" "-w" "enable:switch_ignored" "-w" "enable:missing_lib_sentinel" 
 * "-w" "enable:demo_license" "-v" 
 * "class{Convolution:C:\\Users\\asangpet\\Documents\\MATLAB\\conv_cut.m,C:\\Users\\asangpet\\Documents\\MATLAB\\convolve.m,C:\\Users\\asangpet\\Documents\\MATLAB\\deconv_lucy.m,C:\\Users\\asangpet\\Documents\\MATLAB\\deconv_master.m,C:\\Users\\asangpet\\Documents\\MATLAB\\deconv_reg.m,C:\\Users\\asangpet\\Documents\\MATLAB\\deconv_wnr.m,C:\\Users\\asangpet\\Documents\\MATLAB\\findDeconvError.m,C:\\Users\\asangpet\\Documents\\MATLAB\\normalized.m}" 
 * "class{ParallelConvolution:C:\\Users\\asangpet\\Documents\\MATLAB\\deleteJob.m,C:\\Users\\asangpet\\Documents\\MATLAB\\doConv.m,C:\\Users\\asangpet\\Documents\\MATLAB\\doDeconv.m,C:\\Users\\asangpet\\Documents\\MATLAB\\doDeconvLucy.m,C:\\Users\\asangpet\\Documents\\MATLAB\\doDeconvReg.m,C:\\Users\\asangpet\\Documents\\MATLAB\\doDeconvWnr.m,C:\\Users\\asangpet\\Documents\\MATLAB\\getJobStatus.m,C:\\Users\\asangpet\\Documents\\MATLAB\\waitForJob.m}" 
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
    private static final String sComponentId = "tools_105756A4C28ACB58518A4015B0B9ECEF";
    
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
