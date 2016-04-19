package com.example.q.renderscriptexample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
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

    public static Bitmap blurBitmap(Bitmap bitmap, float radius, Context context) {
        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocation = Allocation.createFromBitmap(rs, bitmap);

        Type t = allocation.getType();

        //Create allocation with the same type
        Allocation blurredAllocation = Allocation.createTyped(rs, t);

        //Create script
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        //Set blur radius (maximum 25.0)
        blurScript.setRadius(radius);
        //Set input for script
        blurScript.setInput(allocation);
        //Call script for output allocation
        blurScript.forEach(blurredAllocation);

        //Copy script result into bitmap
        blurredAllocation.copyTo(bitmap);

        //Destroy everything to free memory
        allocation.destroy();
        blurredAllocation.destroy();
        blurScript.destroy();
        t.destroy();

        return bitmap;
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
