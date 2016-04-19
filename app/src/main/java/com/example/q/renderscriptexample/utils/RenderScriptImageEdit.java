package com.example.q.renderscriptexample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicResize;
import android.support.v8.renderscript.Type;

import com.example.q.renderscriptexample.ScriptC_RGBAtoYUVA;
import com.example.q.renderscriptexample.ScriptC_YUVAtoRGBA;
import com.example.q.renderscriptexample.ScriptC_yHisto;
import com.example.q.renderscriptexample.ScriptC_yRemap;

/**
 * Created by q on 18/04/2016.
 */
public final class RenderScriptImageEdit {

    private RenderScriptImageEdit(){
        //private constructor for utility class
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, float scale, Context context) {
        //Get image size
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocation = Allocation.createFromBitmap(rs, bitmap);

        //Get new image size
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        //Create type for new image
        Type t = Type.createXY(rs, allocation.getElement(), newWidth, newHeight);
        //Create allocation from type
        Allocation scaledAllocation = Allocation.createTyped(rs, t);

        //Create script
        ScriptIntrinsicResize scaleScript = ScriptIntrinsicResize.create(rs);
        //Set input for script
        scaleScript.setInput(allocation);
        //Call script for output allocation
        scaleScript.forEach_bicubic(scaledAllocation);

        //Create new bitmap
        Bitmap res = Bitmap.createBitmap(newWidth, newHeight, bitmap.getConfig());
        //Copy script result into bitmap
        scaledAllocation.copyTo(res);

        //Destroy everything to free memory
        allocation.destroy();
        scaledAllocation.destroy();
        scaleScript.destroy();
        t.destroy();

        return res;
    }

    public static Bitmap histogramEqualization(Bitmap image, Context context) {
        //Get image size
        int width = image.getWidth();
        int height = image.getHeight();

        //Create new bitmap
        Bitmap res = image.copy(image.getConfig(), true);

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocationA = Allocation.createFromBitmap(rs, res);

        //Create allocation with same type
        Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());

        //Convert from RGB to YUV colorspace
        ScriptC_RGBAtoYUVA rgbAtoYUVA = new ScriptC_RGBAtoYUVA(rs);
        rgbAtoYUVA.forEach_root(allocationA, allocationB);
        rgbAtoYUVA.destroy();

        //Compute Y histogram
        int[] histogram = new int[256];
        Allocation outHistogram = Allocation.createSized(rs, Element.I32(rs), histogram.length);
        //Init allocation with zeros
        outHistogram.copyFrom(histogram);

        ScriptC_yHisto yHistogram = new ScriptC_yHisto(rs);
        yHistogram.bind_gOutarray(outHistogram);
        yHistogram.forEach_root(allocationB);
        outHistogram.copyTo(histogram);
        yHistogram.destroy();
        outHistogram.destroy();

        //Compute new map for Y channel
        float[] lut = new float[256];
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += histogram[i];
            lut[i] = sum / (width*height);
        }

        //Remap Y channel
        ScriptC_yRemap yRemap = new ScriptC_yRemap(rs);
        Allocation map = Allocation.createSized(rs, Element.F32(rs), lut.length);
        map.copyFrom(lut);
        yRemap.bind_remapArray(map);
        yRemap.forEach_root(allocationB, allocationA);
        map.destroy();
        yRemap.destroy();


        //Convert back from YUV to RGB colorspace
        ScriptC_YUVAtoRGBA yuvAtoRGBA = new ScriptC_YUVAtoRGBA(rs);
        yuvAtoRGBA.forEach_root(allocationA, allocationB);
        yuvAtoRGBA.destroy();

        //Copy script result into bitmap
        allocationB.copyTo(res);

        //Destroy everything to free memory
        allocationA.destroy();
        allocationB.destroy();
        rs.destroy();

        return res;
    }

}
