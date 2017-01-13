#!/usr/bin/env python
import subscriber
import json
from datetime import datetime, timedelta

TEST_TOPIC = 'navigator-location'  #testPositionServer  navigator-location
TEST_SUBSCRIPTION = 'pull_messages_serverhttp' # map-worker-sub   map-worker-dev pullDataPosition
DATE_NOW = datetime.today()#datetime.utcnow().strftime('%Y-%m-%d')
MAX = 10
MAX_MESSAGE = 60
COUNT = 0
COUNT_MESSAGE=0
candado = 0
candado_message = True
founded = {}
messages_pulling=[] #[{"data": {"Location": "labihm", "Timestamp": "2017-01-16T15:29:05Z", "Username": "Xavier Pionce"}, "messageId": "105504316449790"}]

def get_data_filter(invitado):
    global COUNT
    global candado
    global DATE_NOW
    global founded

    load_data = json.loads(invitado) # load json object
    datasuscriber = subscriber.receive_message(TEST_TOPIC,TEST_SUBSCRIPTION) # load the messages available
    dataSource= json.loads(datasuscriber) #convert the messages in json object
    delta_time= timedelta(days=COUNT)
    current_date = DATE_NOW + delta_time
    date_time = current_date.strftime('%Y-%m-%d')
    print "FECHA: ", date_time

    #{"visitante":"Sergio"}
    print "--------Visitante: "
    print load_data
    print "-----------------"
    #print dataSource


    user={}

    print "dataSource: ",dataSource

    if  is_empty(dataSource):
         candado=1
         user = {'Message':'Not Found'}
    else:
        candado=0

        for eachJsonObject in dataSource:
            print "Data values: ",eachJsonObject['data'].values()
            if (load_data['visitante'] in eachJsonObject['data'].values()) and candado==0:
                 user  = (eachJsonObject['data']) # get the item
                 founded = json.dumps(user)
                 print "-----USER:"
                 print user
                 break
            else:
                 candado = 1

            # for k,v in eachJsonObject['data'].iteritems():
            #     if v.startswith(date_time):
            #         candado=0

    if candado != 0:
        print "Fecha pasada a la actual"
        COUNT+= 1
        user = {'Message':'Not Found'}
        founded = json.dumps(user)
        if COUNT < MAX:
            get_data_filter(json.dumps(load_data))
        else:
            COUNT=0
    else:
        print "Fecha es de hoy o mayor"

    return founded


def pubsub_push(mensaje):
    global founded
    global DATE_NOW
    DATE_NOW = datetime.today()
    current_date = DATE_NOW 
    date_time = current_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]
    load_data = json.loads(mensaje) # load json object


    if is_empty(load_data):
        print "Empty request data"
    else:
        mensaje={'username': load_data['username'],'location':load_data['location'], 'timestamp': date_time}
        founded=mensaje

    return founded


    


def pull_message_data():
    global COUNT_MESSAGE
    global messages_pulling
    global DATE_NOW
    global candado_message
    global COUNT

    datasuscriber = subscriber.receive_message(TEST_TOPIC,TEST_SUBSCRIPTION) # load the messages available
    dataSource= json.loads(datasuscriber) #convert the messages in json object

    delta_time= timedelta(seconds=COUNT_MESSAGE)
    delta_static_time = timedelta(seconds=30)
    delta_time2= timedelta(hours=5)
    
    #datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]
    
    #print "datos: ",dataSource

    if  is_empty(dataSource):
         print "Lista vacia"
         candado_message=True
    else:
        print "Lista con datos"
        DATE_NOW = datetime.today()
        current_date = DATE_NOW + delta_time2+delta_static_time
        current_date = current_date - delta_time
        date_time = current_date.strftime('%Y-%m-%dT%H')
        print "------------------"
        for eachJsonObject in dataSource:
            print "Data values: ",eachJsonObject['data']
            valor = eachJsonObject['data']
            print "FECHA: ", date_time
            if valor['timestamp'].startswith(date_time):
                print "user: ",valor
                messages_pulling.append(valor)
            #print (data[0]['data']['Timestamp'].startswith('2017-01-11'))  --> false
            #     date_time = current_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-7]   

    if candado_message:
        print "Not Found message"
        COUNT+=1
        if COUNT < MAX:
            COUNT_MESSAGE=0
            pull_message_data()
        else:
            #del messages_pulling[:] 
            candado_message = False
            COUNT=0
    else:
        print "Found message"

    return messages_pulling

                    
def contains_item(mensaje,data):
    for index, item in enumerate(data):
        print "Ver mensaje: ", item['username']
        if item['username'] == mensaje['username']:
            return True
    return False


def is_empty(any_structure):
    if any_structure:
        print('Structure is not empty.')
        return False
    else:
        print('Structure is empty.')
        return True

            







