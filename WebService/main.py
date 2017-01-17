from flask import Flask, request
import json
import networkx as nx
from networkx.readwrite import json_graph
import subscriber
import publisher
import datapubsub
import graphsMethods

app = Flask(__name__)

MESSAGE=[]

app.config['DEBUG'] = False #True for local test


#Global Variables:
TEST_TOPIC = 'navigator-location'  #testPositionServer  navigator-location testPositionServer
TEST_SUBSCRIPTION = 'pull_messages_serverhttp' # map-worker-sub   map-worker-dev pull_messages_serverhttp
#message = '{ "Username": "Sergio Moncayo","Location": "labproto","Timestamp": "2017-01-18T15:29:05Z"}'
bandera = 0

# { "username": "Xavier Pionce","location": "labihm","timestamp": "2017-01-12T17:14:05Z"}

#Use for local test
#server_name="localhost"

# Note: We don't need to call run() since our application is embedded within
# the App Engine WSGI application server.

# Define routes for the examples to actually run


#/get_shortest_path
@app.route('/get_shortest_path',methods=['POST'])
def get_shortest_path():
    envelope = request.data.decode('utf-8')
    print envelope
    resultado = graphsMethods.shortest_path(envelope,'graphCTI.json') 
    return   resultado  #json.dumps(nx.dijkstra_path(Grafo,30,18))

#Get the message of 4 message available on the pubsub
@app.route('/pull_message')
def pull_message():
    global MESSAGE
    #url = 'https://api.github.com/users/runnable'
    #resultado = subscriber.receive_message(TEST_TOPIC,TEST_SUBSCRIPTION)
    resultado = datapubsub.pull_message_data()
    if datapubsub.is_empty(resultado):
        print "No se guarda"
    else:
        MESSAGE.append(resultado)

    # with open('dataSource.json', 'w') as outfile:
    #     json.dump(MESSAGE, outfile)

    return  json.dumps(MESSAGE)  #resultado #json.dumps(resultado)

#Get the last message available on the pubsub
@app.route('/pull_message_fast')
def pull_message_fast():
    resultado = subscriber.receive_message_fast(TEST_TOPIC,TEST_SUBSCRIPTION)#.replace('\"','')
    return  resultado #resultado #json.dumps(resultado)

#Get the properties of the graph
@app.route('/get_current_graph')
def get_current_graph():
    resultado =   graphsMethods.read_json_file('graphCTI.json') #graphIncompleteCTI  graphCTI.json 
    #dictionary=  json.dumps(resultado)
    return resultado

#Return if is successfull return the ack otherwise return a empty response
@app.route('/push_message',methods=['POST'])
def push_message():
    global MESSAGE
    lista_nueva = []
    global bandera 
    envelope = request.data.decode('utf-8') #\n\n
    print envelope
    data_publish = datapubsub.pubsub_push(envelope)
    #resultado = publisher.publish_message(TEST_TOPIC, str(envelope).replace('\n\n',''))
    if datapubsub.is_empty(data_publish):
        return "Not Found"
    else:
        #MESSAGE.append(data_publish)
        if datapubsub.is_empty(MESSAGE):
            #default_data.update({'item3': 3})
            MESSAGE.append(data_publish)
            print "Mensaje: ", MESSAGE
        else:
            lista_nueva = MESSAGE[:]
      
            if datapubsub.contains_item(data_publish,lista_nueva):
                print "Old item"
                data_publish = datapubsub.pubsub_push(envelope)
                for index, item in enumerate(lista_nueva):
                    #print "Ver mensaje: ", item['username']
                    print "tratado: ", data_publish['username']
                    if item['username'] == data_publish['username']:
                        lista_nueva.pop(index)
                        lista_nueva.insert(index,data_publish)
                        #lista_nueva[lista_nueva.index(item)]=data_publish
                
                print "Ver mensaje: ", lista_nueva
            else:
                print "new item"
                lista_nueva.insert(0, data_publish)

            MESSAGE = lista_nueva[:]
        
    return json.dumps(MESSAGE, indent=4)

#Return a json of the location of the user, if the user never find return 'Not found'
@app.route('/find_visitor',methods=['POST'])
def find_visitor():
    global MESSAGE
    #envelope = json.loads(request.data)#\n\n
    envelope = request.data.decode('utf-8')
    #print envelope
    resultado = datapubsub.get_data_filter(envelope)
    #resultado = publisher.publish_message(TEST_TOPIC, str(envelope).replace('\n\n',''))
    return  resultado #json.dumps(resultado)


@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return 'Sorry, nothing at this URL.', 404


@app.route('/')
def hello():
    """Return a friendly HTTP greeting."""
    return 'You are running this api rest web server'



# Using for local development
# Comment this lines when deploy this app in the Google Cloud
# if __name__ == '__main__':
# 	app.run( 
# 		host=server_name,
# 		port=int("8076")
# 	)