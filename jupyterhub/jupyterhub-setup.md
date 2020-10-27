# JupyterHub Setup

Run all of the commands on the master node.

## 1. MetalLB Setup

This is required if you are using Kubernetes on bare-metal (not on cloud server). MetalLB is a load balancer for bare-metal machines.

The following instructions were taken from: https://metallb.universe.tf/installation/ and https://metallb.universe.tf/configuration/

```
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.9.4/manifests/namespace.yaml
kubectl apply -f https://raw.githubusercontent.com/metallb/metallb/v0.9.4/manifests/metallb.yaml
# On first install only
kubectl create secret generic -n metallb-system memberlist --from-literal=secretkey="$(openssl rand -base64 128)"
```

Create a configuration file `metallb_config.yaml` and add the following lines. Note that applications will get IP addresses from the load balancers. Therefore, you need to have static IP addresses that are not managed by a DHCP server. At least two IP addresses are recommended. But be aware that you will need to have more IP addresses if you want to expose your applications to outside and have separate IP addresses for each application.

```
apiVersion: v1 
kind: ConfigMap
metadata:
  namespace: metallb-system
  name: config
data:
  config: |
    address-pools:
    - name: default
      protocol: layer2
      addresses:
      - <ip-range-start>-<ip-range-end>
```

To apply the configuration:

```
kubectl apply -f metallb_config.yaml
```



## 2. Install Helm

The following instructions were taken from: https://helm.sh/docs/intro/install/ and https://medium.com/@georgepaw/jupyterhub-with-kubernetes-on-single-bare-metal-instance-tutorial-67cbd5ec0b00

```
kubectl taint nodes --all node-role.kubernetes.io/master-

kubectl --namespace kube-system create serviceaccount tiller
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller

curl https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get | bash
helm init --service-account tiller --wait

kubectl patch deployment tiller-deploy --namespace=kube-system --type=json --patch='[{"op": "add", "path": "/spec/template/spec/containers/0/command", "value": ["/tiller", "--listen=localhost:44134"]}]'

helm install stable/nfs-server-provisioner --namespace nfsprovisioner --set=storageClass.defaultClass=true


helm repo add jupyterhub https://jupyterhub.github.io/helm-chart/
helm repo update
```



## 3. Deploy ingress-nginx using Helm

```
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm install my-release ingress-nginx/ingress-nginx
```



## 4. Test if MetalLB and ingress-nginx are working

```
kubectl get svc
```

you should see `my-release-ingress-nginx-controller` and the external ip address should be one of the MetalLB managed ip addresses.

## 5. Create namespace jhub

```
kubectl create namespace jhub
```

## 5. DockerHub Secret

```
kubectl create secret docker-registry <your-docker-registery-name> --docker-server=https://index.docker.io/v1/ --docker-username=<dockerhub-user> --docker-password=<dockerhub-pass>  --docker-email=<email> --namespace=jhub
```

## 6. IndyCar Volume Mount

We assume that IndyCar folders  are located in `/nfs/indycar/shared`

Create a file `pv-indycar.yaml` and add:

```
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv-jhub-indycar
  namespace: jhub
  labels:
    type: local
spec:
  storageClassName: manual
  capacity:
    storage: 30Gi
  accessModes:
    - ReadOnlyMany
  hostPath:
    path: "/nfs/indycar/shared"
```

Then run: `kubectl apply  -f pv-indycar.yaml`. This will create a persistent volume.

The next step is a persistent volume claim. Create a file named `pv-indycar-claim.yaml` and add:

```
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: pv-jhub-indycar-claim
  namespace: jhub
spec:
  storageClassName: manual
  accessModes:
    - ReadOnlyMany
  resources:
    requests:
      storage: 30Gi
```

Then run: `kubectl apply -f pv-indycar-claim.yaml`

## 7. Deploy JupyterHub using Helm

JupyterHub requires a proxy secret token. To create:

```
openssl rand -hex 32
```

Save the output. We will use it.

Now create a configuration file named `jupyterhub_config.yaml`.  An example configuration is below:

```
proxy:
  secretToken: "replace this with your token"
  https:
    enabled: true
    hosts:
      - host-address
    letsencrypt: # gets an SSL certificate from letstencrypt
      contactEmail: email-address
    service:
      loadBalancerIP: ip-address # this should be of of MetalLB managed ip address

auth:
  admin:
    users:
      - adminuser
  type: dummy
  # dummy authenticator, all passwords are same. Only whitelisted users can login
  dummy:
    password: "a password"
  whitelist:
    users:
      - adminuser
      - normaluser

singleuser:
  defaultUrl: "/lab" # will login to jupyterlab instead of jupyter
  cpu:
    limit: 6 # up to 6 cores but 3 guaranteed
    guarantee: 3
  memory:
    limit: 48G
    guarantee: 24G
  storage:
    capacity: 25Gi # each user will have 25 GB of persistent disk
    dynamic:
      pvcNameTemplate: claim-{username}
      volumeNameTemplate: volume-{username}
      storageAccessModes: [ReadWriteMany]

cull:
  timeout: 28800 # kill the server after 8 hours if user is inactive.
  every: 240
  
hub:
  allowNamedServers: true
  namedServerLimitPerUser: 2
  imagePullSecret:
    enabled: true
    username: <dockerhub-user-name>
    password: <dockerhub-user-pass>
    email: <email>
    registry: <your-docker-registery-name>

```

Then run:

```
RELEASE=jhub
NAMESPACE=jhub

helm upgrade --cleanup-on-fail \
  --install $RELEASE jupyterhub/jupyterhub \
  --namespace $NAMESPACE \
  --create-namespace \
  --version=0.9.0 \
  --values jupyterhub_config.yaml
```





