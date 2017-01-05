from __future__ import print_function
from flask import render_template, request, make_response
from cityApi import app
from citiesDB import *
import json, sys # sys - for printing errors
from bson.objectid import ObjectId # for processing _id strings
from functools import wraps # for creating login-required pages
import random, string # for generating our cookies



# initialize our database object. we're going to be
# needing it quite a bit...
db = initDB()


# print to standard error
def errorPrint(string):
        print(string, file=sys.stderr)


# pulls the list of users from our db and prepares it
# to be jsonified and returned to the user
def buildUserList():
	# build the list of countries
	users = getAllUsers(db)
	userList = []
	for q in users:
		q['_id'] = str(q['_id'])
		for r in q['cities']:
			r['_id'] = str(r['_id'])
		userList.append(q)
	return userList 


# check whether client's cookie matches a user's. if so,
# returns a user object. otherwise returns None
def validateCookie( cookie ):
	u = db.users.find_one({"cookie": cookie})
        if u is not None:
                return u
        return None


# the function factory for creating login-required pages
def requiresLogin(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
                cookie = request.cookies.get('session-cookie')
                if cookie is not None:
                        errorPrint("cookie found...")
                        u = validateCookie(cookie)
                        if u is not None:
                                errorPrint(u['email'] + " is still logged in")
                                return f(*args, **kwargs)
		message = ("Error: must be logged in to view this "
				"resource. Please make a request to "
				"/session to create a new session.")
                return message, 401


        return decorated_function

# check given password against user with given email
def validatePasswd( email, passwd):
	u = db.users.find_one({"email": email})
        if u == None:
                errorPrint("Login error: User not found...")
                return None
        elif u['passwd'] != passwd:
                errorPrint("Login error: Bad passwd...")
                return None
        else:
                errorPrint("Good authentication...")
                return u

# create session cookie for our user who's logging in
def createCookie( email ):
	u = db.users.find_one({"email": email})
        cookieStr = ""
        for i in range(63):
                cookieStr = cookieStr + random.choice(string.letters +
                        string.digits)
	newInfo = {}
	newInfo['cookie'] = cookieStr
	updateUser(db, u['_id'], newInfo)
        errorPrint(cookieStr)
        return cookieStr



# creates or terminates a session by either generating or deleting
# a client's cookie, based on DELETE or POST accordingly
@app.route("/session", methods=['POST', 'DELETE'])
def session():
	if request.method == 'DELETE':
                cookie = request.cookies.get('session-cookie')
                if cookie is not None:
			u = db.users.find_one({"cookie": cookie})
			newInfo = {}
			newInfo['cookie'] = ""
			updateUser(db, u['_id'], newInfo)
			response = make_response("logout successful", 200)
			response.set_cookie('session-cookie',
				value='', expires=0)
			return response	
		message = "Error: already logged out"
                return message, 400
	if request.method == 'POST':
		# only dealing with json requests
		if(request.headers is not None 
			and "application/json" not in request.headers['Accept']):
			return ("Error: must accept application/json",
				406,)

		data = request.get_json(force=True)
                email = data['email']
                passwd = data['passwd']
                user = validatePasswd(email, passwd)
                if user != None:
                        response = make_response("logged in successfully", 200)
                        response.set_cookie('session-cookie',
                                value=createCookie(email))
                        return response
                else:
                        message = "Error: Invalid username/password"
                        return message, 400

		
# returns a list of possible routs and methods
@app.route("/", methods=['GET', 'POST'])
def index():
        # retrieve user info
        if request.method == 'GET':
                message = ("Possible routse:\n\n"
                "/ - GET: displays this info\n"
                "/session - POST: start new session\n"
                "/session - DELETE: ends session\n"
                "/user - POST: registers user\n"
                "/user/profile - GET: retrieve current user's data\n"
                "/user/profile - PUT: modifies current user\n"
                "/user/profile - DELETE: deletes current user\n"
                "/user/city - GET: gets full list of user's cities\n"
                "/user/city - POST: adds new user city\n"
                "/user/city/<id> - GET: gets user city\n"
                "/user/city/<id> - DELETE: deletes city\n"
                "/user/city/<id> - PUT: updates user city\n")
                return message, 200


# route for registering new users
@app.route("/user", methods=['POST'])
def user():
	# add new user
	if request.method == 'POST': 
		# only dealing with json requests
		if(request.headers is not None 
			and "application/json" not in request.headers['Accept']):
			return ("Error: must accept application/json",
				406,)

		# for now, simply return some form data
		print(request.get_json(force=True), file=sys.stderr)
		data = request.get_json(force=True)
		if addUser(db, data) == False:
			return ("Error: bad input; failed to add country",
				400,)
		else:
			message = "user added successfully"
			return message, 200


# if GET, then return the user with _id = user_id; if PUT, 
# then update the user with _id = user_id setting its values
# to whatever are included in the supplied JSON data (i.e. if a
# key is excluded, it is not updated ); if DELETE, then delete
# user with _id = user_id
#
# Note: deletion and update commands will cascade, so cities inside
# the deleted/updated country will likewide be deleted or updated
# according to user supplied data
@app.route("/user/profile", methods=['GET', 'PUT', 'DELETE'])
@requiresLogin
def oneUser():
	# attempt to retrieve the country by request cookie
	# note, we know this exists because login is required
	# for this url
	cookie = request.cookies.get('session-cookie')
	user = db.users.find_one({"cookie": cookie})

	if user != None:
		user['_id'] = str(user['_id'])
		jsonUser = json.dumps(user)
	else:
		user = {}
		jsonUser = json.dumps(user)


	# route GET and POST requests	
	if request.method == 'GET':
		return jsonUser


	elif request.method == 'PUT': 
		# only dealing with json requests
		if(request.headers is not None 
			and "application/json" not in request.headers['Accept']):
			return ("Error: must accept application/json",
				406,)
		
		# print the json data to error out
		print(request.get_json(force=True), file=sys.stderr)
		data = request.get_json(force=True)
		if updateUser(db, str(user["_id"]), data) == False:
			return ("Error updating user...",
				400,)
		else:
			user = db.users.find_one( {"cookie": cookie} )
			user['_id'] = str(user['_id'])
			jsonUser = json.dumps(user)
			return jsonUser


	elif request.method == 'DELETE': 
		# only dealing with json requests
		if(request.headers is not None 
			and "application/json" not in request.headers['Accept']):
			return ("Error: must accept application/json",
				406,)
		
		# print the json data to error out
		if deleteUser(db, str(user['_id'])) == False:
			return ("Error deleting country...",
				400,)
		else:
			response = make_response("user deleted successfuly", 200)
			response.set_cookie('session-cookie',
				value='', expires=0)
			return response	


# if GET, then return a list of the user's cities in JSON format; 
# if POST, then add a new city based on JSON application data from the user
@app.route("/user/city", methods=['GET', 'POST'])
@requiresLogin
def allCities():
	# get the user's full list of cities
	if request.method == 'GET':
		cookie = request.cookies.get('session-cookie')
		user = db.users.find_one({"cookie": cookie})
		return json.dumps(user['cities'])

	# handle post request - create new country entry
	elif request.method == 'POST': 
		cookie = request.cookies.get('session-cookie')
		user = db.users.find_one({"cookie": cookie})

		# only dealing with json requests
		if(request.headers is not None 
			and "application/json" not in request.headers['Accept']):
			return ("Error: must accept application/json",
				406,)

		# print the json data to error out
		print(request.get_json(force=True), file=sys.stderr)
		data = request.get_json(force=True)
		if addCity(db, data, str(user["_id"])) == False:
			return ("Error: bad input; failed to add country",
				400,)
		else:
			# build and return new city list
			user = db.users.find_one({"cookie": cookie})
			return json.dumps(user['cities'])


# if GET, then return user's city with id = city_id; if PUT, 
# then update the city with id = city_id setting its values
# to whatever are included in the supplied JSON data (i.e. if a
# key is excluded, it is not updated ); if DELETE, then delete
# city with id = city_id 
@app.route("/user/city/<city_id>", methods=['GET', 'PUT', 'DELETE'])
@requiresLogin
def oneCity(city_id):
	# get our user object
	cookie = request.cookies.get('session-cookie')
	user = db.users.find_one({"cookie": cookie})

	# get a city in a user's city list matching the id
	# provided in the url
	if request.method == 'GET':
		if city_id != None:
			city = getCity(db, user['_id'], city_id)
		else:
			errorPrint("oneCity: no id provided")
			city = None
		if city != None:
			jsonCity = json.dumps(city)
			return jsonCity, 200
		else:
			city = {}
			jsonCity = json.dumps(city)
			return jsonCity, 404


	# attempts to update city with given id
	elif request.method == 'PUT': 
		# only dealing with json requests
		if(request.headers is not None 
			and "application/json" not in request.headers['Accept']):
			return ("Error: must accept application/json",
				406,)
		
		# print the json data to error out
		print(request.get_json(force=True), file=sys.stderr)
		data = request.get_json(force=True)
		if updateCity(db, user['_id'], city_id, data) == False:
			return ("Error updating city...",
				400,)
		else:
			city = getCity(db, user['_id'], city_id)
			jsonCity = json.dumps(city)
			return jsonCity, 200

	# attempt to delete city with given id
	elif request.method == 'DELETE':
		if deleteCity(db, user['_id'], city_id) == False:
			return ("Error deleting city...",
				400,)
		else:
			city = {}
			jsonCity = json.dumps(city)
			return jsonCity, 200
