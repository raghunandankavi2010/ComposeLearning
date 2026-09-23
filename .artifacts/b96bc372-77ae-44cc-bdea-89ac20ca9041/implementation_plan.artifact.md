# Fix Image Decoding "unimplemented" Error

The app is experiencing `android.graphics.ImageDecoder$DecodeException: Failed to create image decoder with message 'unimplemented'` when loading WebP images from `picsum.photos` using Coil 3. This error typically occurs on certain Android versions (especially newer APIs or emulators) when the system's WebP decoding implementation fails or is missing.

## Proposed Changes

### [Fast Images Component]

#### [MODIFY] [ImageCdn.kt](file:///Users/raghunandan.k/StudioProjects/ComposeLearning/app/src/main/java/com/example/composelearning/fastimages/ImageCdn.kt)
- Update `variant` function to accept `downgradeSteps`.
- If `downgradeSteps > 0`, fallback to JPEG format (by removing the `.webp` extension) to bypass the broken WebP decoder.

#### [MODIFY] [VariantResolver.kt](file:///Users/raghunandan.k/StudioProjects/ComposeLearning/app/src/main/java/com/example/composelearning/fastimages/VariantResolver.kt)
- Pass the `downgradeSteps` parameter from `resolve` to `ImageCdn.variant`.

### [Image Loader Configuration]

#### [MODIFY] [ComposeLearningApplication.kt](file:///Users/raghunandan.k/StudioProjects/ComposeLearning/app/src/main/java/com/example/composelearning/ComposeLearningApplication.kt)
- Add `ImageDecoderDecoder.Factory()` to the `ImageLoader` components. This allows Coil to use the modern `ImageDecoder` API on Android P+, which may have better internal error handling or compatibility than the legacy `BitmapFactory` path for certain formats.

#### [MODIFY] [FeedImageEngine.kt](file:///Users/raghunandan.k/StudioProjects/ComposeLearning/app/src/main/java/com/example/composelearning/fastimages/FeedImageEngine.kt)
- Similar update to `ComposeLearningApplication.kt`: add `ImageDecoderDecoder.Factory()` to the feed-specific `ImageLoader`.

## Verification Plan

### Manual Verification
- Deploy the app to the device/emulator where the error was reported.
- Navigate to the **Fast Image Feed**.
- Verify that images load correctly. If a WebP image fails, it should now successfully fallback to a JPEG variant on the next attempt.
- Check logs for the absence of `IllegalStateException: BitmapFactory returned a null bitmap` after the first retry.
