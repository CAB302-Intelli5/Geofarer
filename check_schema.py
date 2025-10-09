import sqlite3

conn = sqlite3.connect('src/main/resources/db/geofarer.db')
cursor = conn.cursor()

# Check users table schema
cursor.execute("SELECT sql FROM sqlite_master WHERE type='table' AND name='users'")
result = cursor.fetchone()
if result:
    print("Users table schema:")
    print(result[0])
    print()

# Check country_mastery table schema
cursor.execute("SELECT sql FROM sqlite_master WHERE type='table' AND name='country_mastery'")
result = cursor.fetchone()
if result:
    print("Country mastery table schema:")
    print(result[0])
    print()

# Check if there are any existing users
cursor.execute("SELECT user_id, email FROM users")
users = cursor.fetchall()
print(f"Existing users: {len(users)}")
for user in users:
    print(f"  User ID: {user[0]}, Email: {user[1]}")

conn.close()
