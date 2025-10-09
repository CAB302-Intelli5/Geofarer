import sqlite3
from datetime import datetime, timedelta
import random

def main():
    conn = sqlite3.connect('src/main/resources/db/geofarer.db')
    cursor = conn.cursor()
    
    # Create match_history table if it doesn't exist
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS match_history (
            match_id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            country_id VARCHAR NOT NULL,
            match_result VARCHAR NOT NULL CHECK(match_result IN ('win', 'loss')),
            used_hints BOOLEAN DEFAULT 0,
            played_at DATETIME NOT NULL,
            FOREIGN KEY(user_id) REFERENCES users(user_id),
            FOREIGN KEY(country_id) REFERENCES countries(country_id)
        )
    """)
    
    print("✓ match_history table created/verified")
    
    # Get demo user
    cursor.execute("SELECT user_id FROM users WHERE email = ?", ("demo@geofarer.com",))
    demo_user = cursor.fetchone()
    
    if not demo_user:
        print("Demo user not found. Please run add_demo_user.py first.")
        return
    
    demo_user_id = demo_user[0]
    
    # Clear existing match history for demo user
    cursor.execute("DELETE FROM match_history WHERE user_id = ?", (demo_user_id,))
    
    # Get all countries the demo user has played
    cursor.execute("""
        SELECT country_id, mastery_level, correct_guesses, last_played
        FROM country_mastery
        WHERE user_id = ?
        ORDER BY last_played
    """, (demo_user_id,))
    
    countries_played = cursor.fetchall()
    
    print(f"\nGenerating match history for {len(countries_played)} countries...")
    
    total_matches = 0
    
    # For each country, generate match history based on mastery level
    for country_id, mastery_level, correct_guesses, last_played_str in countries_played:
        # Parse the last_played date
        if last_played_str:
            try:
                # Try to parse with microseconds
                last_played = datetime.strptime(last_played_str.split('.')[0], '%Y-%m-%d %H:%M:%S')
            except:
                try:
                    last_played = datetime.strptime(last_played_str, '%Y-%m-%d')
                except:
                    last_played = datetime.now()
        else:
            last_played = datetime.now()
        
        # Generate match history for this country
        # Mastery level 3: more wins leading to mastery
        # Mastery level 1-2: some wins, some losses
        # Mastery level 0: mostly losses
        
        # Determine number of matches to generate (at least correct_guesses, plus some losses)
        num_wins = correct_guesses
        
        if mastery_level >= 3:
            # For mastered countries: show progression from losses to consistent wins
            # Need at least 3 consecutive wins without hints to reach mastery 3
            num_losses = random.randint(2, 6)  # Some initial struggles
            
            # Create a progression: some losses, then alternating, then consistent wins
            matches = []
            
            # Initial losses/mixed results
            for i in range(num_losses):
                used_hints = random.choice([True, False])
                matches.append(('loss', used_hints))
            
            # Some wins with hints
            for i in range(random.randint(1, 3)):
                matches.append(('win', True))
            
            # Final progression to mastery (consecutive wins without hints)
            for i in range(num_wins):
                matches.append(('win', False))
        
        elif mastery_level >= 1:
            # Partially mastered: mix of wins and losses
            num_losses = random.randint(1, num_wins)
            matches = []
            
            # Mix wins and losses
            for i in range(num_wins):
                matches.append(('win', random.choice([True, False])))
            for i in range(num_losses):
                matches.append(('loss', random.choice([True, False])))
            
            random.shuffle(matches)
        
        else:
            # No mastery: mostly losses
            num_losses = random.randint(num_wins + 1, num_wins + 5)
            matches = []
            
            for i in range(num_wins):
                matches.append(('win', True))  # wins with hints
            for i in range(num_losses):
                matches.append(('loss', random.choice([True, False])))
            
            random.shuffle(matches)
        
        # Insert match history with progressive timestamps
        base_time = last_played - timedelta(days=len(matches))
        
        for idx, (result, used_hints) in enumerate(matches):
            match_time = base_time + timedelta(days=idx, hours=random.randint(0, 23), 
                                               minutes=random.randint(0, 59))
            
            cursor.execute("""
                INSERT INTO match_history (user_id, country_id, match_result, used_hints, played_at)
                VALUES (?, ?, ?, ?, ?)
            """, (demo_user_id, country_id, result, 1 if used_hints else 0, match_time))
            
            total_matches += 1
    
    conn.commit()
    
    # Print statistics
    cursor.execute("""
        SELECT 
            COUNT(*) as total_matches,
            SUM(CASE WHEN match_result = 'win' THEN 1 ELSE 0 END) as total_wins,
            SUM(CASE WHEN match_result = 'loss' THEN 1 ELSE 0 END) as total_losses
        FROM match_history
        WHERE user_id = ?
    """, (demo_user_id,))
    
    total_matches, total_wins, total_losses = cursor.fetchone()
    win_rate = (total_wins / total_matches * 100) if total_matches > 0 else 0
    
    print(f"\n=== Match History Statistics ===")
    print(f"Total matches: {total_matches}")
    print(f"Total wins: {total_wins}")
    print(f"Total losses: {total_losses}")
    print(f"Win rate: {win_rate:.1f}%")
    
    # Show some sample progression for a mastered country
    cursor.execute("""
        SELECT c.name, cm.mastery_level
        FROM country_mastery cm
        JOIN countries c ON cm.country_id = c.country_id
        WHERE cm.user_id = ? AND cm.mastery_level >= 3
        LIMIT 1
    """, (demo_user_id,))
    
    sample_country = cursor.fetchone()
    if sample_country:
        country_name, mastery_level = sample_country
        cursor.execute("""
            SELECT 
                c.country_id,
                c.name
            FROM country_mastery cm
            JOIN countries c ON cm.country_id = c.country_id
            WHERE cm.user_id = ? AND cm.mastery_level >= 3
            LIMIT 1
        """, (demo_user_id,))
        
        country_data = cursor.fetchone()
        if country_data:
            country_id_val, _ = country_data
            
            cursor.execute("""
                SELECT match_result, used_hints, played_at
                FROM match_history
                WHERE user_id = ? AND country_id = ?
                ORDER BY played_at
            """, (demo_user_id, country_id_val))
            
            matches = cursor.fetchall()
            
            print(f"\nExample progression for {country_name} (Mastery Level {mastery_level}):")
            print("Match sequence (recent matches):")
            
            for idx, (result, used_hints, played_at) in enumerate(matches[-10:], start=max(1, len(matches) - 9)):
                hints_text = "with hints" if used_hints else "no hints"
                symbol = "✓" if result == 'win' else "✗"
                print(f"  Match {idx}: {symbol} {result.upper()} ({hints_text})")
    
    conn.close()
    print("\n✓ Match history created successfully!")

if __name__ == "__main__":
    main()
