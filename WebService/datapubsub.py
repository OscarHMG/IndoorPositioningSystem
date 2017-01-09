#!/usr/bin/env python
import subscriber
import json

TEST_TOPIC = 'navigator-location'  #testPositionServer  navigator-location testPositionServer
TEST_SUBSCRIPTION = 'pull_messages_serverhttp' # map-worker-sub   map-worker-dev pullDataPosition

def get_data_filter(invitado):
    #invi = invitado
    datasuscriber = subscriber.receive_message(TEST_TOPIC,TEST_SUBSCRIPTION)
    dataSource= json.loads(datasuscriber)
    #print "-----------------"
    #print invitado
    #print "-----------------"
    #print dataSource
    user={}
    #data = json.dumps(datasuscriber)
    for eachJsonObject in dataSource:
        if invitado in eachJsonObject['data']:
            user=(eachJsonObject['data'])
        else:
            user = {'Message':'Not Found'}

    return (user)



