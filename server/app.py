import json
import math

import MySQLdb
import MySQLdb.cursors
import requests
from flask import Flask, render_template, jsonify, request, session

app = Flask(__name__, static_url_path='')
app.secret_key = b'A+jWl4h6wMkR7LcWBm85AO8q'
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

    db.cursor().execute('CREATE TABLE IF NOT EXISTS `Inventory` (`USER_ID` int,`SERIAL_NUMBER` varchar(30),`PRODUCT_TITLE` varchar(255),`PRICE` int,`QUANTITY_ON_HAND` int, `MIN_QUANTITY_BEFORE_NOTIFY` int, `LAST_UPDATE` DATETIME DEFAULT CURRENT_TIMESTAMP);')
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


@app.route('/getinfo')
def getBySerial():
    serial = str(request.args.get('serial'))
    db = get_db()
    db_cursor = db.cursor()
    print(serial)
    if(serial.isdigit() == False):
       return '{Error:\"Invalid serial\"}', 400
    if db_cursor.execute('SELECT * FROM Inventory WHERE SERIAL_NUMBER=%s', (serial,)) > 0:
        entry = db_cursor.fetchall()[0]
        idOfScanner = entry[0]
        serial = entry[1]
        title = entry[2]
        QOH = entry[3]
        MQBN = entry[4]
        # Item found
        db.close()
        return jsonify(USER_ID=idOfScanner, SERIAL_NUMBER=serial,PRODUCT_TITLE=title,QUANTITY_ON_HAND=QOH,MIN_QUANTITY_BEFORE_NOTIFY=MQBN), 200
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
        scanID = row[0]
        barcodeID = row[1]
        userID = row[2]
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
    except MySQLdb.Error as e:
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
            return {}, 200
        else:
            return jsonify(message="Item with barcode `" + str(item['barcodeID']) + "` does not exist. Use `createItem` endpoint first."), 400
    except MySQLdb.Error as e:
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
            return {}, 200
        else:
            return jsonify(message="Item with barcode `" + str(item['barcodeID']) + "` does not exist. Use `createItem` endpoint first."), 400
    except MySQLdb.Error as e:
        db.rollback()
        db.close()
        return jsonify(message=e.args), 500
