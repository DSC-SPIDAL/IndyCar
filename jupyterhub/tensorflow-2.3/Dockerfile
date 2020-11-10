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


RUN mkdir -p /var/run/sshd
COPY bin/* /usr/local/bin/
COPY sshd_config /etc/ssh/sshd_config


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

USER $NB_UID

COPY code-snippets /opt/conda/share/jupyter/metadata/code-snippets 

EXPOSE 22
