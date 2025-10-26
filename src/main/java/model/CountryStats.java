package model;

public class CountryStats {
    private String countryName;
    private String countryCode;
    private double progress; // 0-100
    private boolean fullyUnlocked; // True if mastery_level >= 3
    private int masteryLevel; // 0-3 (represents consecutive hintless guesses)
    private int totalCorrectGuesses;

    public CountryStats(String countryName, String countryCode, double progress,
                        boolean fullyUnlocked, int masteryLevel) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.progress = progress;
        this.fullyUnlocked = fullyUnlocked;
        this.masteryLevel = masteryLevel;
        this.totalCorrectGuesses = 0;
    }

    public CountryStats(String countryName, String countryCode) {
        this(countryName, countryCode, 0.0, false, 0);
    }

    // Getters
    public String getCountryName() {
        return countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getProgress() {
        return progress;
    }

    public boolean isFullyUnlocked() {
        return fullyUnlocked;
    }

    public int getMasteryLevel() {
        return masteryLevel;
    }

    public int getTotalCorrectGuesses() {
        return totalCorrectGuesses;
    }

    // Setters
    public void setProgress(double progress) {
        this.progress = Math.max(0, Math.min(100, progress));
    }

    public void setFullyUnlocked(boolean fullyUnlocked) {
        this.fullyUnlocked = fullyUnlocked;
    }

    public void setMasteryLevel(int masteryLevel) {
        this.masteryLevel = Math.max(0, Math.min(3, masteryLevel));

        // Update progress and unlock status based on mastery level
        this.progress = Math.min(100.0, (this.masteryLevel * 100.0) / 3.0);
        this.fullyUnlocked = this.masteryLevel >= 3;
    }

    public void setTotalCorrectGuesses(int totalCorrectGuesses) {
        this.totalCorrectGuesses = totalCorrectGuesses;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): Mastery %d/3, %.1f%% - %s",
                countryName, countryCode, masteryLevel, progress,
                fullyUnlocked ? "UNLOCKED" : "LOCKED");
    }

    /**
     * Get a visual representation of mastery (stars or similar)
     */
    public String getMasteryStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < masteryLevel) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
}