# create user
kubectl create -f account.yaml

# create role
kubectl create -f role.yaml

# create user token for dashboard
kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')

# deploy dahshboard
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0-beta8/aio/deploy/recommended.yaml

# start dashboard
kubectl proxy

# acccess dashboard
http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/


# create zookeeper pod
kubectl create -f zookeeper.json

# create zookeeper service
kubectl create -f zookeer_service.json