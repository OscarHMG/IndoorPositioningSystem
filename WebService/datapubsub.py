#!/usr/bin/env python
import subscriber
import json
import math
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
messages_pulling=[] #[{"data": {"Location": "labihm", "Timestamp": "2017-01-16T15:29:05Z", "Username": "Xavier Pionce"}, "messageId": "105504316449790"}]

def get_data_filter(invitado,listado):
    global COUNT
    global candado
    global DATE_NOW
    founded = {}

    load_data = json.loads(invitado) # load json object
    load_listado = listado[:]

    #{"visitante":"Sergio"}
    print "--------Visitante: "
    print load_data
    print "-----------------"
    #print dataSource

    if(is_empty(load_listado)):
      founded = {"Mensaje": "Lista Vacia"}
    else:
        for index, item in enumerate(load_listado):
            print "Ver mensaje: ", item['username']
            if item['username'] == load_data['visitante']:
                print "encontrado"
                founded = item
                return json.dumps(founded)
            else:
                founded = {"Mensaje":"Not Found"}

    return json.dumps(founded)


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



def getOrientation(angle):
    load_data = json.loads(angle)
    instruction={}
    state_current = ""
    state_reference = ""

    if(is_empty(load_data)):
        return {"Orientation":"Data is empty"}
    else:
        print type(load_data['pointA'])
        angle_reference = calculate_initial_compass_bearing(load_data['pointA'],load_data['pointB'])
        angle_current = load_data['angle']

        #angle_current = math.degrees(angle_current)
        print "conversion normalizada:", angle_current
        compass_bearing_current = (angle_current + 360) % 360

        print "Angulo referencia: ", angle_reference
        print "----------------"
        print "Angulo actual: ",compass_bearing_current

        quadrant_current = set_Quadrants(compass_bearing_current)
        quadrant_reference = set_Quadrants(angle_reference)

        instruction={"instruction" : set_instruction(quadrant_current,quadrant_reference,compass_bearing_current,angle_reference)}

    return instruction




def set_Quadrants(angle):
    angle_see = angle
    quadrant = 1.0

    if (angle_see > 360):
        angle_see = angle_see %360

    if( angle_see >=0.0 and angle_see <=90.0):
        quadrant = 2.0
    elif (angle_see <= 180.0 and angle_see >90.0):
        quadrant = 3.0
    elif (angle_see <= 270.0 and angle_see > 180.0):
        quadrant = 4.0
    elif (angle_see >= 360.0 and angle_see <270.0) or (angle_see <0.0):
        quadrant = 1.0

    return quadrant

def set_instruction(*args):
    quadrant1 = args[0]
    quadrant2 = args[1]
    angle1 = args[2]
    angle2 = args[3]
    umbral = 30.0
    instruction = ""

    if (quadrant1 != quadrant2):
        if(quadrant2 +1.0 == quadrant1 or (quadrant2==4.0 and quadrant1==1)):
            instruction = "Gire a la derecha"
            return instruction
        elif (quadrant2 == 1.0  and quadrant1 == 3.0) or (quadrant2 == 2.0 and quadrant1 == 4.0) or (quadrant2 == 4.0 and quadrant1 == 2.0) or (quadrant2 == 3.0 and quadrant1 == 1.0):
            instruction = "De media vuelta"
            return instruction
        elif (quadrant2 == 1.0 and quadrant1 == 4.0) or (quadrant2 - 1.0 == quadrant1 ):
            instruction ="Gire a la izquierda"
            return instruction
    else:
        if(set_Quadrants(angle2+umbral) == quadrant1):
            if((angle2+umbral)< angle1):
                instruction = "Gire a la derecha"
                return instruction

        if(set_Quadrants(angle2-umbral) == quadrant1):
            if((angle2-umbral)> angle1):
                instruction = "Gire a la izquierda"
                return instruction

        instruction = "Siga de frente"

    return instruction


def convert_positive(numero):
    result=0.0
    if(numero<0.0):
        result = numero*(-1.0)
    else:
        result=numero

    return result





def calculate_initial_compass_bearing(pointA, pointB):

    if (type(pointA) != list) or (type(pointB) != list):
        raise TypeError("Only tuples are supported as arguments")

    lat1 = math.radians(pointA[0])
    lat2 = math.radians(pointB[0])

    diffLong = math.radians(pointB[1] - pointA[1])

    x = math.sin(diffLong) * math.cos(lat2)
    y = math.cos(lat1) * math.sin(lat2) - (math.sin(lat1)
            * math.cos(lat2) * math.cos(diffLong))

    initial_bearing = math.atan2(x, y)


    initial_bearing = math.degrees(initial_bearing)
    compass_bearing = (initial_bearing + 360) % 360

    return compass_bearing
    #return initial_bearing


                    
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

            







