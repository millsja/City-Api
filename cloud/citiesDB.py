from __future__ import print_function
import sys
from pymongo import MongoClient
from bson.objectid import ObjectId


# initialize the connection to our mongo database
def initDB():
	client = MongoClient('localhost:27017')
	db = client.cityDB
	return db


# checks a user's list of cities and determines
# whether the city already exists in there
def containsCity(db, userId, cityId):
	# first retrieve the city list
	u = db.users.find_one( {"_id": userId} )
	cities = u['cities']

	# check the name of our city against that
	# of each city in the list. if we find it,
	# then return true
	for c in cities:
		if c['id'] == int(cityId):
			return True

	# if we've reached this point, then it
	# isn't in the list	
	return False


# validates citiy edit based on dictionary country and ObjectId countryId
def validateCityEdit(db, userId, cityId, city):

	# check to see whether city even exists
	if containsCity(db, userId, cityId)  == False:
		print("Can't update city: city doesn't exist", file=sys.stderr)
		return False

	# make sure country name isn't blank
	if 'country' in city and city['country'] == "":
		print("Can't update city: country cannot be blank", 
			file=sys.stderr)
		return False

	# make sure that city name isn't blank or already taken
	if 'name' in city and city['name'] == "":
		print("Can't update city: name cannot be blank", 
			file=sys.stderr)
		return False

	# make sure new population is non-negative number 
	if 'population' in city:
		if city['population'] < 0:
			print("Can't update city: pop must be positive", 
				file=sys.stderr)
			return False

	# if we've come this far, then all is well
	return True


# takes a city object, updates the values and returns
def updateCityEntry(dest, source):
	for key in source:
		dest[key] = source[key]
	return dest


# finds city by _id = cityId and updates with values given
# in city dictionary
def updateCity(db, userId, cityId, city):	
	try:
		# first retrieve the city list
		u = db.users.find_one( {"_id": userId} )
		cities = u['cities']

		# check to see whether we're okay to make these changes
		if validateCityEdit(db, userId, cityId, city) == True:

			# update the country entry with new city data
			for q in range(0, len(cities)):
				if cities[q]['id'] == int(cityId):
					cities[q] = updateCityEntry(
						cities[q], city)	

			# now actually run the update
			db.users.update({"_id": u['_id']},
				{"$set": {"cities": cities}},)

			# all is well, so return true
			return True

		# not good: abort, abort!
		else:
			print("Error: updateCity...", file=sys.stderr)
			return False

	except Exception, e:
		print("Error: updateCity...", file=sys.stderr)
		print(str(e), file=sys.stderr)
		return False


# checks a user's list of cities for a matching
# city id and then deletes that city. returns
# true on success, false on failure
def deleteCity(db, userId, cityId):
	# first retrieve the city list
	u = db.users.find_one( {"_id": userId} )
	cities = u['cities']

	# check the name of our city against that
	# of each city in the list. if we find it,
	# then return it 
	found = False
	toRemove = {}
	for c in cities:
		if c['id'] == int(cityId):
			toRemove = c
			found = True
	u['cities'].remove(toRemove)
	db.users.update({"_id": userId}, 
		{"$set": {"cities": u['cities']}},)

	return found


# checks a user's list of cities for a matching
# city id and then returns that city object
# if found 
def getCity(db, userId, cityId):
	# first retrieve the city list
	u = db.users.find_one( {"_id": userId} )
	cities = u['cities']

	# check the name of our city against that
	# of each city in the list. if we find it,
	# then return it 
	for c in cities:
		if c['id'] == int(cityId):
			return c 

	# if we've reached this point, then it
	# isn't in the list	
	return None


# validates new city entry based on dictionary city
def validateCityAdd(db, city, userId):

	if not city['name']:
		return False
	if city['population'] < 0:
		return False
	if not city['population']:
		return False
	if not city['country']:
		return False
	if not city['category']:
		return False
	else:
		return True



# validates user edit based on dictionary newInfo and ObjectId userId
def validateUserEdit(db, userId, newInfo):
	if db.users.find_one({"_id": userId}) == None:
		print("Can't update user: id doesn't exist", file=sys.stderr)
		return False
	if 'email' in newInfo:
		if db.users.find_one( {"email": newInfo['email']} ) != None:
			print("Can't update user: email already exists", 
				file=sys.stderr)
			return False
		if newInfo['email'] == "":
			print("Can't update user: email must not be blank", 
				file=sys.stderr)
			return False
	if 'fname' in newInfo:
		if newInfo['fname'] == "":
			print("Can't update user: fname must not be blank", 
				file=sys.stderr)
			return False
	if 'lname' in newInfo:
		if newInfo['lname'] == "":
			print("Can't update user: lname must not be blank", 
				file=sys.stderr)
			return False
	if 'passwd' in newInfo:
		if newInfo['passwd'] == "":
			print("Can't update user: passwd must not be blank", 
				file=sys.stderr)
			return False
	return True


# validates new user entry based on user dictionary
def validateUserAdd(db, user):
	if not user['email'] or user['email'] == "":
		print("Can't add user: email must not be blank", 
			file=sys.stderr)
		return False
	elif db.users.find_one( {"email": user['email']} ) != None:
		print("Can't add user: email already exists", 
			file=sys.stderr)
		return False
	elif not user['fname'] or user['fname'] == "":
		print("Can't add user: fname must not be blank", 
			file=sys.stderr)
		return False
	elif not user['lname'] or user['lname'] == "":
		print("Can't add user: lname must not be blank", 
			file=sys.stderr)
		return False
	elif not user['passwd'] or user['passwd'] == "":
		print("Can't add user: passwd must not be blank", 
			file=sys.stderr)
		return False
	else:
		return True


# finds user by _id = userId and updates with values given
# in newInfo dictionary
def updateUser(db, userId, newInfo):	
	try:
		userId = ObjectId(userId)
		if validateUserEdit(db, userId, newInfo) == True:
			# submit changes to db
			db.users.update({"_id": userId},
				{"$set": newInfo},)
			return True

		else:
			return False

	except Exception, e:
		print("Error: updateUser...", file=sys.stderr)
		print(str(e), file=sys.stderr)
		return False




# finds and deletes country at _id = countryId as well as any cities
# in that country (because you can't have cities without countries)
def deleteUser(db, userId):	
	try:
		userId = ObjectId(userId)
		user = db.users.find_one({"_id": userId})
		if user != None:
			# submit changes to db
			db.users.delete_one({"_id": userId})
			return True

		else:
			print("Error: deleteUser...", file=sys.stderr)
			return False

	except Exception, e:
		print("Error: deleteUser...", file=sys.stderr)
		print(str(e), file=sys.stderr)
		return False


# finds and deletes city by _id = cityId
# def deleteCity(db, cityId, userId):	
# 	try:
# 		cityId = ObjectId(cityId)
# 		city = db.cities.find_one({"_id": cityId})
# 		if city != None:
# 
# 			# delete city from country entry
# 			countryName = city['country']
# 			country = db.countries.find_one({"name": countryName})
# 			toRemove = {}
# 			for c in country['cities']:
# 				if c['name'] == city['name']:
# 					toRemove = c
# 			country['cities'].remove(toRemove)
# 			country = db.countries.update({"name": countryName}, 
# 				{"$set": {"cities": country['cities']}},)
# 			
# 			# delete the city entry
# 			db.cities.delete_one({"_id": cityId})	
# 
# 			return True
# 
# 		else:
# 			print("Error: deleteCity...", file=sys.stderr)
# 			return False
# 
# 	except Exception, e:
# 		print("Error: deleteCity...", file=sys.stderr)
# 		print(str(e), file=sys.stderr)
# 		return False


# adds new user with values from user dictionary
def addUser(db, user):	
	try:
		if validateUserAdd(db, user):
			db.users.insert( { "fname": user['fname'],
					"lname": user['lname'],
					"cities": [],
					"email": user['email'],
					"cookie": "",
					"passwd": user['passwd'] } )
			return True

		else:
			print("Error: addUser", file=sys.stderr)
			return False

	except Exception, e:
		print("Exception: " + str(e), file=sys.stderr)
		return False


# gets the max id number in a user's city list
# and then returns that + 1, which can be used
# to add the next city into the list
def getNextCityId(db, userId):
	user = db.users.find_one( {"_id": userId} )
	cities = user['cities']
	maxId = -1
	for c in cities:
		maxId = max( maxId, c['id'] )
	return (maxId + 1)


# adds city to user's list of cities based on values from 
# city dictionary and userId
def addCity(db, city, userId):	
	try:
		# first convert our userId string into an
		# id object
		userId = ObjectId(userId)

		# since this user must be validated, we're 
		# going to assume that the user exists and
		# is valid
		if validateCityAdd(db, city, userId):
			# now add the city to the user's city list 
			user = db.users.find_one(
				{"_id": userId} )
			cityList = user['cities']
			cityList.append({ "id": getNextCityId(db, userId),
					"name": city['name'],
					"population": city['population'],
					"category": city['category'],
					"country": city['country']  })
			db.users.update({"_id": userId},
				{"$set": {"cities": cityList}})
			return True

		else:
			print("Error: addCity", file=sys.stderr)
			return False

	except Exception, e:
		print(str(e), file=sys.stderr)
		return False


# drop all tables; admin use only
def dropTables(db):
	db.countries.delete_many( {} )
	db.cities.delete_many( {} )
	db.users.delete_many( {} )


# returns a json list of all cities in the db reverse sorted
# by population
def getAllCities(db):
	return db.cities.find().sort("population", -1)

		
# returns a json list of all countries in the db reverse sorted
# by population
def getAllUsers(db):
	return db.users.find()


# populate our "tables" with some city data
def createTables(db):
	users = [
		{"fname": "James",
		"lname": "Mills", 
		"email": "millsja@oregonstate.edu",
		"passwd": "orange"},

		{"fname": "Leslie",
		"lname": "Knope", 
		"email": "pawnee@example.com",
		"passwd": "orange"},

		{"fname": "Michael",
		"lname": "Bluth", 
		"email": "mbluth@example.com",
		"passwd": "orange"} ]

	cities = [
		{"name": "New York",
		"population": 8550405,
		"category": "A",
		"country": "United States"},

		{"name": "Paris",
		"population": 2229621,
		"category": "A",
		"country": "France"},

		{"name": "London",
		"population": 8673713,
		"category": "A",
		"country": "United Kingdom"},

		{"name": "Montreal",
		"population": 1649519,
		"category": "B",
		"country": "Canada"} ]

	for u in users:
		addUser(db, u)

	# for c in cities:
	# 	addCity(db, c)


# main function for initializing and re-initializing database
# and collections
def initCollections():
	db = initDB()
	dropTables(db)
	createTables(db)
	userList = getAllUsers(db)
	for e in userList:
		print("Adding " + e['fname'] + " " + e['lname'] +
			"...", file=sys.stderr)


# drop all collections and re-initialize them with new
# data and print each country as it's added
if __name__ == "__main__":
	initCollections()
