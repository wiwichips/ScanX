import MySQLdb
import MySQLdb.cursors
from flask import Flask, render_template, jsonify, request, session
from urllib.parse import unquote
from pyfcm import FCMNotification

push_service = FCMNotification(api_key="AAAAAcR0TJo:APA91bHXcZriWflLWElaYyWjuN8mMZuNoID7sdU6vtphNcxAHVCWoquxQK99kxjpg_GP_FOSPAJImleMGhxlsH4TN6VskzWvLFyZfr_hPGXSQOVk05rloo1F62UwAeNzB-X2XVd3PnK0")
app = Flask(__name__, static_url_path='')
app.secret_key = b'A+jWl4h6wMkR7LcWBm85AO8q'
registration_ids = set([])
if __name__ == '__main__':
    app.run(host='0.0.0.0')

def get_db() -> MySQLdb.Connection:
    db = MySQLdb.connect(host='127.0.0.1',
                         user='app',
                         passwd='3EQCjVrNQ9Z9Ysvf',
                         db='scanx')
    return db


def check_db_table():
    # Makes sure the table exists and has the right columns
    db = get_db()

    db.cursor().execute('CREATE DATABASE IF NOT EXISTS `scanx`;')
    db.cursor().execute('USE `scanx`;')

    db.cursor().execute('CREATE TABLE IF NOT EXISTS `Inventory` (`USER_ID` int,`SERIAL_NUMBER` varchar(30),`PRODUCT_TITLE` varchar(255),`PRICE` DECIMAL(65,2),`QUANTITY_ON_HAND` int, `MIN_QUANTITY_BEFORE_NOTIFY` int, `LAST_UPDATE` DATETIME DEFAULT CURRENT_TIMESTAMP);')
    db.commit()
    db.close()
    db = get_db()
    db.cursor().execute('CREATE TABLE IF NOT EXISTS `Scans` (SCAN_ID int AUTO_INCREMENT PRIMARY KEY,`BARCODE_ID` varchar(30),`USER_ID` int, `LAST_UPDATE` DATETIME DEFAULT CURRENT_TIMESTAMP);')
    db.commit()
    db.close()
    db = get_db()
    db.cursor().execute('CREATE TABLE IF NOT EXISTS `Users` (USER_ID int AUTO_INCREMENT PRIMARY KEY,`USERNAME` varchar(30),`PASSWORD` varchar(30), `LAST_UPDATE` DATETIME DEFAULT CURRENT_TIMESTAMP);')
    db.commit()
    db.close()

check_db_table()


# Serves the main HTML page
@app.route('/')
def index(name=None):
    return render_template('', name=name)

@app.route('/getInventory', methods=['GET'])
def getInventory():
    db = get_db()
    db_cursor = db.cursor()
    db_cursor.execute('SELECT * FROM Inventory')
    entry = db_cursor.fetchall()
    data=[]
    for row in entry:
        SERIAL_NUMBER = row[1]
        PRODUCT_TITLE = row[2]
        PRICE = row[3]
        QUANTITY_ON_HAND = row[4]
        singleObject = {}
        singleObject['SERIAL_NUMBER'] = SERIAL_NUMBER
        singleObject['PRODUCT_TITLE'] = PRODUCT_TITLE
        singleObject['PRICE'] =PRICE
        singleObject['QUANTITY_ON_HAND'] =QUANTITY_ON_HAND
        data.append(singleObject)
    # Item found
    db.close()
    return str(data), 200

@app.route('/getinfo')
def getBySerial():
    serial = unquote(str(request.args.get('serial')))
    db = get_db()
    db_cursor = db.cursor()
    print(serial)
    if db_cursor.execute('SELECT * FROM Inventory WHERE SERIAL_NUMBER=%s', (serial,)) > 0:
        entry = db_cursor.fetchall()[0]
        idOfScanner = int(entry[0])
        serial = str(entry[1])
        title = str(entry[2])
        PRICE = float(entry[3])
        QOH = int(entry[4])
        MQBN = float(entry[5])
        # Item found
        db.close()
        return jsonify(USER_ID=idOfScanner, SERIAL_NUMBER=serial, PRODUCT_TITLE=title, PRICE=PRICE, QUANTITY_ON_HAND=QOH, MIN_QUANTITY_BEFORE_NOTIFY=MQBN), 200
    else:
        return '{Error:\"Serial not in database\"}',400

@app.route('/getLastScans')
def getLastScans():
    db = get_db()
    db_cursor = db.cursor()
    db_cursor.execute('SELECT * FROM Scans ORDER BY SCAN_ID DESC LIMIT 10')
    entry = db_cursor.fetchall()
    data=[]
    for row in entry:
        scanID = int(row[0])
        barcodeID = str(row[1])
        userID = int(row[2])
        singleObject = {}
        singleObject['SCAN_ID'] = scanID
        singleObject['BARCODE_ID'] = barcodeID
        singleObject['USER_ID'] =userID
        data.append(singleObject)
    # Item found
    db.close()
    return str(data), 200
    
@app.route('/createUser', methods=['POST'])
def createUser():
    print("here")
    request_json = request.get_json(force=True)
    
    db = get_db()
    db_cursor = db.cursor()
    
    # Try and find the user in the database
    if db_cursor.execute('SELECT * FROM Users WHERE username=%s', (request_json['username'],)) > 0:
        db.close()
        return jsonify(message='User already exist'), 401
    else:
        # User does not exist in the database, register them
        try:
            
            db_cursor.execute('INSERT INTO Users (username, password) VALUES (%s, %s)', (request_json['username'], request_json['password']))
            db.commit()
            db.close()
            #session['username'] = request_json['username']
            #session['logged_in'] = True
            return jsonify(message='New account created'), 200
        except MySQLdb.Error as e:
            db.rollback()
            db.close()
            return jsonify(message=e.args), 500


@app.route('/logout', methods=['GET'])
def logout():
    session.pop('username', None)
    session['logged_in'] = False
    return jsonify(message='OK'), 200


# If input is invalid, returns the invalid item. Otherwise returns true.
def validate_create_edit_input(item):
    # Input validation
    if not isinstance(item['barcodeID'], str):
        return item['barcodeID'], 'string'
    if not isinstance(item['name'], str):
        return item['name'], 'string'
    if not isinstance(item['price'], float):
        return item['price'], 'float'
    if not isinstance(item['minStock'], int):
        return item['minStock'], 'int'
    if not isinstance(item['count'], int):
        return item['count'], 'int'
    return True


# Tries to get a user ID from the session
def get_user_id():
    if session['user_id']:
        return session['user_id']
    else:
        raise Exception("No user ID in session.")


# Checks stock level for an item
def check_stock_item(barcode: str):
    db = get_db()
    cursor = db.cursor()

    try:
        if cursor.execute('SELECT * FROM Inventory WHERE SERIAL_NUMBER=%s AND QUANTITY_ON_HAND < MIN_QUANTITY_BEFORE_NOTIFY ', (barcode,)) > 0:
            item = cursor.fetchone()
            db.close()
            message_title = "Stock Level Alert"
            message_body = "Item has low stock: " + item[2]
            result = push_service.notify_multiple_devices(registration_ids=list(registration_ids), message_title=message_title, message_body=message_body)
            print(message_title)
            print(message_body)
            print(result)
            print("\n")
    except Exception as e:
        db.close()
        print("Error occurred when accessing database")


@app.route("/subscribe", methods=['POST'])
def subscribe_device():
    payload = request.get_json()
    print(payload['id'])
    registration_ids.add(payload['id'])
    return {}, 200


@app.route("/createItem", methods=['POST'])
def create_item():
    item = request.get_json()

    # Input validation
    is_item_valid = validate_create_edit_input(item)
    if is_item_valid is not True:
        return jsonify(message='Item ' + str(is_item_valid[0]) + ' was not of expected type ' + str(is_item_valid[1]) + '.'), 400

    # try:
    #     userID = get_user_id()
    # except:
    #     return jsonify(message='Could not get a user ID. Try logging in first.'), 401

    userID = -1

    db = get_db()
    db_cursor = db.cursor()

    try:
        if db_cursor.execute('SELECT * FROM Inventory WHERE SERIAL_NUMBER=%s', (item['barcodeID'],)) > 0:
            return jsonify(message="Item with barcode `" + str(item['barcodeID']) + "` already exists. Use `editItem` endpoint."), 400
        else:
            db_cursor.execute('INSERT INTO Inventory(USER_ID,SERIAL_NUMBER,PRODUCT_TITLE,PRICE,MIN_QUANTITY_BEFORE_NOTIFY,QUANTITY_ON_HAND) VALUES(%s,%s,%s,%s,%s,%s)', (userID, item['barcodeID'], item['name'], item['price'], item['minStock'], item['count'],))
            db.commit()
            db.close()
            return {}, 200
    except Exception as e:
        db.rollback()
        db.close()
        return jsonify(message=e.args), 500


@app.route("/editItem", methods=['PUT'])
def edit_item():
    item = request.get_json()

    # Input validation
    is_item_valid = validate_create_edit_input(item)
    if is_item_valid is not True:
        return jsonify(message='Item ' + str(is_item_valid[0]) + ' was not of expected type ' + str(is_item_valid[1]) + '.'), 400

    # try:
    #     userID = get_user_id()
    # except:
    #     return jsonify(message='Could not get a user ID. Try logging in first.'), 401

    userID = -1

    db = get_db()
    db_cursor = db.cursor()

    try:
        if db_cursor.execute('SELECT * FROM Inventory WHERE SERIAL_NUMBER=%s', (item['barcodeID'],)) > 0:
            db_cursor.execute('UPDATE Inventory SET USER_ID=%s, SERIAL_NUMBER=%s, PRODUCT_TITLE=%s, PRICE=%s, MIN_QUANTITY_BEFORE_NOTIFY=%s, QUANTITY_ON_HAND=%s WHERE SERIAL_NUMBER=%s', (userID, item['barcodeID'], item['name'], item['price'], item['minStock'], item['count'], item['barcodeID'],))
            db.commit()
            db.close()
            check_stock_item(item['barcodeID'])
            return {}, 200
        else:
            return jsonify(message="Item with barcode `" + str(item['barcodeID']) + "` does not exist. Use `createItem` endpoint first."), 400
    except Exception as e:
        db.rollback()
        db.close()
        return jsonify(message=e.args), 500


@app.route("/editStock", methods=['PUT'])
def edit_stock():
    item = request.get_json()

    # Input validation
    if not isinstance(item['barcodeID'], str):
        return jsonify(message='Item ' + str(item['barcodeID']) + ' was not of expected type `string`.'), 400
    if not isinstance(item['count'], int):
        return jsonify(message='Item ' + str(item['count']) + ' was not of expected type `int`.'), 400

    # try:
    #     userID = get_user_id()
    # except:
    #     return jsonify(message='Could not get a user ID. Try logging in first.'), 401

    userID = -1

    db = get_db()
    db_cursor = db.cursor()

    try:
        if db_cursor.execute('SELECT * FROM Inventory WHERE SERIAL_NUMBER=%s', (item['barcodeID'],)) > 0:
            db_cursor.execute('UPDATE Inventory SET USER_ID=%s, QUANTITY_ON_HAND=%s WHERE SERIAL_NUMBER=%s', (userID, item['count'], item['barcodeID'],))
            db.commit()
            db.close()
            check_stock_item(item['barcodeID'])
            return {}, 200
        else:
            return jsonify(message="Item with barcode `" + str(item['barcodeID']) + "` does not exist. Use `createItem` endpoint first."), 400
    except Exception as e:
        db.rollback()
        db.close()
        return jsonify(message=e.args), 500


@app.route("/search", methods=['GET'])
def search_item():
    item = request.get_json()

    #Input validation
    if not (isinstance(item['type'], str)):
        return jsonify(message='Item ' + str(item['type']) + ' was not of expected type `string`.'), 400
    if not (isinstance(item['term'], str)):
        return jsonify(message='Item ' + str(item['term']) + ' was not of expected type `string`.'), 400

    if not item['type'] == "barcode" and not item['type'] == "name":
        return jsonify(message='Value ' + str(item['type']) + ' was not of expected values `barcode` or `name`.'), 400

    db = get_db()
    db_cursor = db.cursor()

    try:
        if item['type'] == "name":
            db_cursor.execute("SELECT * FROM Inventory WHERE UPPER(PRODUCT_TITLE) LIKE UPPER(%s)", ("%" + item['term'] + "%",))
        elif item['type'] == "barcode":
            db_cursor.execute("SELECT * FROM Inventory WHERE UPPER(SERIAL_NUMBER) LIKE UPPER(%s)", ("%" + item['term'] + "%",))

        found_rows = db_cursor.fetchall()
        db.close()
    except Exception as e:
        db.rollback()
        db.close()
        return jsonify(message=e.args), 500

    item_array = []
    for row in found_rows:
        item_array.append({"barcodeID": str(row[1]), "title": str(row[2]), "price": float(row[3]), "onHand": int(row[4])})

    return jsonify(item_array), 200
