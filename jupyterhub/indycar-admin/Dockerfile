FROM jupyter/tensorflow-notebook:latest

RUN pip install --quiet --no-cache-dir paho-mqtt && \
	pip install --upgrade --quiet --no-cache-dir elyra && \
	jupyter lab build && \
	jupyter lab clean -y && \
    npm cache clean --force && \
    fix-permissions "${CONDA_DIR}" && \
    fix-permissions "/home/${NB_USER}"

USER root
RUN apt-get update && \
    apt-get install -yq --no-install-recommends \
    iputils-ping \
    openssh-client \
    openssh-server && \
    apt-get clean && rm -rf /var/lib/apt/lists/*


RUN pip install --upgrade --quiet --no-cache-dir jupyter-tensorboard && \
    pip install --upgrade --quiet --no-cache-dir jupyterlab_iframe && \
    jupyter labextension install jupyterlab_iframe && \
    jupyter labextension install jupyterlab_tensorboard && \
    jupyter lab build && \
    jupyter lab clean -y && \
    jupyter serverextension enable --py jupyterlab_iframe && \
    npm cache clean --force && \
    fix-permissions "${CONDA_DIR}" && \
    fix-permissions "/home/${NB_USER}"

RUN apt-get update && apt-get install -yq gnupg2  curl && \
    curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl && \
    chmod +x ./kubectl && mv ./kubectl /usr/local/bin



RUN pip install --upgrade --quiet --no-cache-dir ipymonaco && \
    jupyter labextension install ipymonaco && \
    jupyter lab build && \
    jupyter lab clean -y && \
    npm cache clean --force && \
    fix-permissions "${CONDA_DIR}" && \
    fix-permissions "/home/${NB_USER}"

USER $NB_UID

COPY code-snippets /opt/conda/share/jupyter/metadata/code-snippets 
