from flask import Flask, request
app = Flask(__name__)

wifiInfo = {
}

userInfo = {
   "test": {
      "password": "123456789",
      "fullname": "shenjianan"
   }
}

@app.route('/')
def hello_world():
   return "hello_world"

@app.route('/location', methods=['POST'])
def location():
   if 'address'in request.form and 'dateTime' in request.form:
      ip = request.form['address']
      print(ip)
      dateTime = request.form['dateTime']
      print(dateTime)
      return 'success'
   else:
      return 'bad request!', 400

@app.route('/login', methods=['POST'])
def login():
   if 'username'in request.form and 'password' in request.form:
      username = request.form["username"]
      password = request.form["password"]
      print("login", "username:", username, "password:", password)
      if username in userInfo and userInfo[username]["password"] == password:
         return "success!"
      else:
         return 'incorrct username or password', 400
   else:
      return 'incorrect format', 400

@app.route('/registeruser', methods=['POST'])
def registeruser():
   if 'username'in request.form and 'password' in request.form and 'fullname' in request.form:
      if request.form['username'] in userInfo:
         return 'username already exists!', 400
      print("start registering user")
      username = request.form['username']
      password = request.form['password']
      fullname = request.form['fullname']
      print('username', username)
      print('password', password)
      print('fullname', fullname)
      userInfo[username] = {
         "password": password,
         "fullname": fullname
      }
      return 'success'
   else:
      return 'bad request!', 400

@app.route('/registerhotspot', methods=['POST'])
def registerhotspot():
   if 'ssid'in request.form and 'password' in request.form and 'username' in request.form:
      print("start registering hotspot")
      ssid = request.form['ssid']
      password = request.form['password']
      wifiInfo[ssid] = {
         "password": password,
         "usage": 0
      }
      print(wifiInfo)
      return 'success'
   else:
      return 'bad request!', 400

@app.route('/checkhotspot', methods=['POST'])
def checkhotspot():
   if 'ssid'in request.form and 'username' in request.form:
      print("checking hotspot information")
      ssid = request.form['ssid']
      if ssid not in wifiInfo:
         print("no information!")
         return 'no information!', 400
      username = request.form['username']
      print(wifiInfo[ssid])
      return wifiInfo[ssid]["password"]
   else:
      return 'bad request!', 400

if __name__ == '__main__':
   app.run(host="0.0.0.0", port=5002)