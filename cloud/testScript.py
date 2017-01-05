import requests
import json
from citiesDB import initCollections

URL = "http://35.160.5.169/"

# check a document based on id
def verifyId( category, testId ):
	r = requests.get(URL + category + "/" + testId )
	if r != None:
		r = r.json()
		if testId == str(r['_id']):
			return False
	else:
		return False


# check a doucment based on name and category (i.e. country, city)
def verifyName( category, name ):
	r = requests.get(URL + category )
	if r != None:
		r = r.json()
		for c in r:
			if c['name'] == name:
				return True

		return False

	else:
		return False


# country tests...
def countryTests():
	# initialize our test statistics
	testsPassed = 0
	totalTests = 0

	# add a valid country
	totalTests = totalTests + 1
	payload = {"name": "Germany", 
		"population": 82175700, 
		"area": 137847}
	r = requests.post(URL, 
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if verifyName("country", "Germany") == True:
		testsPassed = testsPassed + 1
	else:
		print "Country test 1 failed..."
		print r.text

	# add a country that's invalid because of...
	# ... negative area
	totalTests = totalTests + 1
	payload = {"name": "Greece", 
		"population": 82175700, 
		"area": -5} 
	r = requests.post(URL, 
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if verifyName("country", "Greece") == False:
		testsPassed = testsPassed + 1
	else:
		print "Country test 2 failed..."
		print r.text

	# ... negative population
	totalTests = totalTests + 1
	payload = {"name": "Turkey", 
		"population": -15, 
		"area": 1000000} 
	r = requests.post(URL, 
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if verifyName("country", "Turkey") == False:
		testsPassed = testsPassed + 1
	else:
		print "Country test 3 failed..."
		print r.text

	# ... name already exists
	totalTests = totalTests + 1
	payload = {"name": "Germany", 
		"population": 82175700, 
		"area": 137847}
	r = requests.post(URL, 
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if verifyName("country", "Germany") == True:
		testsPassed = testsPassed + 1
	else:
		print "Country test 4 failed..."
		print r.text

	# delete a country
	totalTests = totalTests + 1
	payload = {"name": "United States", 
		"population": 82175700, 
		"area": 137847}

	# start by getting the country_id for testing later
	r = requests.get(URL)
	r = r.json()
	countryId = ""

	# and get a list of its cities so we can test cascade
	cityList = []
	for c in r:
		if c['name'] == "United States":
			countryId = str(c['_id'])
			for q in c['cities']:
				cityList.append(str(q['_id']))
				print "Adding " + q['name']

	# make the deletion
	r = requests.delete(URL + "country/" + countryId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))

	# check to see whether country still exists
	if verifyName("country", "United States") == False:
		r = requests.get(URL + "city/" + cityList[0])
		r = r.json()
		if 'name' not in r:	
			testsPassed = testsPassed + 1
		else:
			print "Country test 5 failed (no cascade)..."
			print r.text
	else:
		print "Country test 5 failed (still exists)..."
		print r.text

	# delete a country that doesn't exist
	totalTests = totalTests + 1
	payload = {}
	r = requests.get(URL)
	r = r.json()
	countryId = ""
	for c in r:
		if c['name'] == "United States":
			countryId = str(c['_id'])
	r = requests.delete(URL + "country/" + countryId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 404:
		testsPassed = testsPassed + 1
	else:
		print "Country test 6 failed..."
		print r.text

	# update a country that doesn't exist
	totalTests = totalTests + 1
	payload = {"name": "USA"} 
	r = requests.get(URL)
	r = r.json()
	countryId = ""
	for c in r:
		if c['name'] == "United States":
			countryId = str(c['_id'])
	r = requests.put(URL + "country/" + countryId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 404:
		testsPassed = testsPassed + 1
	else:
		print "Country test 7 failed..."
		print r.text

	# update a country that's invalid because of...
	# ... negative area
	totalTests = totalTests + 1
	payload = {"area": -5}
	r = requests.get(URL)
	r = r.json()
	countryId = ""
	for c in r:
		if c['name'] == "Germany":
			countryId = str(c['_id'])
	r = requests.put(URL + "country/" + countryId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 400:
		testsPassed = testsPassed + 1
	else:
		print "Country test 8 failed..."
		print r.text

	# ... negative population
	totalTests = totalTests + 1
	payload = {"population": -5} 
	r = requests.get(URL)
	r = r.json()
	countryId = ""
	for c in r:
		if c['name'] == "Germany":
			countryId = str(c['_id'])
	r = requests.put(URL + "country/" + countryId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 400:
		testsPassed = testsPassed + 1
	else:
		print "Country test 9 failed..."
		print r.text

	# ... name already exists
	totalTests = totalTests + 1
	payload = {"name": "France"} 
	r = requests.get(URL)
	r = r.json()
	countryId = ""
	for c in r:
		if c['name'] == "Germany":
			countryId = str(c['_id'])
	r = requests.put(URL + "country/" + countryId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 400:
		testsPassed = testsPassed + 1
	else:
		print "Country test 10 failed..."
		print r.text

	return testsPassed, totalTests


# city tests...
def cityTests():
	# initialize our test statistics
	testsPassed = 0
	totalTests = 0

	# add a valid city
	totalTests = totalTests + 1
	payload = {"name": "Chicago", 
		"country": "United States",
		"population": 2695598, 
		"category": "A" }
	r = requests.post(URL + "city", 
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if verifyName("city", "Chicago") == True:
		testsPassed = testsPassed + 1
	else:
		print "City test 1 failed..."
		print r.text

	# add a city that's invalid because of...
	# ... negative population
	totalTests = totalTests + 1
	payload = {"name": "Detroit", 
		"country": "United States",
		"population": -5, 
		"category": "A" }
	r = requests.post(URL + "city", 
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if verifyName("city", "Detroit") == False:
		testsPassed = testsPassed + 1
	else:
		print "City test 2 failed..."
		print r.text

	# ... name already exists
	totalTests = totalTests + 1
	payload = {"name": "New York", 
		"country": "United States",
		"population": -5, 
		"category": "A" }
	r = requests.post(URL + "city", 
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 400:
		testsPassed = testsPassed + 1
	else:
		print "City test 3 failed..."
		print r.text

	# delete a city
	totalTests = totalTests + 1
	failed = False
	r = requests.get(URL + "city")
	r = r.json()
	cityId = ""
	for c in r:
		if c['name'] == "Montreal":
			cityId = str(c['_id'])
	r = requests.delete(URL + "city/" + cityId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if verifyName("city", "Montreal") == False:
		# check to see whether it still exists in country
		r = requests.get(URL)
		r = r.json()
		for c in r:
			if c['name'] == "Canada":
				for q in c['cities']:
					if q['name'] == "Montreal":
						failed = True	
		if failed == False:
			testsPassed = testsPassed + 1

	else:
		print "City test 4 failed..."
		print r.text

	# delete a city that doesn't exist
	totalTests = totalTests + 1
	r = requests.get(URL + "city")
	r = r.json()
	cityId = ""
	for c in r:
		if c['name'] == "Montreal":
			cityId = str(c['_id'])
	r = requests.delete(URL + "city/" + cityId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 404:
		testsPassed = testsPassed + 1
	else:
		print "City test 5 failed..."
		print r.text

	# update a city that doesn't exist
	totalTests = totalTests + 1
	payload = {"name": "Ville de Montreal"}
	r = requests.get(URL + "city")
	r = r.json()
	cityId = ""
	for c in r:
		if c['name'] == "Montreal":
			cityId = str(c['_id'])
	r = requests.put(URL + "country/" + cityId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 404:
		testsPassed = testsPassed + 1
	else:
		print "City test 6 failed..."
		print r.text

	# update a city that's invalid because of...
	# ... negative population
	totalTests = totalTests + 1
	payload = {"population": -5}
	r = requests.get(URL + "city")
	r = r.json()
	cityId = ""
	for c in r:
		if c['name'] == "New York":
			cityId = str(c['_id'])
	r = requests.put(URL + "country/" + cityId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 400:
		testsPassed = testsPassed + 1
	else:
		print "City test 7 failed..."
		print r.text

	# ... name already exists
	totalTests = totalTests + 1
	payload = {"name": "Paris"}
	r = requests.get(URL + "city")
	r = r.json()
	cityId = ""
	for c in r:
		if c['name'] == "New York":
			cityId = str(c['_id'])
	r = requests.put(URL + "country/" + cityId,
		headers={'accept': 'application/json'},
		data=json.dumps(payload))
	if r.status_code == 400:
		testsPassed = testsPassed + 1
	else:
		print "City test 8 failed..."
		print r.text

	# return testsPassed / totalTests * 100
	return testsPassed, totalTests


# run our test suite
if __name__ == "__main__":
	print "Re-initializing database..."

	initCollections()
	passed, total = countryTests()

	print "Re-initializing database..."
	initCollections()

	cityPassed, cityTotal = cityTests()
	
	print ""
	print "--- Country tests ---"
	print str(passed) + "/" + str(total) + " = " + \
		str(passed*100/total) + "%"


	print "" 
	print "--- City tests ---"
	print str(cityPassed) + "/" + str(cityTotal) + " = " + \
		str(cityPassed*100/cityTotal) + "%"
	print ""

	print "Re-initializing database..."
	initCollections()
