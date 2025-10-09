import sqlite3
import hashlib
from datetime import datetime, timedelta
import random

def hash_password(password):
    """Hash password using SHA-256"""
    return hashlib.sha256(password.encode()).hexdigest()

def main():
    conn = sqlite3.connect('src/main/resources/db/geofarer.db')
    cursor = conn.cursor()
    
    # Create demo user
    demo_email = "demo@geofarer.com"
    demo_password = "Demo123!"
    demo_password_hash = hash_password(demo_password)
    
    # Check if demo user already exists
    cursor.execute("SELECT user_id FROM users WHERE email = ?", (demo_email,))
    existing_user = cursor.fetchone()
    
    if existing_user:
        demo_user_id = existing_user[0]
        print(f"Demo user already exists with ID: {demo_user_id}")
        # Clear existing mastery data for this user
        cursor.execute("DELETE FROM country_mastery WHERE user_id = ?", (demo_user_id,))
        print("Cleared existing mastery data")
    else:
        # Insert demo user
        cursor.execute("""
            INSERT INTO users (username, email, password_hash, created_at) 
            VALUES (?, ?, ?, ?)
        """, ("Demo User", demo_email, demo_password_hash, datetime.now()))
        demo_user_id = cursor.lastrowid
        print(f"Created demo user with ID: {demo_user_id}")
    
    # Get some countries from different regions
    cursor.execute("""
        SELECT country_id, region, name 
        FROM countries 
        WHERE region IS NOT NULL AND region != ''
        ORDER BY region, name
    """)
    countries = cursor.fetchall()
    
    # Group countries by region
    countries_by_region = {}
    for country_id, region, name in countries:
        if region not in countries_by_region:
            countries_by_region[region] = []
        countries_by_region[region].append((country_id, name))
    
    print(f"\nFound {len(countries_by_region)} regions")
    
    # Add mastered countries from different regions
    mastered_countries = []
    partially_mastered = []
    
    # Master 5-10 countries from each region
    for region, region_countries in countries_by_region.items():
        if len(region_countries) == 0:
            continue
            
        # Master some countries (level 3)
        num_to_master = min(random.randint(5, 10), len(region_countries))
        for i in range(num_to_master):
            mastered_countries.append(region_countries[i])
        
        # Partially master some countries (level 1-2)
        start_partial = num_to_master
        num_partial = min(random.randint(3, 7), len(region_countries) - start_partial)
        for i in range(start_partial, start_partial + num_partial):
            if i < len(region_countries):
                partially_mastered.append(region_countries[i])
    
    print(f"Will master {len(mastered_countries)} countries")
    print(f"Will partially master {len(partially_mastered)} countries")
    
    # Insert mastery data with progressive dates
    base_date = datetime.now() - timedelta(days=60)
    
    # Add mastered countries
    for idx, (country_id, name) in enumerate(mastered_countries):
        # Spread out over past 60 days
        days_offset = int((idx / len(mastered_countries)) * 60)
        last_played = base_date + timedelta(days=days_offset)
        
        # Mastered countries have level 3 and 5-15 correct guesses
        correct_guesses = random.randint(5, 15)
        
        cursor.execute("""
            INSERT INTO country_mastery (user_id, country_id, mastery_level, correct_guesses, last_played)
            VALUES (?, ?, ?, ?, ?)
        """, (demo_user_id, country_id, 3, correct_guesses, last_played))
    
    # Add partially mastered countries
    for idx, (country_id, name) in enumerate(partially_mastered):
        days_offset = int((idx / len(partially_mastered)) * 40) + 20
        last_played = base_date + timedelta(days=days_offset)
        
        # Partially mastered: level 1-2, fewer correct guesses
        mastery_level = random.randint(1, 2)
        correct_guesses = random.randint(1, mastery_level * 2)
        
        cursor.execute("""
            INSERT INTO country_mastery (user_id, country_id, mastery_level, correct_guesses, last_played)
            VALUES (?, ?, ?, ?, ?)
        """, (demo_user_id, country_id, mastery_level, correct_guesses, last_played))
    
    conn.commit()
    
    # Print summary
    cursor.execute("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN mastery_level >= 3 THEN 1 ELSE 0 END) as mastered,
            SUM(correct_guesses) as total_correct
        FROM country_mastery
        WHERE user_id = ?
    """, (demo_user_id,))
    
    total, mastered, total_correct = cursor.fetchone()
    
    print(f"\n=== Demo User Statistics ===")
    print(f"Email: {demo_email}")
    print(f"Password: {demo_password}")
    print(f"User ID: {demo_user_id}")
    print(f"Total countries played: {total}")
    print(f"Countries mastered: {mastered}")
    print(f"Total correct guesses: {total_correct}")
    
    # Show breakdown by region
    cursor.execute("""
        SELECT 
            c.region,
            COUNT(*) as total_played,
            SUM(CASE WHEN cm.mastery_level >= 3 THEN 1 ELSE 0 END) as mastered
        FROM country_mastery cm
        JOIN countries c ON cm.country_id = c.country_id
        WHERE cm.user_id = ?
        GROUP BY c.region
        ORDER BY c.region
    """, (demo_user_id,))
    
    print("\nBreakdown by region:")
    for region, total_played, mastered in cursor.fetchall():
        print(f"  {region}: {mastered}/{total_played} mastered")
    
    conn.close()
    print("\n✓ Demo user created successfully!")

if __name__ == "__main__":
    main()
