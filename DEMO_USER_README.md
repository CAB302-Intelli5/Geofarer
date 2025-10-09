# Demo User for User Stats Page

A demo user has been created to showcase the user statistics page with meaningful data.

## Demo User Credentials

- **Email:** demo@geofarer.com
- **Password:** Demo123!

## Statistics Overview

The demo user has:
- **88 countries played** across 8 continents
- **53 countries mastered** (reached mastery level 3)
- **596 total correct guesses**
- **972 total matches** played (704 wins, 268 losses)
- **72.4% win rate**

### Breakdown by Region

- Africa: 6/13 mastered
- Antarctica: 1/1 mastered
- Asia: 8/18 mastered
- Europe: 8/17 mastered
- North America: 10/18 mastered
- Oceania: 6/10 mastered
- Seven seas (open ocean): 8/8 mastered
- South America: 6/12 mastered

## What's New

### Match-Based Progression Chart

The "Mastery Progression Over Time" chart has been updated to show progression by **match number** instead of by date. This provides:

1. **Match-by-Match Tracking**: See how your mastery progresses with each game played
2. **Win/Loss Visualization**: Each point on the chart represents a match outcome
3. **Proximity to Mastery**: The chart shows how many countries you've mastered at each point in your gaming journey
4. **Performance Insight**: Understand your learning curve and improvement over time

### How Mastery Works

- **Win without hints**: Gain progress toward mastery (+34 points per win)
- **Win with hints**: Small progress (+5 points)
- **Loss**: Lose progress (-25 points)
- **Mastery levels**: 0-99 (Level 0), 100-199 (Level 1), 200-299 (Level 2), 300+ (Level 3 - Mastered)

The demo user's progression shows realistic learning patterns:
- Initial struggles with losses and mixed results
- Gradual improvement with alternating wins and losses
- Final mastery with consecutive wins without hints

## Database Schema

A new `match_history` table has been added to track individual match results:

```sql
CREATE TABLE match_history (
    match_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    country_id VARCHAR NOT NULL,
    match_result VARCHAR NOT NULL CHECK(match_result IN ('win', 'loss')),
    used_hints BOOLEAN DEFAULT 0,
    played_at DATETIME NOT NULL,
    FOREIGN KEY(user_id) REFERENCES users(user_id),
    FOREIGN KEY(country_id) REFERENCES countries(country_id)
)
```

## Scripts

### add_demo_user.py
Creates the demo user and populates country mastery data across different regions.

### add_match_history.py
Generates realistic match history data for the demo user, showing progression from beginner to master for various countries.

## Testing

To test the new features:

1. Log in with the demo user credentials
2. Navigate to "My Stats" from the menu
3. View the updated "Mastery Progression by Match" chart
4. See the demo user's mastered countries list
5. Check the overall statistics and continent breakdown

## Future Enhancements

- Per-country match progression charts
- Compare your progression with other users
- Achievement badges for reaching milestones
- Detailed match history view with timeline
