#pragma version(1)
#pragma rs java_package_name(com.example.q.renderscriptexample)

//Array of 256 values to remap Y channel
volatile float *remapArray;

void root(const uchar4 *v_in, uchar4 *v_out, const void *usrData, uint32_t x, uint32_t y) {
    //Convert input uchar4 to float4
    float4 f4 = rsUnpackColor8888(*v_in);

    //Get Y channel from float4
    float Y = f4.r;
    //Get color value between 0 and 255 (included)
    int32_t val = Y * 255;
    //Get new Y value from the map at the old Y value position
    Y = remapArray[val];

     //Put the values in the output uchar4
    *v_out = rsPackColorTo8888(Y, f4.g, f4.b, f4.a);
}