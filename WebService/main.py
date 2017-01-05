from flask import Flask, request
import json
import networkx as nx
from networkx.readwrite import json_graph
import subscriber
import publisher
import datapubsub
import graphsMethods

app = Flask(__name__)


app.config['DEBUG'] = True #True for local test


#Global Variables:
TEST_TOPIC = 'navigator-location'  #testPositionServer  navigator-location testPositionServer
TEST_SUBSCRIPTION = 'pull_messages_serverhttp' # map-worker-sub   map-worker-dev pull_messages_serverhttp
#message = '{ "Username": "Sergio","Location": "mainstairs","Timestamp": "2016-12-18T15:29:05Z"}'
#message2 = '{ "Username": "Oscar","Location": "labproto","Timestamp": "2016-12-18T19:29:05Z"}'
Grafo = nx.Graph()

#Use for local test
server_name="localhost"

# Note: We don't need to call run() since our application is embedded within
# the App Engine WSGI application server.

# Define routes for the examples to actually run


#/get_shortest_path
@app.route('/get_shortest_path',methods=['POST'])
def get_shortest_path():
    envelope = request.data.decode('utf-8')
    print envelope
    Grafo = graphsMethods.read_json_file_graph('graphCTI.json')
    #ver = nx.dijkstra_path(Grafo,0,18)
    resultado = graphsMethods.shortest_path(envelope,'graphCTI.json') 
    return   resultado  #json.dumps(nx.dijkstra_path(Grafo,30,18))

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
    return resultado#resultado #json.dumps(resultado)

#Get the last message available on the pubsub
@app.route('/pull_message_fast')
def pull_message_fast():
    resultado = subscriber.receive_message_fast(TEST_TOPIC,TEST_SUBSCRIPTION)#.replace('\"','')
    return  resultado #resultado #json.dumps(resultado)

@app.route('/get_current_graph')
def get_current_graph():
    resultado =   graphsMethods.read_json_file('graphCTI.json') #graphIncompleteCTI  graphCTI.json 
    #dictionary=  json.dumps(resultado)
    return resultado

#Return if is successfull return the ack otherwise return a empty response
@app.route('/push_message',methods=['POST'])
def push_message():
    envelope = request.data.decode('utf-8') #\n\n
    print envelope
    
    resultado = publisher.publish_message(TEST_TOPIC, str(envelope).replace('\n\n',''))
    return json.dumps(resultado, indent=4)

#Return a json of the location of the user, if the user never find return 'Not found'
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
    return 'You are running this api rest web server'



#Using for local development
#Comment this lines when deploy this app in the Google Cloud
if __name__ == '__main__':
	app.run( 
		host=server_name,
		port=int("8075")
	)