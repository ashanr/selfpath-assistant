Place your TFLite or ONNX model file here.

Supported models:
  - gemma-2b-it-cpu-int4.bin  (Gemma 2B — recommended, ~1.5 GB)
  - qwen-0.5b-cpu-int4.bin    (Qwen 0.5B — lightweight, ~350 MB)

Download instructions:
  1. Visit https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android
  2. Follow the model download guide for Gemma or Qwen
  3. Place the .bin file in this directory
  4. Rebuild the project

The InferenceEngine will automatically detect and load the first compatible
model file it finds in this directory.
