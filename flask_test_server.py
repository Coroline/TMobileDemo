from flask import Flask, request
app = Flask(__name__)

wifiInfo = {
   "5$/hour"+"d4:5d:64:32:c9:f4": "problem?"
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
def set():
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

@app.route('/register', methods=['POST'])
def get():
   if 'username'in request.form and 'password' in request.form and 'fullname' in request.form:
      print("start registering")
      print("ssid", request.form["ssid"])
      print("bssid", request.form["bssid"])
      print("password", request.form["password"])
      combine = request.form["ssid"]+request.form["bssid"]
      wifiInfo[combine] = request.form["password"]
      return 'success'
   else:
      return 'bad request!', 400

if __name__ == '__main__':
   app.run(host="0.0.0.0", port=5002)