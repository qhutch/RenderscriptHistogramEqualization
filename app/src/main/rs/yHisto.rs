#pragma version(1)
#pragma rs java_package_name(com.example.q.renderscriptexample)

//Array of 256 values
volatile int32_t *gOutarray;

void root(const uchar4 *v_in, const void *usrData, uint32_t x, uint32_t y) {
    //Convert input uchar4 to float4
    float4 f4 = rsUnpackColor8888(*v_in);

    //Get Y channel from float4
    float Y = f4.r;
    //Get color value between 0 and 255 (included)
    int32_t val = Y * 255;
    //Get array adress corresponding to color value
    volatile int32_t* addr=gOutarray+val;
    //Increment histogram for that value
    rsAtomicInc(addr);
}