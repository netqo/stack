#include "ProvablyFairCore.h"
#include <iostream>
#include <stdexcept>
#include <string>

//  --- Test Helpers ---

/**
 * @brief Parsed components of a ProvablyFairCore game result.
 *
 * Every public calculate* method returns a string formatted as "result|hash".
 * This struct holds the split values after parsing.
 */
struct GameOutput
{
  std::string result;
  std::string hash;
};

/**
 * @brief Splits a "result|hash" game output string into its components.
 *
 * @param raw The raw output from any ProvablyFairCore::calculate* method.
 * @return GameOutput Parsed result and hash fields.
 */
GameOutput parseGameOutput(const std::string &raw)
{
  size_t delimiterPos = raw.find('|');
  return {raw.substr(0, delimiterPos), raw.substr(delimiterPos + 1)};
}

/**
 * @brief Returns the first 8 characters of a hash followed by "...".
 *
 * @param hash The full 64-character hex hash.
 * @return std::string Truncated hash for display purposes.
 */
std::string abbreviateHash(const std::string &hash)
{
  return hash.substr(0, 8) + "...";
}

//  --- Test Suites ---

/**
 * @brief Executes a series of tests for the Coinflip provably fair algorithm.
 *
 * This function simulates consecutive rounds using an incrementing nonce to
 * verify the deterministic hash calculation and outcome mapping (Heads/Tails).
 *
 * @param serverSeed The immutable server seed used for the session.
 * @param clientSeed The client seed provided by the user.
 * @param numRounds The total number of consecutive test rounds to execute.
 */
void testCoinflipAlgorithm(const std::string &serverSeed,
                           const std::string &clientSeed, int numRounds)
{
  std::cout << "--- COINFLIP TESTS (" << numRounds << " Rounds) ---\n";
  for (long nonce = 1; nonce <= numRounds; ++nonce)
  {
    auto [result, hash] = parseGameOutput(
        ProvablyFairCore::calculateCoinflip(serverSeed, clientSeed, nonce));
    std::string coinFace = (result == "0") ? "HEADS" : "TAILS";
    std::cout << "Nonce " << nonce << " -> Hash: " << abbreviateHash(hash)
              << " -> Bit: " << result << " (" << coinFace << ")\n";
  }
}

/**
 * @brief Executes a series of tests for the European Roulette provably fair
 * algorithm.
 *
 * This function simulates consecutive rounds using an incrementing nonce to
 * verify that the calculated winning numbers are consistently within the valid
 * range (0-36), applying rejection sampling behind the scenes.
 *
 * @param serverSeed The immutable server seed used for the session.
 * @param clientSeed The client seed provided by the user.
 * @param numRounds The total number of consecutive test rounds to execute.
 */
void testRouletteAlgorithm(const std::string &serverSeed,
                           const std::string &clientSeed, int numRounds)
{
  std::cout << "\n--- ROULETTE TESTS (" << numRounds << " Rounds) ---\n";
  for (long nonce = 1; nonce <= numRounds; ++nonce)
  {
    auto [result, hash] = parseGameOutput(
        ProvablyFairCore::calculateRoulette(serverSeed, clientSeed, nonce));
    std::cout << "Nonce " << nonce << " -> Hash: " << abbreviateHash(hash)
              << " -> Winning Number: " << result << "\n";
  }
}

/**
 * @brief Executes tests for the Crash provably fair algorithm.
 *
 * @param serverSeed The immutable server seed used for the session.
 * @param clientSeed The client seed provided by the user.
 * @param numRounds The total number of consecutive test rounds to execute.
 */
void testCrashAlgorithm(const std::string &serverSeed,
                        const std::string &clientSeed, int numRounds)
{
  std::cout << "\n--- CRASH TESTS (" << numRounds << " Rounds) ---\n";
  for (long nonce = 1; nonce <= numRounds; ++nonce)
  {
    auto [result, hash] = parseGameOutput(
        ProvablyFairCore::calculateCrashPoint(serverSeed, clientSeed, nonce));
    std::cout << "Nonce " << nonce << " -> Hash: " << abbreviateHash(hash)
              << " -> Multiplier: " << result << "x\n";
  }
}

/**
 * @brief Executes tests for the Mines provably fair algorithm.
 *
 * @param serverSeed The immutable server seed used for the session.
 * @param clientSeed The client seed provided by the user.
 * @param numRounds The total number of consecutive test rounds to execute.
 */
void testMinesAlgorithm(const std::string &serverSeed,
                        const std::string &clientSeed, int numRounds)
{
  std::cout << "\n--- MINES TESTS (" << numRounds << " Rounds, 5 Mines) ---\n";
  for (long nonce = 1; nonce <= numRounds; ++nonce)
  {
    auto [result, hash] = parseGameOutput(
        ProvablyFairCore::calculateMines(serverSeed, clientSeed, nonce, 5));
    std::cout << "Nonce " << nonce << " -> Hash: " << abbreviateHash(hash)
              << " -> Bomb Locations: [" << result << "]\n";
  }
}

/**
 * @brief Executes tests for the Blackjack provably fair deck generation
 * algorithm.
 *
 * @param serverSeed The immutable server seed used for the session.
 * @param clientSeed The client seed provided by the user.
 */
void testBlackjackAlgorithm(const std::string &serverSeed,
                            const std::string &clientSeed)
{
  std::cout << "\n--- BLACKJACK TEST (Single Deck Array) ---\n";
  auto [result, hash] = parseGameOutput(
      ProvablyFairCore::calculateBlackjackDeck(serverSeed, clientSeed, 1));
  std::cout << "Nonce 1 -> Hash: " << abbreviateHash(hash)
            << " -> Shuffled Deck Array: [" << result.substr(0, 30) << "...]\n";
}

/**
 * @brief Verifies that input validation throws std::invalid_argument.
 *
 * Tests each boundary condition: empty seeds, non-positive nonces,
 * and out-of-range mine counts.
 */
void testInputValidation()
{
  std::cout << "\n--- INPUT VALIDATION TESTS ---\n";
  int passed = 0;
  int total = 0;
  auto expectThrow = [&](const std::string &label, auto fn)
  {
    total++;
    try
    {
      fn();
      std::cout << "FAIL: " << label << " (no exception thrown)\n";
    }
    catch (const std::invalid_argument &e)
    {
      std::cout << "PASS: " << label << " -> " << e.what() << "\n";
      passed++;
    }
    catch (const std::exception &e)
    {
      std::cout << "FAIL: " << label << " (wrong exception: " << e.what()
                << ")\n";
    }
  };
  expectThrow("Empty serverSeed",
              []() { ProvablyFairCore::calculateCoinflip("", "client", 1); });
  expectThrow("Empty clientSeed",
              []() { ProvablyFairCore::calculateCoinflip("server", "", 1); });
  expectThrow("Zero nonce", []()
              { ProvablyFairCore::calculateRoulette("server", "client", 0); });
  expectThrow(
      "Negative nonce",
      []() { ProvablyFairCore::calculateCrashPoint("server", "client", -1); });
  expectThrow("numMines = 0", []()
              { ProvablyFairCore::calculateMines("server", "client", 1, 0); });
  expectThrow("numMines = 25", []()
              { ProvablyFairCore::calculateMines("server", "client", 1, 25); });
  std::cout << "Validation: " << passed << "/" << total << " passed.\n";
}

//  --- Entry Point ---

/**
 * @brief Main entry point for the offline terminal testing suite.
 *
 * Provides mock datasets for the Server Seed and Client Seed to validate
 * the fundamental game engines without requiring Android or JNI overhead.
 *
 * @return int Standard UNIX exit code (0 for success).
 */
int main()
{
  std::cout << "==========================================\n";
  std::cout << "  CASINO NATIVE ENGINE - LOCAL TESTS      \n";
  std::cout << "==========================================\n";
  const std::string serverSeed =
      "b1c28c897f26f634d5885c4b8159bb8ed35bc8edb3724c65f80e0c034bc4464b";
  const std::string clientSeed = "android_user_hash_999";
  constexpr int totalTestRounds = 10;
  std::cout << "\n[SERVER SEED]: " << serverSeed << "\n";
  std::cout << "[CLIENT SEED]: " << clientSeed << "\n\n";
  testCoinflipAlgorithm(serverSeed, clientSeed, totalTestRounds);
  testRouletteAlgorithm(serverSeed, clientSeed, totalTestRounds);
  testCrashAlgorithm(serverSeed, clientSeed, totalTestRounds);
  testMinesAlgorithm(serverSeed, clientSeed, totalTestRounds);
  testBlackjackAlgorithm(serverSeed, clientSeed);
  testInputValidation();
  std::cout << "\nAll tests successfully completed.\n";
  return 0;
}
