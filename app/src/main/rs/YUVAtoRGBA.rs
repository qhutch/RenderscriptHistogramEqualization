#pragma version(1)
#pragma rs java_package_name(com.example.q.renderscriptexample)

#include "rs_debug.rsh"

//Method to keep the result between 0 and 1
static float bound (float val) {
    return fmin(1.0f, fmax(0.0f, val));
}

void root(const uchar4 *v_in, uchar4 *v_out, const void *usrData, uint32_t x, uint32_t y) {
    //Convert input uchar4 to float4
    float4 f4 = rsUnpackColor8888(*v_in);

    //Get value for Y channel
    float Y = f4.r;
    //Get value for U and V channel (back to their original values)
    float U = (2*f4.g)-1;
    float V = (2*f4.b)-1;

    //Compute values for red, green and blue channels
    float red = bound(Y + 1.14f * V);
    float green = bound(Y - 0.395f * U - 0.581f * V);
    float blue = bound(Y + 2.033f * U);

     //Put the values in the output uchar4
    *v_out = rsPackColorTo8888(red, green, blue, f4.a);
}