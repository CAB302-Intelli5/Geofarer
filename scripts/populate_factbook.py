import sqlite3
import requests

def create_database(db_path):
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()

    # Reset table
    cur.execute("DROP TABLE IF EXISTS factbook")
    cur.execute("""
    CREATE TABLE factbook (
        gec TEXT PRIMARY KEY,
        region TEXT,
        data TEXT
    )
    """)

    regions = [
        "africa", "antarctica", "australia-oceania", "central-america-n-caribbean",
        "central-asia", "east-n-southeast-asia", "europe", "middle-east",
        "north-america", "oceans", "south-america", "south-asia", "world"
    ]

    base_url = "https://raw.githubusercontent.com/factbook/factbook.json/master"

    for region in regions:
        # Get file listing via GitHub API
        api_url = f"https://api.github.com/repos/factbook/factbook.json/contents/{region}"
        resp = requests.get(api_url)
        if resp.status_code != 200:
            print(f"⚠️ Could not list region {region}")
            continue

        files = resp.json()
        for file_info in files:
            if file_info["name"].endswith(".json"):
                gec = file_info["name"][:-5]
                download_url = file_info["download_url"]

                print(f"Fetching {gec} from {region}")
                data = requests.get(download_url).text

                cur.execute("INSERT INTO factbook VALUES (?, ?, ?)",
                            (gec, region, data))

    conn.commit()
    conn.close()

if __name__ == "__main__":
    db_path = "../src/main/resources/factbook.db"
    create_database(db_path)
    print(f"Database created at {db_path}")
