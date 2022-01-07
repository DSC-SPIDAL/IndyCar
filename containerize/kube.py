from cloudmesh.common.Shell import Shell
import json
from pprint import pprint

class kubectl:

    def __init__(self):
        self.update()
        self.IP = Shell.run("minikube ip").strip() 


    def _update(self,kind):
        try:
            r = json.loads(Shell.run(f"kubectl get {kind} -o json"))
        except:
            r = None

        return r
    
    def update(self):
        self._pods = self._update("pods")
        self._services = self._update("services")        


    def pod(self, name):
        for element in self._pods["items"]:
            if element["metadata"]["name"] == name:
                return element
        return None

    def pod_ip(self, name):
        return k.pod(name)["status"]["hostIP"]

    def pod_status(self, name):
        return k.pod(name)["status"]["containerStatuses"][0]["ready"]

    def pod_info(self, name):
        p = self.pod(name)
        pprint (p)

    def service(self, name):
        for element in self._services["items"]:
            if element["metadata"]["name"] == name:
                return element
        return None

    def service_ip(self, name):
        try:
            return k.service(name)["spec"]["clusterIP"]
        except:
            return None
    
    def service_ip(self, name):
        try:
            return k.service(name)["spec"]["clusterIP"]
        except:
            return None
        
    def service_port(self, name):
        try:
            return k.service(name)["spec"]["ports"][0]["port"]
        except:
            return None
        
    def service_info(self, name):
        p = self.service(name)
        pprint (p)
        
    
if __name__ == "__main__":
    k = kubectl()

    #k.pod_info("zookeeper")

    #print(k.pod_ip("zookeeper"))
    #print(k.pod_status("zookeeper"))

    names = ["zookeeper", "nimbus", "storm-ui"]
    for name in names:
        print(f'Pod     {name}: {k.pod_ip(name)} {k.pod_status(name)}')    
        # k.pod_info(name)

    
    #k.service_info("zookeeper")

    for name in names:
        print(f'Service {name}: {k.service_ip(name)}:{k.service_port(name)}')    
        # k.service_info(name)


    print ("minikube IP:", k.IP)
