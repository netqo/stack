/**
 * @file native-lib.cpp
 * @brief JNI bridge exposing ProvablyFairCore to the Kotlin NativeGameEngine.
 *
 * This file is compiled exclusively by the Android NDK toolchain.
 * It is NOT included in the local desktop test build (no <jni.h> on host).
 *
 * Each exported function:
 * 1. Extracts C strings from JNI jstring arguments.
 * 2. Delegates to the corresponding ProvablyFairCore::calculate* method.
 * 3. Releases JNI string references.
 * 4. Catches C++ exceptions and translates them to Java exceptions.
 */

#ifdef __ANDROID__

#include "ProvablyFairCore.h"
#include <jni.h>
#include <string>

//  --- JNI Helpers ---

/**
 * @brief Throws a Java exception from native code.
 *
 * @param env       JNI environment pointer.
 * @param className Fully qualified Java exception class name.
 * @param message   Exception message.
 */
static void throwJavaException(JNIEnv *env, const char *className,
                               const char *message)
{
  jclass exClass = env->FindClass(className);
  if (exClass != nullptr)
    env->ThrowNew(exClass, message);
}

/**
 * @brief Executes a ProvablyFairCore call with full JNI lifecycle management.
 *
 * Extracts native C strings from jstring arguments, invokes the provided
 * core function, releases JNI references, and translates any C++ exceptions
 * into their Java equivalents.
 *
 * @tparam CoreFn Callable with signature std::string(std::string, std::string).
 * @param env        JNI environment pointer.
 * @param serverSeed JNI string for the server seed.
 * @param clientSeed JNI string for the client seed.
 * @param coreFn     Lambda invoking the target ProvablyFairCore method.
 * @return jstring   The result on success, or nullptr after throwing a Java
 * exception.
 */
template <typename CoreFn>
static jstring executeCoreSafe(JNIEnv *env, jstring serverSeed,
                               jstring clientSeed, CoreFn coreFn)
{
  const char *nativeServerSeed = env->GetStringUTFChars(serverSeed, nullptr);
  const char *nativeClientSeed = env->GetStringUTFChars(clientSeed, nullptr);
  try
  {
    std::string result =
        coreFn(std::string(nativeServerSeed), std::string(nativeClientSeed));
    env->ReleaseStringUTFChars(serverSeed, nativeServerSeed);
    env->ReleaseStringUTFChars(clientSeed, nativeClientSeed);
    return env->NewStringUTF(result.c_str());
  }
  catch (const std::invalid_argument &e)
  {
    env->ReleaseStringUTFChars(serverSeed, nativeServerSeed);
    env->ReleaseStringUTFChars(clientSeed, nativeClientSeed);
    throwJavaException(env, "java/lang/IllegalArgumentException", e.what());
    return nullptr;
  }
  catch (const std::exception &e)
  {
    env->ReleaseStringUTFChars(serverSeed, nativeServerSeed);
    env->ReleaseStringUTFChars(clientSeed, nativeClientSeed);
    throwJavaException(env, "java/lang/RuntimeException", e.what());
    return nullptr;
  }
}

//  --- JNI Exports ---

extern "C"
{

  /** @brief JNI bridge for ProvablyFairCore::calculateCoinflip. */
  JNIEXPORT jstring JNICALL
  Java_com_plainstudio_stackcasino_engine_NativeGameEngine_evaluateCoinflip(
      JNIEnv *env, jobject /* this */, jstring serverSeed, jstring clientSeed,
      jlong nonce)
  {
    return executeCoreSafe(env, serverSeed, clientSeed,
                           [nonce](const std::string &ss, const std::string &cs)
                           { return ProvablyFairCore::calculateCoinflip(ss, cs, nonce); });
  }

  /** @brief JNI bridge for ProvablyFairCore::calculateRoulette. */
  JNIEXPORT jstring JNICALL
  Java_com_plainstudio_stackcasino_engine_NativeGameEngine_evaluateRoulette(
      JNIEnv *env, jobject /* this */, jstring serverSeed, jstring clientSeed,
      jlong nonce)
  {
    return executeCoreSafe(env, serverSeed, clientSeed,
                           [nonce](const std::string &ss, const std::string &cs)
                           { return ProvablyFairCore::calculateRoulette(ss, cs, nonce); });
  }

  /** @brief JNI bridge for ProvablyFairCore::calculateCrashPoint. */
  JNIEXPORT jstring JNICALL
  Java_com_plainstudio_stackcasino_engine_NativeGameEngine_evaluateCrashPoint(
      JNIEnv *env, jobject /* this */, jstring serverSeed, jstring clientSeed,
      jlong nonce)
  {
    return executeCoreSafe(env, serverSeed, clientSeed,
                           [nonce](const std::string &ss, const std::string &cs)
                           { return ProvablyFairCore::calculateCrashPoint(ss, cs, nonce); });
  }

  /** @brief JNI bridge for ProvablyFairCore::calculateMines. */
  JNIEXPORT jstring JNICALL
  Java_com_plainstudio_stackcasino_engine_NativeGameEngine_evaluateMines(
      JNIEnv *env, jobject /* this */, jstring serverSeed, jstring clientSeed,
      jlong nonce, jint numMines)
  {
    return executeCoreSafe(
        env, serverSeed, clientSeed,
        [nonce, numMines](const std::string &ss, const std::string &cs)
        { return ProvablyFairCore::calculateMines(ss, cs, nonce, static_cast<int>(numMines)); });
  }

  /** @brief JNI bridge for ProvablyFairCore::calculateBlackjackDeck. */
  JNIEXPORT jstring JNICALL
  Java_com_plainstudio_stackcasino_engine_NativeGameEngine_evaluateBlackjackDeck(
      JNIEnv *env, jobject /* this */, jstring serverSeed, jstring clientSeed,
      jlong nonce)
  {
    return executeCoreSafe(env, serverSeed, clientSeed,
                           [nonce](const std::string &ss, const std::string &cs)
                           { return ProvablyFairCore::calculateBlackjackDeck(ss, cs, nonce); });
  }

} // extern "C"

#endif // __ANDROID__
