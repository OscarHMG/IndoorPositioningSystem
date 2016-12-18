from flask import Flask
import requests
import json
import subscriber
import publisher

app = Flask(__name__)
app.config['DEBUG'] = True #True for local test

TEST_TOPIC = 'navigator-location'  #testPositionServer  navigator-location testPositionServer
TEST_SUBSCRIPTION = 'map-worker-sub' # map-worker-sub   python-visualizer  map-worker-dev pullDataPosition
message = '{ "Username": "Sergio","Location": "mainstairs","Timestamp": "2016-12-18T15:29:05Z"}'
message2 = '{ "Username": "Oscar","Location": "labproto","Timestamp": "2016-12-18T19:29:05Z"}'

#Use for local test
server_name="localhost"

# Note: We don't need to call run() since our application is embedded within
# the App Engine WSGI application server.

# Define routes for the examples to actually run
@app.route('/pull_message')
def pull_message():
    #url = 'https://api.github.com/users/runnable'
    # this issues a GET to the url. replace "get" with "post", "head",
    # "put", "patch"... to make a request using a different method
    #r = requests.get(url)
    # return json.dumps(r.json(), indent=4)
    resultado = subscriber.receive_message(TEST_TOPIC,TEST_SUBSCRIPTION).replace('\"','')
    return json.dumps(resultado)

@app.route('/push_message')
def push_message():
    resultado = publisher.publish_message(TEST_TOPIC, message)
    return json.dumps(resultado, indent=4)

@app.route('/push_message2')
def push_message2():
    resultado = publisher.publish_message(TEST_TOPIC, message2)
    return json.dumps(resultado, indent=4)


@app.route('/')
def hello():
    """Return a friendly HTTP greeting."""
    return 'You are running this web server'

@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return 'Sorry, nothing at this URL.', 404


#Using for local development
#Comment this lines when deploy this app in the Google Cloud
if __name__ == '__main__':
	app.run( 
		host=server_name,
		port=int("8070")
	)