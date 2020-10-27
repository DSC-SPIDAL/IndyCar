# Docker & Kubernetes Setup



## 0. Create a new user

```
adduser kube
usermod -aG sudo kube
# kube is username. Feel free to use another user name
```

## 1. Install Docker-CE

The following instructions were taken from: https://docs.docker.com/engine/install/ubuntu/

The instructions were tested on Ubuntu 18.04

```bash
sudo apt-get update
sudo apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common
```

Add GPG Key:

```
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
```

Add the repository:

```
sudo add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"
```

Install Docker Engine:

```
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io
```

Using docker as a non-root user:

```
sudo usermod -aG docker kube
# kube is username. Feel free to use another user name
```

Testing the setup:

Log out and login again to run docker without sudo. Then:

```
docker run hello-world
```

Output should be similar to:

```
Hello from Docker!
This message shows that your installation appears to be working correctly.
...
```

To run Docker automatically if the node is restarted:

```
sudo systemctl enable docker
```



## 2. Install Kubernetes

Kubernetes installation instructions were taken from: https://phoenixnap.com/kb/install-kubernetes-on-ubuntu

Add GPG key:

```
curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key add
```

Add Repository:

```
sudo apt-add-repository "deb http://apt.kubernetes.io/ kubernetes-xenial main"
```

Install Kubernetes:

```
sudo apt-get install kubeadm kubelet kubectl
sudo apt-mark hold kubeadm kubelet kubectl
```

Set hostname (Optional):

This part is not required if nodes already have different hostnames.

```
sudo hostnamectl set-hostname <name>
# replace <name> with "master" on the master node
# replace <name> with "worker01" on the first worker and so on
```

Disable swap:

```
sudo swapoff -a
```

### Kubernetes Master Configuration

```
sudo kubeadm init --pod-network-cidr=10.244.0.0/16
```

Save the `kube join` part of the previous command's output. We will use that part for workers to join the Kubernetes cluster.

```
# example join command: kubeadm join 149.165.150.72:6443 --token rkq33o.pni7zmwy9sx5f7hl \
    --discovery-token-ca-cert-hash sha256:c3af1143...ef4b56674c19e7e0 
```

Copy Kubernetes configuration files to home directory:

```
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

Deploy Pod Network:

```
sudo kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
```



### Joining Workers

Run the `kube join` command with `sudo` on all workers to join workers to the cluster. 

Example:

```
sudo kubeadm join master-ip:6443 --token rkq33o.pni7zmwy9sx5f7hl \
    --discovery-token-ca-cert-hash sha256:c3af1143...ef4b56674c19e7e0 
```



### Check if All Workers Are Joined

Run the following code on the master node:

```
kubectl get nodes
```

Example output:

```
kube@e002:~$ kubectl get nodes
NAME   STATUS   ROLES    AGE    VERSION
e002   Ready    master   9m4s   v1.19.3
e003   Ready    <none>   55s    v1.19.3
```





