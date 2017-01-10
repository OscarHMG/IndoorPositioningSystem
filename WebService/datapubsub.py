#!/usr/bin/env python
import subscriber
import json

TEST_TOPIC = 'navigator-location'  #testPositionServer  navigator-location
TEST_SUBSCRIPTION = 'pull_messages_serverhttp' # map-worker-sub   map-worker-dev pullDataPosition

def get_data_filter(invitado):
    load_data = json.loads(invitado) # load json object
    datasuscriber = subscriber.receive_message(TEST_TOPIC,TEST_SUBSCRIPTION) # load the messages available
    dataSource= json.loads(datasuscriber) #convert the messages in json object
    #{"visitante":"Sergio"}
    print "--------Visitante: "
    print load_data
    print "-----------------"
    #print dataSource
    user={}
    #data = json.dumps(datasuscriber)
    for eachJsonObject in dataSource:
        if load_data['visitante'] in eachJsonObject['data']:
            user=(eachJsonObject['data']) # get the item
            print "-----USER:"
            print user
            return user
        else:
            user = {'Message':'Not Found'}

    return (user)



