import mysql.connector
from mysql.connector import errorcode

# Database connection info - change as needed
config = {
    'user': 'root',
    'password': 'Abhishek@6',
    'host': 'localhost',
    'raise_on_warnings': True
}

DB_NAME = 'premium_restaurant'

TABLES = {}

TABLES['food_items'] = (
    '''
    CREATE TABLE IF NOT EXISTS food_items (
        id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        price DECIMAL(10,2) NOT NULL,
        category VARCHAR(100),
        image_path VARCHAR(255),
        is_special BOOLEAN DEFAULT 0,
        original_price DECIMAL(10,2),
        discount_percentage DECIMAL(5,2),
        avg_rating DECIMAL(3,2)
    )
    '''
)

TABLES['orders'] = (
    '''
    CREATE TABLE IF NOT EXISTS orders (
        id INT PRIMARY KEY AUTO_INCREMENT,
        order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
        subtotal DECIMAL(10,2) NOT NULL,
        tax DECIMAL(10,2) NOT NULL,
        total DECIMAL(10,2) NOT NULL
    )
    '''
)

TABLES['order_items'] = (
    '''
    CREATE TABLE IF NOT EXISTS order_items (
        id INT PRIMARY KEY AUTO_INCREMENT,
        order_id INT NOT NULL,
        food_item_id INT NOT NULL,
        item_name VARCHAR(255) NOT NULL,
        item_price DECIMAL(10,2) NOT NULL,
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
        FOREIGN KEY (food_item_id) REFERENCES food_items(id) ON DELETE CASCADE
    )
    '''
)

TABLES['reviews'] = (
    '''
    CREATE TABLE IF NOT EXISTS reviews (
        id INT PRIMARY KEY AUTO_INCREMENT,
        order_id INT NOT NULL,
        food_item_id INT NOT NULL,
        rating DECIMAL(3,2) CHECK (rating >= 0 AND rating <= 5),
        review_text TEXT,
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
        FOREIGN KEY (food_item_id) REFERENCES food_items(id) ON DELETE CASCADE
    )
    '''
)

def create_database(cursor):
    try:
        cursor.execute(
            f"CREATE DATABASE IF NOT EXISTS {DB_NAME} DEFAULT CHARACTER SET 'utf8mb4'")
        print(f"Database '{DB_NAME}' created or already exists.")
    except mysql.connector.Error as err:
        print(f"Failed to create database: {err}")
        exit(1)

def main():
    try:
        cnx = mysql.connector.connect(**config)
        cursor = cnx.cursor()

        create_database(cursor)
        cursor.execute(f"USE {DB_NAME}")

        for table_name in TABLES:
            table_description = TABLES[table_name]
            try:
                print(f"Creating table '{table_name}'... ", end='')
                cursor.execute(table_description)
                print("OK")
            except mysql.connector.Error as err:
                print(f"FAILED: {err}")
        cursor.close()
        cnx.close()
        print("All tables created successfully.")

    except mysql.connector.Error as err:
        if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
            print("Access denied: Check user name or password.")
        elif err.errno == errorcode.ER_BAD_DB_ERROR:
            print("Database does not exist.")
        else:
            print(err)

if __name__ == "__main__":
    main()
