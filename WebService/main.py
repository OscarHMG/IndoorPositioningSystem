from flask import Flask, request
import json
import subscriber
import publisher
import datapubsub

app = Flask(__name__)
app.config['DEBUG'] = False #True for local test

TEST_TOPIC = 'navigator-location'  #testPositionServer  navigator-location testPositionServer
TEST_SUBSCRIPTION = 'pull_messages_serverhttp' # map-worker-sub   map-worker-dev pullDataPosition pull_messages_serverhttp
message = '{ "Username": "Sergio","Location": "mainstairs","Timestamp": "2016-12-18T15:29:05Z"}'
#message2 = '{ "Username": "Oscar","Location": "labproto","Timestamp": "2016-12-18T19:29:05Z"}'

#Use for local test
#server_name="localhost"

# Note: We don't need to call run() since our application is embedded within
# the App Engine WSGI application server.

# Define routes for the examples to actually run

#Get the message of 4 message available on the pubsub
@app.route('/pull_message')
def pull_message():
    #url = 'https://api.github.com/users/runnable'
    # this issues a GET to the url. replace "get" with "post", "head",
    # "put", "patch"... to make a request using a different method
    #r = requests.get(url)
    # return json.dumps(r.json(), indent=4)
    #words = [w.replace('[br]', '<br />') for w in words]
    resultado = subscriber.receive_message(TEST_TOPIC,TEST_SUBSCRIPTION)#.replace('\"','\'')
    #print "--------------"
    #print json.loads(resultado)
    return resultado#resultado #json.dumps(resultado)

@app.route('/pull_message_fast')
def pull_message_fast():
    #url = 'https://api.github.com/users/runnable'
    # this issues a GET to the url. replace "get" with "post", "head",
    # "put", "patch"... to make a request using a different method
    #r = requests.get(url)
    # return json.dumps(r.json(), indent=4)
    resultado = subscriber.receive_message_fast(TEST_TOPIC,TEST_SUBSCRIPTION)#.replace('\"','')
    return  resultado#resultado #json.dumps(resultado)

@app.route('/push_message',methods=['POST'])
def push_message():
    envelope = request.data.decode('utf-8') #\n\n
    print envelope
    
    resultado = publisher.publish_message(TEST_TOPIC, str(envelope).replace('\n\n',''))
    return json.dumps(resultado, indent=4)

@app.route('/find_visitor',methods=['POST'])
def find_visitor():
    #envelope = json.loads(request.data)#\n\n
    envelope = request.data
    #print envelope
    resultado = datapubsub.get_data_filter(envelope)
    #resultado = publisher.publish_message(TEST_TOPIC, str(envelope).replace('\n\n',''))
    return json.dumps(resultado)


@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return 'Sorry, nothing at this URL.', 404
#def push_message2():
 #   resultado = publisher.publish_message(TEST_TOPIC, message2)
  #  return json.dumps(resultado, indent=4)


@app.route('/')
def hello():
    """Return a friendly HTTP greeting."""
    return 'You are running this web server'



#Using for local development
#Comment this lines when deploy this app in the Google Cloud
#if __name__ == '__main__':
#	app.run( 
#		host=server_name,
#		port=int("8072")
#	)