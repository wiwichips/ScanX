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
    db.cursor().execute('CREATE TABLE IF NOT EXISTS `Inventory` (`ID_OF_SCANNER` int,`SERIAL_NUMBER` varchar(30),`PRODUCT_TITLE` varchar(255),`QUANTITY_ON_HAND` int, `MIN_QUANTITY_BEFORE_NOTIFY` int, `LAST_UPDATE` DATETIME DEFAULT CURRENT_TIMESTAMP);')
    db.commit()
    db.close()


check_db_table()


# Serves the main HTML page
@app.route('/')
def index(name=None):
    return render_template('', name=name)


@app.route('/getinfo')
def getdemoData():
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
        return jsonify(ID_OF_SCANNER=idOfScanner, SERIAL_NUMBER=serial,PRODUCT_TITLE=title,QUANTITY_ON_HAND=QOH,MIN_QUANTITY_BEFORE_NOTIFY=MQBN), 200
    else:
        return '{Error:\"Serial not in database\"}',400


# def get_scanner_ID():
#     try:
#         if session['barcodeID']:
#             return session['barcodeID']
#     except IndexError:
#         print("Generating new scanner ID")


@app.route("/createItem", methods=['POST'])
def create_item():
    item = request.get_json()

    # Input validation
    if not isinstance(item['barcodeID'], str):
        return jsonify(message="barcodeID is not a string."), 400
    if not isinstance(item['name'], str):
        return jsonify(message="name is not a string."), 400
    if not isinstance(item['price'], float):
        return jsonify(message="price is not a float."), 400
    if not isinstance(item['minStock'], int):
        return jsonify(message="minStock is not an int."), 400
    if not isinstance(item['count'], int):
        return jsonify(message="count is not an int."), 400

    db = get_db()
    db_cursor = db.cursor()

    # scannerID = get_scanner_ID()
    barcodeID = item['barcodeID']
    name = item['name']
    price = item['price']
    minStock = item['minStock']
    count = item['count']

    try:
        if db_cursor.execute('SELECT * FROM inventory WHERE SERIAL_NUMBER=%s', (barcodeID,)) > 0:
            return jsonify(message="Item with barcode `" + barcodeID + "` already exists. Use `editItem` endpoint."), 400
        else:
            db_cursor.execute('INSERT INTO inventory(SERIAL_NUMBER,PRODUCT_TITLE,PRICE,MIN_QUANTITY_BEFORE_NOTIFY,QUANTITY_ON_HAND) VALUES(%s,%s,%s,%s,%s)', (barcodeID, name, price, minStock, count,))
            # TODO: Scanner ID
            # db_cursor.execute('INSERT INTO inventory(ID_OF_SCANNER,SERIAL_NUMBER,PRODUCT_TITLE,PRICE,MIN_QUANTITY_BEFORE_NOTIFY,QUANTITY_ON_HAND) VALUES(%s,%s,%s,%s,%s,%s)', (scannerID, barcodeID, name, price, minStock, count,))
            db.commit()
            db.close()
            return {}, 200
    except MySQLdb.Error as e:
        db.close()
        return jsonify(message=e.args), 500


@app.route("/editItem", methods=['PUT'])
def edit_item():

    return 500


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

# # Delete user from database example
# @app.route('/users', methods=['DELETE'])
# def delete_user():
#     db = get_db()
#     try:
#         db.cursor().execute('DELETE FROM users WHERE username=%s', (session['username'],))
#         db.commit()
#         db.close()
#         # Clear the session cookie
#         session.pop('username', None)
#         session.pop('logged_in', None)
#         return jsonify(message='OK'), 200
#     except MySQLdb.Error as e:
#         db.rollback()
#         db.close()
#         return jsonify(message=e.args), 500
#
#
# # example of using a url param
# @app.route('/ficsit/<mod_id>', methods=['GET'])
# def mod_details(mod_id):
#     response = make_query(json.dumps({
#                                          'query': 'query {getMod(modId: "' + mod_id + '") {versions{link} full_description logo hotness downloads popularity}}'}))
#     return jsonify(json.loads(response.text)['data']['getMod']), 200
