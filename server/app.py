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
    return "1"

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

# example of using sessions if we plan on it in the future 
"""
@app.route('/login', methods=['POST'])
def login():
    request_json = request.get_json(force=True)
    db = get_db()
    db_cursor = db.cursor()
    
    # Try and find the user in the database
    if db_cursor.execute('SELECT * FROM users WHERE username=%s', (request_json['username'],)) > 0:
        if db_cursor.fetchall()[0][1] != request_json['password']:
            # Incorrect password
            db.close()
            return jsonify(message='Invalid login'), 401
    else:
        # User does not exist in the database, register them
        try:
            db_cursor.execute('INSERT INTO users VALUES (%s, %s)', (request_json['username'], request_json['password']))
            db.commit()
            db.close()
            session['username'] = request_json['username']
            session['logged_in'] = True
            return jsonify(newAccount=True), 200
        except MySQLdb.Error as e:
            db.rollback()
            db.close()
            return jsonify(message=e.args), 500

    # Set session variables and return logged in user
    db.close()
    session['username'] = request_json['username']
    session['logged_in'] = True
    return jsonify(message='Logged in', newAccount=False), 200


@app.route('/logout', methods=['GET'])
def logout():
    session.pop('username', None)
    session['logged_in'] = False
    return jsonify(message='OK'), 200
"""




# Delete user from database example
@app.route('/users', methods=['DELETE'])
def delete_user():
    db = get_db()
    try:
        db.cursor().execute('DELETE FROM users WHERE username=%s', (session['username'],))
        db.commit()
        db.close()
        # Clear the session cookie
        session.pop('username', None)
        session.pop('logged_in', None)
        return jsonify(message='OK'), 200
    except MySQLdb.Error as e:
        db.rollback()
        db.close()
        return jsonify(message=e.args), 500









# example of using a url param
@app.route('/ficsit/<mod_id>', methods=['GET'])
def mod_details(mod_id):
    response = make_query(json.dumps({'query': 'query {getMod(modId: "' + mod_id + '") {versions{link} full_description logo hotness downloads popularity}}'}))
    return jsonify(json.loads(response.text)['data']['getMod']), 200
