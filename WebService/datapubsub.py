#!/usr/bin/env python
import subscriber
import json
from datetime import datetime, timedelta

TEST_TOPIC = 'navigator-location'  #testPositionServer  navigator-location
TEST_SUBSCRIPTION = 'pull_messages_serverhttp' # map-worker-sub   map-worker-dev pullDataPosition
DATE_NOW = datetime.today()#datetime.utcnow().strftime('%Y-%m-%d')
MAX = 7
COUNT = 0
candado = 0


def get_data_filter(invitado):
    global COUNT
    global candado
    global DATE_NOW

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

    for eachJsonObject in dataSource:
        for k,v in eachJsonObject['data'].iteritems():
            if v.startswith(date_time):
                print "Fecha es de hoy o mayor"
                candado=0

            if (load_data['visitante'] in eachJsonObject['data'].values()) and candado==0:
                 user  = (eachJsonObject['data']) # get the item
                 print "-----USER:"
                 print user
                 break
            else:
                candado = 1


    if candado != 0:
        print "Fecha pasada a la actual"
        COUNT+= 1
        user = {'Message':'Not Found'}
        if COUNT < MAX:
            get_data_filter(json.dumps(load_data))
        else:
            COUNT=0


    return json.dumps(user)

            







