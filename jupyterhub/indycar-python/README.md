
# IndyCar Deployment Codes for Kubernetes

To use the python codes:

1. Kubernetes config files should be in `$HOME/.kube`. Kubernetes python api will read the configurations from the default location.
2. Python path should be set

```
import os
os.environ['PYTHONPATH']='/path-to-file/indycar-python/iuindycar'
import iuindycar.orchestrator as orc
```
3. Required files should be on nfs
4. Docker images should be ready on each nodes.