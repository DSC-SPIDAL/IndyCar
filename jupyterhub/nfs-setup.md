# NFS Setup

We use NFS to store our shared data. Then we mount data folders to JupyterHub Servers.

The following instructions were taken from: https://www.tecmint.com/install-nfs-server-on-ubuntu/

We use the first node as NFS Server and rest as client. You may setup differently.

## 1. Server Setup

### 1.1. Install NFS Kernel Server

```
sudo apt install nfs-kernel-server
```



### 1.2. Create  directories

```
mkdir /nfs /nfs/indycar
sudo chown -R nobody:nogroup /nfs
sudo chmod 777 /nfs
```



### 1.3. Grant Access to Clients

```
sudo vim /etc/exports
```
Add the followings to the file. Note that you need to add either each client's IP or you can enable it to entire subnet.
```
/nfs/indycar    second-node-ip(rw,sync,no_subtree_check,no_root_squash)
/nfs/indycar    third-node-ip(rw,sync,no_subtree_check,no_root_squash)
```

### 1.4 Export the NFS Directory

```
sudo exportfs -a
sudo systemctl restart nfs-kernel-server
```



## 2. Client Setup

```
sudo apt-get update
sudo apt install nfs-common
sudo mkdir /nfs /nfs/indycar
sudo mount server-ip:/nfs/indycar  /nfs/indycar
```

