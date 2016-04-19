#pragma version(1)
#pragma rs java_package_name(com.example.q.renderscriptexample)

void root(const uchar4 *v_in, uchar4 *v_out, const void *usrData, uint32_t x, uint32_t y) {
    //Convert input uchar4 to float4
    float4 f4 = rsUnpackColor8888(*v_in);

    //Get each channel from the pixel
    float red = f4.r;
    float green = f4.g;
    float blue = f4.b;
    float alpha = f4.a;

    //Get YUV channels values
    float Y = 0.299f * red + 0.587f * green + 0.114f * blue;
    float U = (0.492f * (blue - Y));
    float V = (0.877f * (red - Y));
    //We keep the U value between 0 and 1 instead of -Umax and +Umax
    U = (U+1)/2;
    //We keep the V value between 0 and 1 instead of -Vmax and +Vmax
    V = (V+1)/2;

    //Put the values in the output uchar4, note that we keep the alpha value
    *v_out = rsPackColorTo8888(Y, U, V, alpha);
}