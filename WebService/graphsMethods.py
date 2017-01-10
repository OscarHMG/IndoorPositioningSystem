import networkx as nx
from networkx.readwrite import json_graph
import json


#Method to read ther properties of the graph
def read_json_file(filename):
    with open(filename) as f:
        js_graph = json.load(f)
    grafo = json_graph.node_link_graph(js_graph)
    N,K = grafo.number_of_nodes(), grafo.number_of_edges()
    nodos = ("Nodes: ", N)
    arcos = ("Edges: ", K)
    return "grafo: "+str(nodos) +str(arcos)

#Method to load the graph on memory
def read_json_file_graph(filename):
    with open(filename) as f:
        js_graph = json.load(f)
    grafo = json_graph.node_link_graph(js_graph)
    return grafo

#Method to get the specific shortest path
def shortest_path(puntosJson,filename):
    load_data = json.loads(puntosJson)
    #print load_data
    G = read_json_file_graph(filename)
    location = nx.get_node_attributes(G,'location')
    #print location

    inicial = [nodo for nodo in location.items() if nodo[1] == load_data['inicio']]
    print "------------------inicio:"
    nodo_inicial = [item[0] for item in inicial]
    print nodo_inicial[0]
    final = [nodo for nodo in location.items() if nodo[1] == load_data['fin']]
    print "------------------fin:"
    nodo_final = [item[0] for item in final]

    print nodo_final[0]

    resultado = nx.dijkstra_path(G,nodo_inicial[0],nodo_final[0])

   #  lat , lng
    position = nx.get_node_attributes(G,'position')

    lista_nodo = G.nodes(data=True) # get a list of nodes with attributes

    path = []

                
    # for id_key in resultado:
    #     for nodo in position.items():
    #         if nodo[0]== id_key:
    #            result_ver =  nodo
    #            print result_ver
    #            path.append(result_ver)
    for id_key in resultado:
       result_ver =  [nodo for nodo in position.items() if nodo[0]== id_key]
       path.append(result_ver)
    
          
    print path

    #resultado = nx.dijkstra_path(grafo,1,18)  path not found
    return json.dumps(path)


#search(myDict, 'Mary')
def search(values, searchFor):
    for k in values:
        for v in values[k]:
            if searchFor in v:
                return k
    return None