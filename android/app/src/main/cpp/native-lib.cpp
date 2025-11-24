#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <android/log.h>

#define LOG_TAG "NativeCVGL"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT void JNICALL
Java_com_example_cvgl_MainActivity_nativeProcessFrame(
        JNIEnv* env,
        jobject /* this */,
        jlong matAddrInput,
        jlong matAddrOutput) {
    
    cv::Mat& matInput = *(cv::Mat*)matAddrInput;
    cv::Mat& matOutput = *(cv::Mat*)matAddrOutput;

    if (matInput.empty()) return;

    // Input is NV21 or YUV_420_888. 
    // For simplicity, let's assume we get a grayscale image or convert it.
    // If using CameraX ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888, the Y plane is grayscale.
    
    // Apply Canny Edge Detection
    // 1. Blur to reduce noise
    cv::Mat blurred;
    cv::GaussianBlur(matInput, blurred, cv::Size(5, 5), 1.5);

    // 2. Canny
    cv::Canny(blurred, matOutput, 50, 150);
    
    // 3. Invert for better visibility (optional: white edges on black background is standard)
    // cv::bitwise_not(matOutput, matOutput);
}

}
