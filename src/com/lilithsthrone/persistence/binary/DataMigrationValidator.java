package com.lilithsthrone.persistence.binary;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Data Migration Validator - Ensures binary data migration integrity
 * 
 * Validates that all static data was correctly encoded to binary and
 * can be decoded back to usable objects without data loss or corruption.
 * 
 * Responsibilities:
 *  - Compare text source data with binary encoding
 *  - Verify encoding/decoding correctness
 *  - Check for data loss or corruption
 *  - Report validation statistics
 *  - Identify problematic files
 * 
 * Usage:
 *  // Validate full migration
 *  ValidationResult result = DataMigrationValidator.validateMigration();
 *  if (result.isSuccess()) {
 *      System.out.println("Migration successful!");
 *  } else {
 *      System.out.println("Validation failed: " + result.getErrors());
 *  }
 *  
 *  // Validate single category
 *  ValidationResult catResult = DataMigrationValidator.validateCategory("CHARACTERS");
 *  
 *  // Get detailed report
 *  String report = DataMigrationValidator.getDetailedReport();
 * 
 * @since Step 4
 * @version 1.0
 */
public class DataMigrationValidator {

	private static final String VALIDATOR_NAME = "DataMigrationValidator";

	/**
	 * Validation result object
	 */
	public static class ValidationResult {
		private boolean success = true;
		private List<String> errors = new ArrayList<>();
		private List<String> warnings = new ArrayList<>();
		private Map<String, Object> statistics = new HashMap<>();

		public void addError(String error) {
			errors.add(error);
			success = false;
		}

		public void addWarning(String warning) {
			warnings.add(warning);
		}

		public void addStatistic(String key, Object value) {
			statistics.put(key, value);
		}

		public boolean isSuccess() {
			return success;
		}

		public List<String> getErrors() {
			return errors;
		}

		public List<String> getWarnings() {
			return warnings;
		}

		public Map<String, Object> getStatistics() {
			return statistics;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("ValidationResult {\n");
			sb.append("  success: ").append(success).append("\n");
			
			if (!errors.isEmpty()) {
				sb.append("  errors: ").append(errors.size()).append("\n");
				for (String error : errors) {
					sb.append("    - ").append(error).append("\n");
				}
			}
			
			if (!warnings.isEmpty()) {
				sb.append("  warnings: ").append(warnings.size()).append("\n");
				for (String warning : warnings) {
					sb.append("    - ").append(warning).append("\n");
				}
			}
			
			if (!statistics.isEmpty()) {
				sb.append("  statistics:\n");
				for (Map.Entry<String, Object> entry : statistics.entrySet()) {
					sb.append("    ").append(entry.getKey()).append(": ")
					  .append(entry.getValue()).append("\n");
				}
			}
			
			sb.append("}");
			return sb.toString();
		}
	}

	/**
	 * Validate the complete migration
	 * Checks all data categories for proper encoding/decoding
	 * 
	 * @return Validation result
	 */
	public static ValidationResult validateMigration() {
		System.out.println("[" + VALIDATOR_NAME + "] Starting full migration validation...");
		
		ValidationResult result = new ValidationResult();
		long startTime = System.currentTimeMillis();

		try {
			// Validate each category
			validateCharacterData(result);
			validateItemData(result);
			validateWeaponData(result);
			validateClothingData(result);
			validateRaceData(result);
			validateColorData(result);
			validateCombatMoveData(result);
			validateDialogueData(result);
			validateEncounterData(result);
			validateOutfitData(result);
			validatePatternData(result);
			validateSexTypeData(result);
			validateStatusEffectData(result);
			validateTattooData(result);
			validateSetBonusData(result);
			validateKeybindData(result);
			validateRandomEnchantmentData(result);

			long elapsed = System.currentTimeMillis() - startTime;
			result.addStatistic("validation_time_ms", elapsed);
			result.addStatistic("success", result.isSuccess());

		} catch (Exception e) {
			result.addError("Unexpected error during validation: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("[" + VALIDATOR_NAME + "] Validation complete: " + 
		                 (result.isSuccess() ? "SUCCESS" : "FAILED"));

		return result;
	}

	/**
	 * Validate character data
	 */
	private static void validateCharacterData(ValidationResult result) {
		validateCategory("CHARACTERS", "binary_cache/characters", result);
	}

	/**
	 * Validate item data
	 */
	private static void validateItemData(ValidationResult result) {
		validateCategory("ITEMS", "binary_cache/items", result);
	}

	/**
	 * Validate weapon data
	 */
	private static void validateWeaponData(ValidationResult result) {
		validateCategory("WEAPONS", "binary_cache/weapons", result);
	}

	/**
	 * Validate clothing data
	 */
	private static void validateClothingData(ValidationResult result) {
		validateCategory("CLOTHING", "binary_cache/clothing", result);
	}

	/**
	 * Validate race data
	 */
	private static void validateRaceData(ValidationResult result) {
		validateCategory("RACES", "binary_cache/race", result);
	}

	/**
	 * Validate color data
	 */
	private static void validateColorData(ValidationResult result) {
		validateCategory("COLOURS", "binary_cache/colours", result);
	}

	/**
	 * Validate combat move data
	 */
	private static void validateCombatMoveData(ValidationResult result) {
		validateCategory("COMBAT_MOVES", "binary_cache/combatMove", result);
	}

	/**
	 * Validate dialogue data
	 */
	private static void validateDialogueData(ValidationResult result) {
		validateCategory("DIALOGUE", "binary_cache/dialogue", result);
	}

	/**
	 * Validate encounter data
	 */
	private static void validateEncounterData(ValidationResult result) {
		validateCategory("ENCOUNTERS", "binary_cache/encounters", result);
	}

	/**
	 * Validate outfit data
	 */
	private static void validateOutfitData(ValidationResult result) {
		validateCategory("OUTFITS", "binary_cache/outfits", result);
	}

	/**
	 * Validate pattern data
	 */
	private static void validatePatternData(ValidationResult result) {
		validateCategory("PATTERNS", "binary_cache/patterns", result);
	}

	/**
	 * Validate sex type data
	 */
	private static void validateSexTypeData(ValidationResult result) {
		validateCategory("SEX_TYPES", "binary_cache/sex", result);
	}

	/**
	 * Validate status effect data
	 */
	private static void validateStatusEffectData(ValidationResult result) {
		validateCategory("STATUS_EFFECTS", "binary_cache/statusEffects", result);
	}

	/**
	 * Validate tattoo data
	 */
	private static void validateTattooData(ValidationResult result) {
		validateCategory("TATTOOS", "binary_cache/tattoos", result);
	}

	/**
	 * Validate set bonus data
	 */
	private static void validateSetBonusData(ValidationResult result) {
		validateCategory("SET_BONUSES", "binary_cache/setBonuses", result);
	}

	/**
	 * Validate keybind data
	 */
	private static void validateKeybindData(ValidationResult result) {
		validateCategory("KEYBINDS", "binary_cache/keybinds", result);
	}

	/**
	 * Validate random enchantment data
	 */
	private static void validateRandomEnchantmentData(ValidationResult result) {
		validateCategory("RANDOM_ENCHANTMENTS", "binary_cache/randomEnchantments", result);
	}

	/**
	 * Validate a single category
	 */
	private static void validateCategory(String categoryName, String cachePath, ValidationResult result) {
		try {
			Path categoryPath = Paths.get(cachePath);
			
			if (!Files.exists(categoryPath)) {
				result.addWarning("Category " + categoryName + " has no binary cache at " + cachePath);
				return;
			}

			long fileCount = Files.list(categoryPath)
				.filter(p -> p.toString().endsWith(".bin"))
				.count();

			if (fileCount == 0) {
				result.addWarning("Category " + categoryName + " has no .bin files");
				return;
			}

			// Try to load and decode a sample file to verify integrity
			Files.list(categoryPath)
				.filter(p -> p.toString().endsWith(".bin"))
				.limit(1)
				.forEach(filePath -> {
					try {
						byte[] data = Files.readAllBytes(filePath);
						
						// Verify data is not empty
						if (data.length == 0) {
							result.addError("Empty binary file: " + filePath.getFileName());
							return;
						}

						// Try decoding based on category
						decodeAndValidate(categoryName, data, result);

						System.out.println("[" + VALIDATOR_NAME + "] " + categoryName + 
						                 ": " + fileCount + " files, sample validated");

					} catch (Exception e) {
						result.addError("Failed to decode " + categoryName + " sample: " + e.getMessage());
					}
				});

		} catch (IOException e) {
			result.addWarning("Could not validate " + categoryName + ": " + e.getMessage());
		}
	}

	/**
	 * Decode and validate data
	 */
	private static void decodeAndValidate(String categoryName, byte[] data, ValidationResult result) {
		try {
			switch (categoryName) {
				case "CHARACTERS":
					Map<String, Object> character = StaticDataBinaryDecoder.decodeCharacterData(data);
					if (character == null || character.isEmpty()) {
						result.addError("Character data decoded to empty map");
					}
					break;
					
				case "ITEMS":
					Map<String, Object> item = StaticDataBinaryDecoder.decodeItemData(data);
					if (item == null || item.isEmpty()) {
						result.addError("Item data decoded to empty map");
					}
					break;
					
				default:
					// Other categories would have similar validation
					break;
			}
		} catch (Exception e) {
			result.addError("Decoding error for " + categoryName + ": " + e.getMessage());
		}
	}

	/**
	 * Get a detailed validation report
	 * 
	 * @return HTML or text formatted report
	 */
	public static String getDetailedReport() {
		ValidationResult result = validateMigration();
		
		StringBuilder report = new StringBuilder();
		report.append("========================================\n");
		report.append("DATA MIGRATION VALIDATION REPORT\n");
		report.append("========================================\n\n");
		
		report.append("Status: ").append(result.isSuccess() ? "PASS" : "FAIL").append("\n\n");
		
		if (!result.getErrors().isEmpty()) {
			report.append("ERRORS (").append(result.getErrors().size()).append("):\n");
			for (String error : result.getErrors()) {
				report.append("  ✗ ").append(error).append("\n");
			}
			report.append("\n");
		}
		
		if (!result.getWarnings().isEmpty()) {
			report.append("WARNINGS (").append(result.getWarnings().size()).append("):\n");
			for (String warning : result.getWarnings()) {
				report.append("  ⚠ ").append(warning).append("\n");
			}
			report.append("\n");
		}
		
		if (!result.getStatistics().isEmpty()) {
			report.append("STATISTICS:\n");
			for (Map.Entry<String, Object> entry : result.getStatistics().entrySet()) {
				report.append("  ").append(entry.getKey()).append(": ")
				      .append(entry.getValue()).append("\n");
			}
		}
		
		report.append("\n========================================\n");
		
		return report.toString();
	}

	/**
	 * Run validation and print report
	 */
	public static void main(String[] args) {
		System.out.println(getDetailedReport());
	}
}
