#include "ProvablyFairCore.h"
#include "Sha256.h"
#include <algorithm>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <iomanip>
#include <numeric>
#include <sstream>
#include <vector>

//  --- Private Helpers ---

/**
 * @brief Generates an HMAC-SHA256 string representation.
 *
 * Delegates to the self-contained casino::hmacSha256Hex implementation
 * (Sha256.h). The previous OpenSSL-based path was replaced to remove the
 * external dependency: the engine now builds identically on desktop (host)
 * and Android (NDK) without needing libssl/libcrypto.
 *
 * @param key The secret key (Server Seed).
 * @param msg The target message to be hashed (Client Seed + Nonce).
 * @return std::string Hexadecimal lowercase output of the SHA-256 digest.
 */
std::string ProvablyFairCore::generateHMAC_SHA256(const std::string &key,
                                                  const std::string &msg)
{
  return casino::hmacSha256Hex(key, msg);
}

/**
 * @brief Builds the standard HMAC message from client seed and nonce.
 *
 * @param clientSeed User-provided seed string.
 * @param nonce      Round counter.
 * @return std::string Formatted as "clientSeed:nonce".
 */
std::string ProvablyFairCore::buildMessage(const std::string &clientSeed,
                                           long nonce)
{
  return clientSeed + ":" + std::to_string(nonce);
}

/**
 * @brief Parses a hexadecimal substring into a 32-bit unsigned integer.
 *
 * @param hex    Full hex string to read from.
 * @param offset Starting character position.
 * @param len    Number of hex characters to parse.
 * @return uint32_t The parsed unsigned value.
 */
uint32_t ProvablyFairCore::parseHex32(const std::string &hex, size_t offset,
                                      size_t len)
{
  std::string chunk = hex.substr(offset, len);
  return static_cast<uint32_t>(std::strtoul(chunk.c_str(), nullptr, 16));
}

/**
 * @brief Parses a hexadecimal substring into a 64-bit unsigned integer.
 *
 * @param hex    Full hex string to read from.
 * @param offset Starting character position.
 * @param len    Number of hex characters to parse.
 * @return uint64_t The parsed unsigned value.
 */
uint64_t ProvablyFairCore::parseHex64(const std::string &hex, size_t offset,
                                      size_t len)
{
  std::string chunk = hex.substr(offset, len);
  return std::strtoull(chunk.c_str(), nullptr, 16);
}

/**
 * @brief Joins a vector of integers into a comma-separated string.
 *
 * @param values The integers to serialize.
 * @return std::string Comma-delimited representation (e.g. "1,20,14,9,23").
 */
std::string ProvablyFairCore::joinCsv(const std::vector<int> &values)
{
  std::string result;
  for (size_t i = 0; i < values.size(); ++i)
  {
    if (i > 0)
      result += ",";
    result += std::to_string(values[i]);
  }
  return result;
}

/**
 * @brief Performs unbiased random selection via rejection sampling.
 *
 * Consumes 32-bit blocks from the hash stream, rehashing when exhausted.
 * Rejects values that would cause modulo bias for the given range.
 *
 * @param serverSeed    The server seed for potential rehashing.
 * @param currentHashHex The current hash (mutated on exhaustion).
 * @param chunkCounter   Tracks consumption position (mutated on read).
 * @param range          The exclusive upper bound for the result [0, range).
 * @return int An unbiased random index within [0, range).
 */
int ProvablyFairCore::selectUnbiasedIndex(const std::string &serverSeed,
                                          std::string &currentHashHex,
                                          int &chunkCounter, int range)
{
  while (true)
  {
    size_t requiredOffset = static_cast<size_t>(
        chunkCounter * HEX_CHARS_PER_32BIT_BLOCK + HEX_CHARS_PER_32BIT_BLOCK);
    if (requiredOffset > currentHashHex.length())
    {
      currentHashHex = generateHMAC_SHA256(serverSeed, currentHashHex);
      chunkCounter = 0;
    }
    uint32_t val = parseHex32(
        currentHashHex,
        static_cast<size_t>(chunkCounter * HEX_CHARS_PER_32BIT_BLOCK),
        HEX_CHARS_PER_32BIT_BLOCK);
    chunkCounter++;
    uint32_t maxUnbiased = MAX_UINT32 - (MAX_UINT32 % range);
    if (val <= maxUnbiased)
      return static_cast<int>(val % range);
  }
}

/**
 * @brief Validates common seed and nonce parameters.
 *
 * Called at the entry of every public calculate* method to guard against
 * invalid inputs before any cryptographic work is performed.
 *
 * @param serverSeed Must be non-empty.
 * @param clientSeed Must be non-empty.
 * @param nonce      Must be positive (> 0).
 * @throws std::invalid_argument If any parameter is invalid.
 */
void ProvablyFairCore::validateSeeds(const std::string &serverSeed,
                                     const std::string &clientSeed, long nonce)
{
  if (serverSeed.empty())
    throw std::invalid_argument("serverSeed must not be empty");
  if (clientSeed.empty())
    throw std::invalid_argument("clientSeed must not be empty");
  if (nonce <= 0)
    throw std::invalid_argument("nonce must be positive");
}

//  --- Public Game Algorithms ---

/**
 * @brief Evaluates a Coinflip round using Provably Fair logic.
 *
 * @param serverSeed Immutable cryptographic string generated by the server.
 * @param clientSeed User-provided string adding external entropy.
 * @param nonce      Incremental round counter preventing replay attacks.
 * @return std::string Resulting bit ("0" or "1") followed by a '|' and the
 * final hash.
 */
std::string ProvablyFairCore::calculateCoinflip(const std::string &serverSeed,
                                                const std::string &clientSeed,
                                                long nonce)
{
  validateSeeds(serverSeed, clientSeed, nonce);
  std::string hashHex =
      generateHMAC_SHA256(serverSeed, buildMessage(clientSeed, nonce));
  uint32_t firstByte = parseHex32(hashHex, 0, 2);
  int result = firstByte & 1;
  return std::to_string(result) + "|" + hashHex;
}

/**
 * @brief Evaluates a European Roulette round using Provably Fair logic.
 *
 * Employs rejection sampling mathematically ensuring that the extraction
 * of the numbers 0 through 36 holds zero modulo bias.
 *
 * @param serverSeed Immutable cryptographic string generated by the server.
 * @param clientSeed User-provided string adding external entropy.
 * @param nonce      Incremental round counter preventing replay attacks.
 * @return std::string Winning pocket number followed by a '|' and the final
 * hash.
 */
std::string ProvablyFairCore::calculateRoulette(const std::string &serverSeed,
                                                const std::string &clientSeed,
                                                long nonce)
{
  validateSeeds(serverSeed, clientSeed, nonce);
  std::string currentHashHex =
      generateHMAC_SHA256(serverSeed, buildMessage(clientSeed, nonce));
  std::string originalHashHex = currentHashHex;
  // 65536 (2^16) % 37 = 9, so values >= 65536 - 9 = 65527 are biased
  static constexpr uint32_t ROULETTE_REJECTION_THRESHOLD =
      (1U << 16) - ((1U << 16) % ROULETTE_OUTCOMES);
  int result = -1;
  int safetyCounter = 0;
  while (result == -1 && safetyCounter < REJECTION_SAFETY_LIMIT)
  {
    for (size_t i = 0; i + ROULETTE_BLOCK_HEX_CHARS <= currentHashHex.length();
         i += ROULETTE_BLOCK_HEX_CHARS)
    {
      uint32_t val = parseHex32(currentHashHex, i, ROULETTE_BLOCK_HEX_CHARS);
      if (val < ROULETTE_REJECTION_THRESHOLD)
      {
        result = static_cast<int>(val % ROULETTE_OUTCOMES);
        break;
      }
    }
    // If all blocks in the current hash were rejected, rehash and retry
    if (result == -1)
    {
      currentHashHex = generateHMAC_SHA256(serverSeed, currentHashHex);
      safetyCounter++;
    }
  }
  // Extremely unlikely fallback to prevent absolute lock-ups
  if (result == -1)
    result = 0;
  return std::to_string(result) + "|" + originalHashHex;
}

/**
 * @brief Evaluates the Crash game multiplier using Provably Fair logic.
 *
 * Parses 52 bits of the deterministic hash into an exponential curve
 * simulating a 1% House Edge for instant busts.
 *
 * @param serverSeed Immutable cryptographic string generated by the server.
 * @param clientSeed User-provided string adding external entropy.
 * @param nonce      Incremental round counter preventing replay attacks.
 * @return std::string Crash multiplier float followed by a '|' and the final
 * hash.
 */
std::string ProvablyFairCore::calculateCrashPoint(const std::string &serverSeed,
                                                  const std::string &clientSeed,
                                                  long nonce)
{
  validateSeeds(serverSeed, clientSeed, nonce);
  std::string hashHex =
      generateHMAC_SHA256(serverSeed, buildMessage(clientSeed, nonce));
  uint64_t h = parseHex64(hashHex, 0, CRASH_HASH_HEX_DIGITS);
  double crashPoint = 1.0;
  if (h % 100 != 0)
  { // 1% House Edge for instant crash at 1.00
    crashPoint = std::max(1.0, std::floor((100.0 * CRASH_ENTROPY_SPACE - h) /
                                          (CRASH_ENTROPY_SPACE - h)) /
                                   100.0);
  }
  std::stringstream result;
  result << std::fixed << std::setprecision(2) << crashPoint;
  return result.str() + "|" + hashHex;
}

/**
 * @brief Calculates a non-repeating grid array of hidden mines.
 *
 * Limits Fisher-Yates array shuffling strictly to the requested number of mines
 * leveraging dynamic modulus limits to ensure rejection sampling fairness.
 *
 * @param serverSeed Immutable cryptographic string generated by the server.
 * @param clientSeed User-provided string adding external entropy.
 * @param nonce      Incremental round counter preventing replay attacks.
 * @param numMines   The number of mines to compute into the 25-cell grid.
 * @return std::string Comma-separated mine positions followed by a '|' and the
 * hash.
 */
std::string ProvablyFairCore::calculateMines(const std::string &serverSeed,
                                             const std::string &clientSeed,
                                             long nonce, int numMines)
{
  validateSeeds(serverSeed, clientSeed, nonce);
  if (numMines < MIN_MINES || numMines > MAX_MINES)
    throw std::invalid_argument("numMines must be between 1 and 24");
  std::string currentHashHex =
      generateHMAC_SHA256(serverSeed, buildMessage(clientSeed, nonce));
  std::string originalHashHex = currentHashHex;
  std::vector<int> grid(MINES_GRID_SIZE);
  std::iota(grid.begin(), grid.end(), 0);
  std::vector<int> mines;
  int chunkCounter = 0;
  for (int i = 0; i < numMines; ++i)
  {
    int currentLength = MINES_GRID_SIZE - i;
    int pos = selectUnbiasedIndex(serverSeed, currentHashHex, chunkCounter,
                                  currentLength);
    mines.push_back(grid[pos]);
    grid.erase(grid.begin() + pos);
  }
  return joinCsv(mines) + "|" + originalHashHex;
}

/**
 * @brief Pre-computes an entire deck standard formulation for Blackjack.
 *
 * Applies Fisher-Yates algorithm using dynamic re-hashing to iteratively
 * complete 52 random swaps. Bypasses C++ JNI session-state handling
 * limitations.
 *
 * @param serverSeed Immutable cryptographic string generated by the server.
 * @param clientSeed User-provided string adding external entropy.
 * @param nonce      Incremental round counter preventing replay attacks.
 * @return std::string A fully shuffled 52-card comma-separated array followed
 * by '|' and hash.
 */
std::string ProvablyFairCore::calculateBlackjackDeck(
    const std::string &serverSeed, const std::string &clientSeed, long nonce)
{
  validateSeeds(serverSeed, clientSeed, nonce);
  std::string currentHashHex =
      generateHMAC_SHA256(serverSeed, buildMessage(clientSeed, nonce));
  std::string originalHashHex = currentHashHex;
  std::vector<int> deck(BLACKJACK_DECK_SIZE);
  std::iota(deck.begin(), deck.end(), 0);
  int chunkCounter = 0;
  for (int i = BLACKJACK_DECK_SIZE - 1; i >= 1; --i)
  {
    int pos =
        selectUnbiasedIndex(serverSeed, currentHashHex, chunkCounter, i + 1);
    std::swap(deck[i], deck[pos]);
  }
  return joinCsv(deck) + "|" + originalHashHex;
}
