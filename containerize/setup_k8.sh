# deploy dahshboard
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0-beta8/aio/deploy/recommended.yaml

# create user
kubectl create -f account.yaml

# create role
kubectl create -f role.yaml

# create user token for dashboard
kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')

# start dashboard
kubectl proxy &
